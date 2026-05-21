package `fun`.abbas.android_res_translator.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIPasteboard

@Composable
actual fun rememberCopyToClipboardHandler(): (String) -> Unit =
    remember {
        { text ->
            UIPasteboard.generalPasteboard.string = text
        }
    }
