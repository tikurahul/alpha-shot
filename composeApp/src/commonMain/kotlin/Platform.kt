import com.juul.kable.Advertisement

interface Platform {
    fun bleFilter(): (advertisement: Advertisement) -> Boolean
}

expect fun getPlatform(): Platform
