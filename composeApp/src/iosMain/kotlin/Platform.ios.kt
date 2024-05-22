import com.juul.kable.Advertisement

actual fun getPlatform(): Platform {
    return object : Platform {
        override fun bleFilter(): (advertisement: Advertisement) -> Boolean {
            return {
                // Nothing platform specific to filter here.
                true
            }
        }
    }
}
