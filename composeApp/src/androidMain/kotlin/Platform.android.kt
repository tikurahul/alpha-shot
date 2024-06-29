import com.juul.kable.Advertisement
import com.juul.kable.AndroidAdvertisement

actual fun getPlatform(): Platform {
    return object : Platform {
        override fun bleFilter(): (advertisement: Advertisement) -> Boolean {
            return {
                val androidAdvertisement = it as AndroidAdvertisement
                // We only want to connect to devices that we have bonded with. This way,
                // the pairing process is decoupled from the app. To pair the device, just use
                // the traditional Android Bluetooth Settings panel, and pair it with the camera.
                // Additional Note: If you ever delete the paired device on the Sony Camera, that
                // does not automatically clear the bonded state on the Android side of things.
                // Make sure you forget the Camera from the Bluetooth Settings panel.
                androidAdvertisement.bondState == AndroidAdvertisement.BondState.Bonded
            }
        }
    }
}
