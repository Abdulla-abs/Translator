package `fun`.abbas.android_res_translator.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

@Composable
actual fun rememberXmlFileAccess(): XmlFileAccess = remember { JvmXmlFileAccess() }

@Composable
actual fun rememberDirectoryPicker(onResult: (String?) -> Unit): () -> Unit {
    return remember {
        {
            SwingUtilities.invokeLater {
                val chooser = JFileChooser()
                chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                chooser.dialogTitle = "Select Workspace Root"
                val result = chooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    onResult(chooser.selectedFile.absolutePath)
                } else {
                    onResult(null)
                }
            }
        }
    }
}

private class JvmXmlFileAccess : XmlFileAccess {
    override fun launchPickXml(onResult: (Result<String>) -> Unit) {
        Thread {
            val result =
                try {
                    val dialog = FileDialog(null as Frame?, "选择 strings.xml", FileDialog.LOAD)
                    dialog.setFilenameFilter { _, name -> name.endsWith(".xml", ignoreCase = true) }
                    dialog.isVisible = true
                    val dir = dialog.directory
                    val fileName = dialog.file
                    if (dir == null || fileName == null) {
                        Result.failure(Exception("已取消"))
                    } else {
                        Result.success(File(dir, fileName).readText(Charsets.UTF_8))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            SwingUtilities.invokeLater { onResult(result) }
        }.start()
    }

    override fun launchSaveXml(
        content: String,
        suggestedName: String,
        onDone: (Boolean) -> Unit,
    ) {
        Thread {
            val ok =
                try {
                    val dialog = FileDialog(null as Frame?, "保存 strings.xml", FileDialog.SAVE)
                    dialog.file = suggestedName
                    dialog.isVisible = true
                    val dir = dialog.directory
                    val fileName = dialog.file
                    if (dir == null || fileName == null) {
                        false
                    } else {
                        File(dir, fileName).writeText(content, Charsets.UTF_8)
                        true
                    }
                } catch (_: Exception) {
                    false
                }
            SwingUtilities.invokeLater { onDone(ok) }
        }.start()
    }

    override fun launchSaveSpreadsheet(
        bytes: ByteArray,
        suggestedName: String,
        onDone: (Boolean) -> Unit,
    ) {
        Thread {
            val ok =
                try {
                    val dialog = FileDialog(null as Frame?, "保存 Excel", FileDialog.SAVE)
                    dialog.file =
                        if (suggestedName.endsWith(".xlsx", ignoreCase = true)) {
                            suggestedName
                        } else {
                            "$suggestedName.xlsx"
                        }
                    dialog.isVisible = true
                    val dir = dialog.directory
                    val fileName = dialog.file
                    if (dir == null || fileName == null) {
                        false
                    } else {
                        val name =
                            if (fileName.endsWith(".xlsx", ignoreCase = true)) {
                                fileName
                            } else {
                                "$fileName.xlsx"
                            }
                        File(dir, name).writeBytes(bytes)
                        true
                    }
                } catch (_: Exception) {
                    false
                }
            SwingUtilities.invokeLater { onDone(ok) }
        }.start()
    }

    override fun launchPickSpreadsheet(onResult: (Result<ByteArray>) -> Unit) {
        Thread {
            val result =
                try {
                    val dialog = FileDialog(null as Frame?, "选择 Excel", FileDialog.LOAD)
                    dialog.setFilenameFilter { _, name ->
                        name.endsWith(".xlsx", ignoreCase = true) ||
                            name.endsWith(".xls", ignoreCase = true)
                    }
                    dialog.isVisible = true
                    val dir = dialog.directory
                    val fileName = dialog.file
                    if (dir == null || fileName == null) {
                        Result.failure(Exception("已取消"))
                    } else {
                        Result.success(File(dir, fileName).readBytes())
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            SwingUtilities.invokeLater { onResult(result) }
        }.start()
    }
}
