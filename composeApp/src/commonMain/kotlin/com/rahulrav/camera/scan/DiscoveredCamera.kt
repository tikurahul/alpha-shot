package com.rahulrav.camera.scan

import com.juul.kable.Advertisement
import com.juul.kable.Identifier
import kotlinx.serialization.Serializable

@Serializable
data class DiscoveredCamera(
    val name: String,
    val advertisement: Advertisement,
    val modelCode: String,
    val modelInfo: SupportedAlphaCamera,
    val pairState: PairState,
) {
    val identifier: Identifier
        get() = advertisement.identifier
}
