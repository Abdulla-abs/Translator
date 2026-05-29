package `fun`.abbas.android_res_translator.ui.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/** 按 [colorScheme] 同步平台系统栏前景色（Android 状态栏/导航栏图标）。 */
@Composable
expect fun ApplySystemBarsAppearance(colorScheme: ColorScheme)
