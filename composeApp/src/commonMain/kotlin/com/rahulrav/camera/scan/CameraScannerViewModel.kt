package com.rahulrav.camera.scan

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.util.fastAny
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.juul.kable.Advertisement
import com.rahulrav.camera.SonyCameraControl
import com.rahulrav.camera.scan.CameraScannerViewModel.Companion.TAG
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

interface CameraScannerViewModel {
    val state: State<ScanState>

    fun scan()

    fun stopScanning()

    fun pairAndConnect(camera: DiscoveredCamera)

    companion object {
        const val TAG = "CameraScannerViewModel"
    }
}

class CameraScanViewModelImpl(
    private val cameraControl: SonyCameraControl = SonyCameraControl(),
    private val bleDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel(), CameraScannerViewModel {

    private val _state = mutableStateOf<ScanState>(ScanState.Idle)
    override val state: State<ScanState> = _state

    // The job responsible for scanning a peripheral.
    private var job: Job? = null

    override fun scan() {
        if (job != null) {
            Logger.e(TAG) { "Scan job is not null while not scanning, how?" }
            return
        }
        if (_state.value is ScanState.Scanning) {
            Logger.e(TAG) { "Scan state is Scanning while not scanning, how?" }
            return
        }

        _state.value = ScanState.Scanning()
        job =
            viewModelScope.launch(bleDispatcher) {
                cameraControl.scan().collect { onCameraDiscovered(it) }
            }
        Logger.i(TAG) { "Scan started" }
    }

    override fun stopScanning() {
        val currentState = _state.value
        if (currentState !is ScanState.Scanning) {
            Logger.e(TAG) { "Can't stop scanning because no scan is in progress" }
            return
        }

        val currentJob = job
        if (currentJob == null) {
            Logger.e(TAG) { "Scan job is null while state is Scanning, how?" }
            return
        }

        currentJob.cancel("Stop scanning")
        job = null

        _state.value =
            if (currentState.cameras.isEmpty()) {
                ScanState.Idle
            } else {
                ScanState.IdleWithResults(currentState.cameras)
            }

        Logger.i(TAG) { "Scan stopped" }
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

        Logger.i(TAG) { "Camera discovered: ${advertisement.name} (${advertisement.identifier}" }
        val manufacturerData =
            advertisement.manufacturerData(0x2D01) ?: advertisement.manufacturerData(0x012D)
        if (manufacturerData == null) {
            Logger.w("Ignoring discovered camera, missing manufacturer data.\n$advertisement")
            return
        }

        val modelCode = manufacturerData.decodeToString(4, 6)
        val modelInfo = SupportedAlphaCamera.forCodeOrNull(modelCode)
        if (modelInfo == null) {
            Logger.i(TAG) { "Ignoring discovered camera, unsupported model: $modelCode\n$advertisement" }
            return
        }

        if (scanResults.cameras.fastAny { it.identifier == advertisement.identifier }) {
            Logger.d(TAG) { "Ignoring already discovered camera: ${advertisement.identifier}" }
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
