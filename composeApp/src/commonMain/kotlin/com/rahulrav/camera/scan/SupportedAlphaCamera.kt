package com.rahulrav.camera.scan

enum class SupportedAlphaCamera(
    val modelCode: String,
    val modelName: String,
    val modelNumber: String
) {
  ILCE_7M4(
      modelCode = "TODO",
      modelNumber = "ILCE-7M4",
      modelName = "Sony Alpha 7 IV",
  ),
  ILCE_7RM5(
      modelCode = "E1",
      modelNumber = "ILCE-7RM5",
      modelName = "Sony Alpha 7R V",
  ),
  ILCE_6600(
      modelCode = "TODO",
      modelNumber = "ILCE-6600",
      modelName = "Sony Alpha 6600",
  );

  companion object {
    val modelsByCode = entries.associateBy { it.modelCode }

    fun forCodeOrNull(modelCode: String) = modelsByCode[modelCode]
  }
}
