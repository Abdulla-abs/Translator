package `fun`.abbas.android_res_translator.ui.files

import androidx.compose.runtime.Composable

@Composable
actual fun WithFilePermissions(content: @Composable () -> Unit) {
    WithStoragePermission(content)
}
