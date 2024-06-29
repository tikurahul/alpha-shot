import com.juul.kable.Advertisement

actual fun getPlatform(): Platform {
    return object : Platform {
        override fun bleFilter(): (advertisement: Advertisement) -> Boolean {
            return {
                // Ideally, we would have liked to filter for bonded devices like we do on Android.
                // Otherwise you end up having to scan and show the prompts over and over again.
                true
            }
        }
    }
}
