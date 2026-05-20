package `fun`.abbas.android_res_translator.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.net.URI

@Composable
actual fun rememberOpenExternalUrlHandler(): (String) -> Unit =
    remember {
        { url ->
            runCatching {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI(url))
                }
            }
        }
    }
