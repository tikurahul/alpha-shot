package com.rahulrav.camera.scan

import alpha_shot.composeapp.generated.resources.*
import alpha_shot.composeapp.generated.resources.Res
import alpha_shot.composeapp.generated.resources.ilce7m3
import alpha_shot.composeapp.generated.resources.ilce7m4
import alpha_shot.composeapp.generated.resources.ilce7rm5
import org.jetbrains.compose.resources.DrawableResource

enum class SupportedAlphaCamera(
    val modelCode: String,
    val modelName: String,
    val modelNumber: String,
    val picture: DrawableResource,
) {
    ILCE_7M3(
        modelCode = "TODO",
        modelNumber = "ILCE-7M3",
        modelName = "Sony Alpha 7 III",
        picture = Res.drawable.ilce7m3,
    ),
    ILCE_7M4(
        modelCode = "U1",
        modelNumber = "ILCE-7M4",
        modelName = "Sony Alpha 7 IV",
        picture = Res.drawable.ilce7m4,
    ),
    ILCE_7RM5(
        modelCode = "E1",
        modelNumber = "ILCE-7RM5",
        modelName = "Sony Alpha 7R V",
        picture = Res.drawable.ilce7rm5,
    ),
    ILCE_6600(
        modelCode = "TODO",
        modelNumber = "ILCE-6600",
        modelName = "Sony Alpha 6600",
        picture = Res.drawable.ilce6600,
    );

    companion object {
        private val modelsByCode = entries.associateBy { it.modelCode }

        fun forCodeOrNull(modelCode: String) = modelsByCode[modelCode]
    }
}
