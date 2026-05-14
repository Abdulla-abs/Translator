package `fun`.abbas.android_res_translator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform