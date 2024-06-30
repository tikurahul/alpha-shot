package com.rahulrav.camera.scan

import SonyCameraControl
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.juul.kable.Advertisement
import getPlatform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

interface CameraScanViewModel {
    val state: State<ScanState>
    
    fun doScan()

    fun pair(camera: DiscoveredCamera)
}

class CameraScanViewModelImpl(
    private val cameraControl: SonyCameraControl = SonyCameraControl(getPlatform()),
    private val scanDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel(), CameraScanViewModel {

    private val _state = mutableStateOf<ScanState>(ScanState.StartingScan)
    override val state: State<ScanState> = _state

    override fun doScan() {
        viewModelScope.launch(scanDispatcher) {
            cameraControl.scan().collect { onCameraDiscovered(it) }
        }
    }

    private fun onCameraDiscovered(advertisement: Advertisement) {
        val scanResults =
            when (val currentState = _state.value) {
                is ScanState.ScanResults -> currentState
                else -> {
                    Logger.i("First camera discovered, moving to ScanResults...")
                    val scanResults = ScanState.ScanResults()
                    _state.value = scanResults
                    scanResults
                }
            }

        Logger.i("Camera discovered: ${advertisement.name} (${advertisement.identifier}")
        val manufacturerData = advertisement.manufacturerData(0x2D01)
        if (manufacturerData == null) {
            Logger.w("Ignoring discovered camera, missing manufacturer data.")
            return
        }

        val modelCode = manufacturerData.decodeToString(6, 7)
        val modelInfo = SupportedAlphaCamera.forCodeOrNull(modelCode)
        if (modelInfo == null) {
            Logger.i("Ignoring discovered camera, unsupported model.")
            return
        }

        val discoveredCamera =
            DiscoveredCamera(
                name = advertisement.name ?: advertisement.peripheralName ?: "Unknown",
                identifier = advertisement.identifier,
                modelCode = modelCode,
                modelInfo = modelInfo,
            )
        scanResults.cameras += discoveredCamera

        scanResults.cameras.sortBy { it.name }
    }

    override fun pair(camera: DiscoveredCamera) {
        TODO()
    }
}

sealed interface ScanState {
    object StartingScan : ScanState

    data class ScanResults(
        val cameras: SnapshotStateList<DiscoveredCamera> = mutableStateListOf(),
    ) : ScanState
}
