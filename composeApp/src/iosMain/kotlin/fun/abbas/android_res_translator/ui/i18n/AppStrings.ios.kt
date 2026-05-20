package `fun`.abbas.android_res_translator.ui.i18n

import platform.Foundation.NSUserDefaults

private const val LANG_KEY = "AppleLanguages"

actual suspend fun <T> withPlatformLocale(
    languageTag: String,
    block: suspend () -> T,
): T {
    val defaults = NSUserDefaults.standardUserDefaults
    val previous = defaults.objectForKey(LANG_KEY)
    defaults.setObject(listOf(languageTag), LANG_KEY)
    return try {
        block()
    } finally {
        if (previous != null) {
            defaults.setObject(previous, LANG_KEY)
        } else {
            defaults.removeObjectForKey(LANG_KEY)
        }
    }
}
