package com.rahulrav.camera.scan

sealed interface PairState {
    object NotPaired : PairState
    
    object Pairing : PairState

    object Paired : PairState

    data class Failed(val exception: Exception) : PairState
}
