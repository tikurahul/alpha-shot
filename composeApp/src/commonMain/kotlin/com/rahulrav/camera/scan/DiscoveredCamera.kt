package com.rahulrav.camera.scan

import com.juul.kable.Identifier

data class DiscoveredCamera(
    val name: String,
    val identifier: Identifier,
    val modelCode: String,
    val modelInfo: SupportedAlphaCamera,
    val pairState: PairState,
) {
  val isSupported: Boolean = modelInfo != null
}
