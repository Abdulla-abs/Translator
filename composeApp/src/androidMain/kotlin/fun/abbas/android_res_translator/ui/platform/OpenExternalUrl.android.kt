package `fun`.abbas.android_res_translator.ui.platform

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberOpenExternalUrlHandler(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }
}
