package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.AndroidSettingsContext
import java.io.File

actual fun appTranslationProjectsRoot(): String =
    File(AndroidSettingsContext.require().filesDir, "translation-projects").absolutePath

actual fun appCompareProjectsRoot(): String =
    File(AndroidSettingsContext.require().filesDir, "compare-projects").absolutePath

actual fun appResMultiProjectsRoot(): String =
    File(AndroidSettingsContext.require().filesDir, "res-multi-projects").absolutePath

actual fun readTextFile(path: String): String = File(path).readText(Charsets.UTF_8)

actual fun writeTextFileAtomic(path: String, content: String) {
    val target = File(path)
    target.parentFile?.mkdirs()
    val tmp = File("$path.tmp")
    tmp.writeText(content, Charsets.UTF_8)
    if (target.exists()) target.delete()
    tmp.renameTo(target)
}

actual fun ensureDirectory(path: String) {
    File(path).mkdirs()
}

actual fun fileExists(path: String): Boolean = File(path).exists()

actual fun deletePathRecursively(path: String): Boolean {
    val root = File(path)
    if (!root.exists()) return true
    return root.walkBottomUp().fold(true) { ok, file -> file.delete() && ok }
}

actual fun listFileNamesInDirectory(directoryPath: String): List<String> {
    val dir = File(directoryPath)
    if (!dir.isDirectory) return emptyList()
    return dir.listFiles()?.map { it.name } ?: emptyList()
}

actual fun isDirectoryPath(path: String): Boolean = File(path).isDirectory

actual fun copyDirectoryRecursively(sourcePath: String, destinationPath: String): Boolean {
    if (sourcePath.startsWith("content://")) return false
    return copyLocalDirectoryRecursively(File(sourcePath), File(destinationPath))
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
