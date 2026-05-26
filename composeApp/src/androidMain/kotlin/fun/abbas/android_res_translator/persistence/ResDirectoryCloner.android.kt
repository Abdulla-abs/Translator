package `fun`.abbas.android_res_translator.persistence

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import `fun`.abbas.android_res_translator.AndroidSettingsContext
import java.io.File

actual fun cloneResDirectoryToWorkspace(source: String, workspacePath: String): Boolean {
    val context = AndroidSettingsContext.require()
    return when {
        source.startsWith("content://") -> cloneFromContentTree(context, Uri.parse(source), workspacePath)
        else -> cloneFromFilePath(source, workspacePath)
    }
}

private fun cloneFromFilePath(source: String, workspacePath: String): Boolean {
    val src = File(source)
    if (!src.exists() || !src.isDirectory) return false
    val dest = File(workspacePath)
    if (dest.exists()) deletePathRecursively(workspacePath)
    return copyLocalDirectoryRecursively(src, dest)
}

private fun copyLocalDirectoryRecursively(source: File, destination: File): Boolean {
    if (!source.exists()) return false
    if (source.isFile) {
        destination.parentFile?.mkdirs()
        source.copyTo(destination, overwrite = true)
        return true
    }
    if (!destination.exists() && !destination.mkdirs()) return false
    val children = source.listFiles() ?: return false
    return children.fold(true) { ok, child ->
        copyLocalDirectoryRecursively(child, File(destination, child.name)) && ok
    }
}

private fun cloneFromContentTree(
    context: Context,
    treeUri: Uri,
    workspacePath: String,
): Boolean {
    val root = DocumentFile.fromTreeUri(context, treeUri) ?: return false
    if (!root.isDirectory) return false
    val dest = File(workspacePath)
    if (dest.exists()) deletePathRecursively(workspacePath)
    if (!dest.mkdirs()) return false
    return copyDocumentTree(context, root, dest)
}

private fun copyDocumentTree(
    context: Context,
    source: DocumentFile,
    destDir: File,
): Boolean {
    val children = source.listFiles()
    return children.fold(true) { ok, child ->
        val name = child.name ?: return@fold false
        val target = File(destDir, name)
        val childOk =
            if (child.isDirectory) {
                target.mkdirs()
                copyDocumentTree(context, child, target)
            } else {
                target.parentFile?.mkdirs()
                runCatching {
                    context.contentResolver.openInputStream(child.uri)?.use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                }.isSuccess && target.exists()
            }
        childOk && ok
    }
}
