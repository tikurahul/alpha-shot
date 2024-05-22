import com.juul.kable.Advertisement
import com.juul.kable.AndroidAdvertisement

actual fun getPlatform(): Platform {
    return object : Platform {
        override fun bleFilter(): (advertisement: Advertisement) -> Boolean {
            return {
                val androidAdvertisement = it as AndroidAdvertisement
                androidAdvertisement.bondState == AndroidAdvertisement.BondState.Bonded
            }
        }
    }
}
