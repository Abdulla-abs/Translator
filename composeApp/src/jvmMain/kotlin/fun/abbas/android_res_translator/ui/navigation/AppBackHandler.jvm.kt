package `fun`.abbas.android_res_translator.ui.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun AppBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // Desktop 无系统返回键；由界面内返回按钮处理。
}
