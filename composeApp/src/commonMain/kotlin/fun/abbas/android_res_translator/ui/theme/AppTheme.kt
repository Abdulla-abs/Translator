package `fun`.abbas.android_res_translator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * 应用 [DESIGN.md](../../../../../../../DESIGN.md) 设计系统：深色 Material3 主题 + 代码字体 Local。
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalCodeTextStyle provides AppCodeTextStyle) {
        MaterialTheme(
            colorScheme = AppDarkColorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
