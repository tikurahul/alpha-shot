import co.touchlab.kermit.Logger
import com.juul.kable.Advertisement
import com.juul.kable.Bluetooth
import com.juul.kable.ConnectionLostException
import com.juul.kable.Filter
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.State
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import com.juul.kable.peripheral
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class SonyCameraControl(private val platform: Platform) {
  /** The [Scanner] used to scan for the Camera. */
  private val scanner = Scanner {
    filters =
        listOf(Filter.ManufacturerData(id = SONY_ID, data = sonyData, dataMask = sonyDataMask))
    logging {
      engine = SystemLogEngine
      level = Logging.Level.Warnings
      format = Logging.Format.Multiline
    }
  }

  /** The [CoroutineScope] for the BLE peripheral device. */
  private val coroutineScope = newScope()

  /** The last known [Peripheral] instance that we connected to. */
  private var peripheral: Peripheral? = null

  /** Start off with a state that says that we are initiating a connection over Bluetooth. */
  private val state: MutableStateFlow<State> = MutableStateFlow(State.Connecting.Bluetooth)

  fun scan(): Flow<Advertisement> = scanner.advertisements

  fun dispose() {
    scope.launch {
      peripheral?.disconnect()
      coroutineScope.cancel()
    }
  }

  fun cameraState(): StateFlow<State> {
    return state
  }

  suspend fun connect() {
    coroutineScope.launch { findAndConnect() }
  }

  suspend fun capturePhoto() {
    coroutineScope.launch {
      acquireFocus()
      captureRequest()
      val pictureAcquired = peripheral?.awaitNotification { it.contentEquals(PICTURE_ACQUIRED) }
      if (pictureAcquired != null) {
        Logger.d(TAG) { "Acquired Photo." }
      }
      shutterUpRequest()
      reset()
    }
  }

  private suspend fun findAndConnect() {
    val advertisement = advertisementFlow().first()
    val peripheral = coroutineScope.peripheral(advertisement)
    Logger.d(TAG) { "Ready to use peripheral $peripheral" }
    this.peripheral = peripheral
    // Relay state transitions from the peripheral.
    coroutineScope.launch {
      peripheral.state.collect {
        Logger.d(TAG) { "Peripheral State: $it" }
        state.value = it
      }
    }
    try {
      peripheral.connect()
    } catch (exception: ConnectionLostException) {
      // Will reconnect.
    }
    // Setup auto-reconnect behavior outside of the Bluetooth Stack.
    enableAutoReconnect()
  }

  private suspend fun acquireFocus() {
    coroutineScope.launch {
      // Send the focus reset command
      reset()
      // Send the request focus command
      focusRequest()
      // Wait for a response
      val focusAcquired = peripheral?.awaitNotification { it.contentEquals(FOCUS_ACQUIRED) }
      if (focusAcquired != null) {
        Logger.d(TAG) { "Acquired focus" }
      }
    }
  }

  /** Dispatches the reset command on the Sony Peripheral device. */
  private suspend fun reset() {
    peripheral?.write(remoteCommand, byteArrayOf(1, 6), WriteType.WithResponse)
  }

  /** Dispatches the reset command on the Sony Peripheral device. */
  private suspend fun focusRequest() {
    peripheral?.write(remoteCommand, byteArrayOf(1, 7), WriteType.WithResponse)
  }

  private suspend fun captureRequest() {
    peripheral?.write(remoteCommand, byteArrayOf(1, 9), WriteType.WithResponse)
  }

  private suspend fun shutterUpRequest() {
    peripheral?.write(remoteCommand, byteArrayOf(1, 8), WriteType.WithResponse)
  }

  private suspend fun enableAutoReconnect() {
    val combined =
        combine(Bluetooth.availability, state) { availability: Bluetooth.Availability, state: State
          ->
          availability to state
        }
    combined
        .filter { (availability, state) ->
          availability == Bluetooth.Availability.Available && (state is State.Disconnected)
        }
        .first()
    coroutineScope.ensureActive()
    peripheral = null
    Logger.d(TAG) { "Waiting to reconnect to camera." }
    delay(reconnectDelay)
    findAndConnect()
  }

  private fun advertisementFlow(): Flow<Advertisement> {
    val advertisementFlow =
        scanner.advertisements
            // Only look for devices that we have paired with in the past
            .filter { it.isConnectable == true && platform.bleFilter().invoke(it) }

    advertisementFlow.launchIn(coroutineScope)
    return advertisementFlow
  }

  companion object {
    const val TAG = "Sony Camera Control"

    /** The delay if we temporarily get reconnected. */
    private val reconnectDelay = 1.seconds

    private val peripheralJob = Job()

    //  The scope that manages the lifetime of the peripherals we connect to.
    private val scope = CoroutineScope(Dispatchers.IO + peripheralJob)

    /** Sony Manufacturer Id. */
    const val SONY_ID = 0x012D // Endianness

    /** Manufacturer specific data payload in the advertising packet. */
    // [3, 0, 101, 0, 85, 49, 34, -65, 0, 35, -73, 12, 33, 96, 0, 0, 0, 0, 0, 0]
    val sonyData = byteArrayOf(0x03, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

    /**
     * The actual data mask that defines the parts of the data that we care about. Here we only care
     * that the first 2 bytes match [3, 0] which means that the device we are connecting to is a
     * Camera.
     */
    val sonyDataMask =
        byteArrayOf(
            0xFF.toByte(), 0xFF.toByte(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

    /** The Remote Camera Control Descriptor. */
    private const val CAMERA_CONTROL = "8000FF00-FF00-FFFF-FFFF-FFFFFFFFFFFF"

    /** The Characteristic id for `REMOTE_COMMAND` interface. */
    private const val REMOTE_COMMAND = "0000ff01-0000-1000-8000-00805f9b34fb"

    /** The Characteristic id for the `REMOTE_NOTIFY` interface. */
    private const val REMOTE_NOTIFY = "0000ff02-0000-1000-8000-00805f9b34fb"

    /** The `REMOTE_COMMAND` [com.juul.kable.Characteristic] instance. */
    private val remoteCommand = characteristicOf(CAMERA_CONTROL, REMOTE_COMMAND)

    /** The `REMOTE_NOTIFY` [com.juul.kable.Characteristic] instance. */
    private val remoteNotify = characteristicOf(CAMERA_CONTROL, REMOTE_NOTIFY)

    /** A payload that represents that the Camera has acquired focus. */
    private val FOCUS_ACQUIRED = byteArrayOf(0x02, 0x3F, 0x20)

    /** The payload sent by the camera once a photo has been acquired. */
    private val PICTURE_ACQUIRED = byteArrayOf(0x02, 0xA0.toByte(), 0x20)

    /** Creates a [CoroutineScope] per [com.juul.kable.Peripheral] that we connect to. */
    private fun newScope(): CoroutineScope {
      // https://github.com/JuulLabs/kable/issues/577
      // Create an intermediate scope to Kable can reap the threads on disconnect.
      val job = Job(scope.coroutineContext.job)
      return CoroutineScope(scope.coroutineContext + job)
    }

    private suspend fun Peripheral.awaitNotification(
        maxDelay: Duration = 1.seconds,
        predicate: (contents: ByteArray) -> Boolean
    ): ByteArray? {
      return withTimeoutOrNull<ByteArray?>(maxDelay.inWholeMilliseconds) {
        val observation = observe(remoteNotify)
        // Just wait for the first notification on the `REMOTE_NOTIFY` interface.
        observation.first(predicate)
      }
    }
  }
}
