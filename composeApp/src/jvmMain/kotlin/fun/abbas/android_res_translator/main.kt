package `fun`.abbas.android_res_translator

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AndroidResTranslator",
    ) {
        App()
    }
}