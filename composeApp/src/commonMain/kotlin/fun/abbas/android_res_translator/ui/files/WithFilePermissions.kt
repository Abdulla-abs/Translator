package `fun`.abbas.android_res_translator.ui.files

import androidx.compose.runtime.Composable

/**
 * Platform-specific wrapper that ensures any required file-access permissions are granted
 * before [content] is shown. On Android this triggers the runtime storage-permission flow;
 * on other platforms it is a transparent pass-through.
 */
@Composable
expect fun WithFilePermissions(content: @Composable () -> Unit)
