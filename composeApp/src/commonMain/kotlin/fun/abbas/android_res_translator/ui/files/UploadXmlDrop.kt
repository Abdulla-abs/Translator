package `fun`.abbas.android_res_translator.ui.files

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class DroppedXmlFile(
    val content: String,
    val displayName: String,
)

/** JVM 桌面支持从资源管理器拖入 XML；其他平台返回 [Modifier] 本身（no-op）。 */
@Composable
expect fun rememberUploadXmlDropModifier(onDrop: (List<DroppedXmlFile>) -> Unit): Modifier
