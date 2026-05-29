package `fun`.abbas.android_res_translator.ui.platform

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import `fun`.abbas.android_res_translator.ui.theme.usesDarkStatusBarIcons

@Composable
actual fun ApplySystemBarsAppearance(colorScheme: ColorScheme) {
    val view = LocalView.current
    val useDarkIcons = colorScheme.usesDarkStatusBarIcons()
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = useDarkIcons
                isAppearanceLightNavigationBars = useDarkIcons
            }
        }
    }
}
