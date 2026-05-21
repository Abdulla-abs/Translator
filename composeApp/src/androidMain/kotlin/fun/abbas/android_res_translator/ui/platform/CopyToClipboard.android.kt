package `fun`.abbas.android_res_translator.ui.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberCopyToClipboardHandler(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { text ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("translation", text))
        }
    }
}
