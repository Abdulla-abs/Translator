package `fun`.abbas.android_res_translator.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object PlatformAppLocale {
    private var defaultLocale: Locale? = null
    private val LocalPlatformAppLocale = staticCompositionLocalOf { Locale.getDefault().toLanguageTag() }

    actual val currentTag: String
        @Composable get() = LocalPlatformAppLocale.current

    @Composable
    actual infix fun provides(languageTag: String): ProvidedValue<*> {
        val configuration = LocalConfiguration.current
        if (defaultLocale == null) {
            defaultLocale = Locale.getDefault()
        }
        val newLocale =
            when (languageTag) {
                defaultLocale!!.toLanguageTag() -> defaultLocale!!
                else -> Locale.forLanguageTag(languageTag)
            }
        Locale.setDefault(newLocale)
        configuration.setLocale(newLocale)
        val resources = LocalContext.current.resources
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalPlatformAppLocale provides newLocale.toLanguageTag()
    }
}
