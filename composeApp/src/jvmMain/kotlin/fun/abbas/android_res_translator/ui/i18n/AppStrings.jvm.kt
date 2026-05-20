package `fun`.abbas.android_res_translator.ui.i18n

import java.util.Locale

actual suspend fun <T> withPlatformLocale(
    languageTag: String,
    block: suspend () -> T,
): T {
    val previous = Locale.getDefault()
    Locale.setDefault(Locale.forLanguageTag(languageTag))
    return try {
        block()
    } finally {
        Locale.setDefault(previous)
    }
}
