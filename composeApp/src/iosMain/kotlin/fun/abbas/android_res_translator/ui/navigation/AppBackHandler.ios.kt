package `fun`.abbas.android_res_translator.ui.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun AppBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // iOS 侧滑返回由系统处理；编辑器内返回走 onBack 按钮。
}
