package com.rahulrav.camera.scan

import SonyCameraControl
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.util.fastAny
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.juul.kable.Advertisement
import getPlatform
import kotlinx.coroutines.*

interface CameraScannerViewModel {
    val state: State<ScanState>

    fun doScan()

    fun stopScan()

    fun pairAndConnect(camera: DiscoveredCamera)
}

class CameraScanViewModelImpl(
    private val cameraControl: SonyCameraControl = SonyCameraControl(getPlatform()),
    private val bleDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel(), CameraScannerViewModel {

    private val _state = mutableStateOf<ScanState>(ScanState.Idle)
    override val state: State<ScanState> = _state

    private var scanJob: Job? = null

    override fun doScan() {
        if (scanJob != null) {
            Logger.e("Scan job is not null while not scanning, how?")
            return
        }
        if (_state.value is ScanState.Scanning) {
            Logger.e("Scan state is Scanning while not scanning, how?")
            return
        }

        _state.value = ScanState.Scanning()
        scanJob =
            viewModelScope.launch(bleDispatcher) {
                cameraControl.scan().collect { onCameraDiscovered(it) }
            }
        Logger.i("Scan started")
    }

    override fun stopScan() {
        val currentState = _state.value
        if (currentState !is ScanState.Scanning) {
            Logger.e("Can't stop scanning because no scan is in progress")
            return
        }

        val currentJob = scanJob
        if (currentJob == null) {
            Logger.e("Scan job is null while state is Scanning, how?")
            return
        }

        currentJob.cancel("Stop scanning")
        scanJob = null

        _state.value =
            if (currentState.cameras.isEmpty()) {
                ScanState.Idle
            } else {
                ScanState.IdleWithResults(currentState.cameras)
            }

        Logger.i("Scan stopped")
    }

    private fun onCameraDiscovered(advertisement: Advertisement) {
        val scanResults =
            when (val currentState = _state.value) {
                is ScanState.Scanning -> currentState
                else -> {
                    Logger.i("First camera discovered, moving to ScanResults...")
                    ScanState.Scanning().also { newState -> _state.value = newState }
                }
            }

        Logger.i("Camera discovered: ${advertisement.name} (${advertisement.identifier}")
        val manufacturerData =
            advertisement.manufacturerData(0x2D01) ?: advertisement.manufacturerData(0x012D)
        if (manufacturerData == null) {
            Logger.w("Ignoring discovered camera, missing manufacturer data.\n$advertisement")
            return
        }

        val modelCode = manufacturerData.decodeToString(4, 6)
        val modelInfo = SupportedAlphaCamera.forCodeOrNull(modelCode)
        if (modelInfo == null) {
            Logger.i("Ignoring discovered camera, unsupported model: $modelCode\n$advertisement")
            return
        }

        if (scanResults.cameras.fastAny { it.identifier == advertisement.identifier }) {
            Logger.d("Ignoring already discovered camera: ${advertisement.identifier}")
            return
        }

        val discoveredCamera =
            DiscoveredCamera(
                name = advertisement.name ?: advertisement.peripheralName ?: "Unknown",
                advertisement = advertisement,
                modelCode = modelCode,
                modelInfo = modelInfo,
                pairState = PairState.NotPaired,
            )
        scanResults.cameras += discoveredCamera

        scanResults.cameras.sortBy { it.name }
    }

    override fun pairAndConnect(camera: DiscoveredCamera) {
        viewModelScope.launch(bleDispatcher) { cameraControl.connect(camera) }
    }
}

sealed interface ScanState {
    data object Idle : ScanState

    data class Scanning(
        override val cameras: SnapshotStateList<DiscoveredCamera> = mutableStateListOf(),
    ) : ScanState, StateWithResults

    data class IdleWithResults(
        override val cameras: SnapshotStateList<DiscoveredCamera>,
    ) : ScanState, StateWithResults

    interface StateWithResults {
        val cameras: List<DiscoveredCamera>
    }
}
