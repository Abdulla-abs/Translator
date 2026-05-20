package `fun`.abbas.android_res_translator.ui.navigation

import androidx.compose.runtime.Composable

/** 拦截系统返回键/手势，优先于 Activity 默认退出行为。 */
@Composable
expect fun AppBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
)
