package `fun`.abbas.android_res_translator.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberXmlFileAccess(): XmlFileAccess =
    remember {
        object : XmlFileAccess {
            override fun launchPickXml(onResult: (Result<String>) -> Unit) {
                onResult(Result.failure(Exception("iOS 端暂请直接粘贴 XML 内容")))
            }

            override fun launchSaveXml(
                content: String,
                suggestedName: String,
                onDone: (Boolean) -> Unit,
            ) {
                onDone(false)
            }
        }
    }

@Composable
actual fun rememberDirectoryPicker(onResult: (String?) -> Unit): () -> Unit {
    return remember {
        {
            onResult(null)
        }
    }
}
