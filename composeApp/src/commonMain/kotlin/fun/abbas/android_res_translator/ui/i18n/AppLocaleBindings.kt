package `fun`.abbas.android_res_translator.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.key

val LocalAppLocale = compositionLocalOf { AppLocale.En }

expect object PlatformAppLocale {
    val currentTag: String
        @Composable get

    @Composable
    infix fun provides(languageTag: String): ProvidedValue<*>
}

@Composable
fun ProvideAppLocale(
    locale: AppLocale,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        PlatformAppLocale provides locale.tag,
        LocalAppLocale provides locale,
    ) {
        key(locale) {
            content()
        }
    }
}
