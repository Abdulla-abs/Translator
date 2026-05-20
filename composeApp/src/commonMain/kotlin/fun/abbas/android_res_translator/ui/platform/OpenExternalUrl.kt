package `fun`.abbas.android_res_translator.ui.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberOpenExternalUrlHandler(): (String) -> Unit
