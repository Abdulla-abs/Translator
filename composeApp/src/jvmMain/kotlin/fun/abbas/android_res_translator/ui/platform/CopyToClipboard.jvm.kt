package `fun`.abbas.android_res_translator.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
actual fun rememberCopyToClipboardHandler(): (String) -> Unit =
    remember {
        { text ->
            runCatching {
                val selection = StringSelection(text)
                Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
            }
        }
    }
