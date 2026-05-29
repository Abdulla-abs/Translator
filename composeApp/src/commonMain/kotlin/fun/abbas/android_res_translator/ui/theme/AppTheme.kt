package `fun`.abbas.android_res_translator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import `fun`.abbas.android_res_translator.ui.platform.ApplySystemBarsAppearance
import `fun`.abbas.android_res_translator.ui.settings.AppAppearance

/**
 * Material3 主题：配色由 [appearance] 决定；排版、圆角、间距恒为 [AppTypography]、[AppShapes]、[AppSpacing]。
 */
@Composable
fun AppTheme(
    appearance: AppAppearance = AppAppearance.Classic,
    content: @Composable () -> Unit,
) {
    val colorScheme = appearance.resolveColorScheme()
    CompositionLocalProvider(LocalCodeTextStyle provides AppCodeTextStyle) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
        ) {
            ApplySystemBarsAppearance(colorScheme)
            content()
        }
    }
}
