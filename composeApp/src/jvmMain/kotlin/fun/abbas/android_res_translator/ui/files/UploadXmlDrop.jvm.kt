package `fun`.abbas.android_res_translator.ui.files

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText

private fun pathFromDropUri(uriString: String): Path? =
    runCatching {
        when {
            uriString.startsWith("file:", ignoreCase = true) -> URI(uriString).let { Paths.get(it) }
            else -> Paths.get(uriString)
        }
    }.getOrNull()

/**
 * 必须在 [DragAndDropTarget.onDrop] 返回前同步读完；拖放会话结束后读数据会触发
 * InvalidDnDOperationException: No drop current。
 */
@OptIn(ExperimentalComposeUiApi::class)
private fun readDroppedXmlFilesSync(event: DragAndDropEvent): List<DroppedXmlFile> {
    val uriStrings =
        when (val data = event.dragData()) {
            is DragData.FilesList -> data.readFiles()
            else -> emptyList()
        }

    val fromUris =
        uriStrings.mapNotNull { uriString ->
            runCatching {
                val path = pathFromDropUri(uriString) ?: return@mapNotNull null
                if (!path.isRegularFile() || !path.name.endsWith(".xml", ignoreCase = true)) {
                    return@mapNotNull null
                }
                DroppedXmlFile(
                    content = path.readText(Charsets.UTF_8),
                    displayName = path.name,
                )
            }.getOrNull()
        }
    if (fromUris.isNotEmpty()) return fromUris

    return runCatching {
        val transferable = event.awtTransferable
        if (!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return emptyList()
        }
        @Suppress("UNCHECKED_CAST")
        val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
        files
            .filter { it.isFile && it.name.endsWith(".xml", ignoreCase = true) }
            .map { file ->
                DroppedXmlFile(
                    content = file.readText(Charsets.UTF_8),
                    displayName = file.name,
                )
            }
    }.getOrElse { emptyList() }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun rememberUploadXmlDropModifier(onDrop: (List<DroppedXmlFile>) -> Unit): Modifier {
    val onDropState by rememberUpdatedState(onDrop)
    var dragHighlight by remember { mutableStateOf(false) }

    val target =
        remember {
            object : DragAndDropTarget {
                override fun onEntered(event: DragAndDropEvent) {
                    dragHighlight = true
                }

                override fun onExited(event: DragAndDropEvent) {
                    dragHighlight = false
                }

                override fun onEnded(event: DragAndDropEvent) {
                    dragHighlight = false
                }

                override fun onDrop(event: DragAndDropEvent): Boolean {
                    dragHighlight = false
                    val dropped = readDroppedXmlFilesSync(event)
                    if (dropped.isEmpty()) return false
                    onDropState(dropped)
                    return true
                }
            }
        }

    return Modifier
        .then(
            if (dragHighlight) {
                Modifier.drawBehind {
                    drawRoundRect(
                        color = Color(0xFF00E5FF).copy(alpha = 0.35f),
                        style = Stroke(width = 3f),
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    )
                }
            } else {
                Modifier
            },
        )
        .dragAndDropTarget(
            shouldStartDragAndDrop = { event -> event.dragData() is DragData.FilesList },
            target = target,
        )
}
