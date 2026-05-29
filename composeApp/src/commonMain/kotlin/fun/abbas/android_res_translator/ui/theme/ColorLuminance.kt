package `fun`.abbas.android_res_translator.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.pow

internal fun Color.relativeLuminance(): Float {
    fun channel(c: Float): Float =
        if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
    return 0.2126f * channel(red) + 0.7152f * channel(green) + 0.0722f * channel(blue)
}

/** 浅色背景使用深色状态栏图标；深色背景使用浅色图标。 */
fun ColorScheme.usesDarkStatusBarIcons(): Boolean = background.relativeLuminance() > 0.5f
