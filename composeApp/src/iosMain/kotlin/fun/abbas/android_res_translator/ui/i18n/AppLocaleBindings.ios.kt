package `fun`.abbas.android_res_translator.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

actual object PlatformAppLocale {
    private const val LANG_KEY = "AppleLanguages"
    private val defaultTag = (preferredLanguages.firstOrNull() as? String) ?: "en"
    private val LocalPlatformAppLocale = staticCompositionLocalOf { defaultTag }

    actual val currentTag: String
        @Composable get() = LocalPlatformAppLocale.current

    @Composable
    actual infix fun provides(languageTag: String): ProvidedValue<*> {
        val newTag = languageTag.ifBlank { defaultTag }
        NSUserDefaults.standardUserDefaults.setObject(listOf(newTag), LANG_KEY)
        return LocalPlatformAppLocale provides newTag
    }
}
