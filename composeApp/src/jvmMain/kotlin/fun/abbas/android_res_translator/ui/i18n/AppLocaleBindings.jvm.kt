package `fun`.abbas.android_res_translator.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

actual object PlatformAppLocale {
    private var defaultLocale: Locale? = null
    private val LocalPlatformAppLocale = staticCompositionLocalOf { Locale.getDefault().toLanguageTag() }

    actual val currentTag: String
        @Composable get() = LocalPlatformAppLocale.current

    @Composable
    actual infix fun provides(languageTag: String): ProvidedValue<*> {
        if (defaultLocale == null) {
            defaultLocale = Locale.getDefault()
        }
        val newLocale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(newLocale)
        return LocalPlatformAppLocale provides newLocale.toLanguageTag()
    }
}
