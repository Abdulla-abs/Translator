package `fun`.abbas.android_res_translator.ui

import androidx.compose.runtime.Composable

/** 打开 / 保存 UTF-8 文本（如 strings.xml）；各平台自行实现。 */
interface XmlFileAccess {
    fun launchPickXml(onResult: (Result<String>) -> Unit)

    fun launchSaveXml(
        content: String,
        suggestedName: String,
        onDone: (Boolean) -> Unit,
    )

    fun launchSaveSpreadsheet(
        bytes: ByteArray,
        suggestedName: String,
        onDone: (Boolean) -> Unit,
    )
}

@Composable
expect fun rememberXmlFileAccess(): XmlFileAccess

@Composable
expect fun rememberDirectoryPicker(onResult: (String?) -> Unit): () -> Unit
