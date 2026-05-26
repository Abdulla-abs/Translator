package `fun`.abbas.android_res_translator.persistence

import java.io.File

/** 单元测试可注入临时目录。 */
internal var translationProjectsRootOverride: String? = null
internal var compareProjectsRootOverride: String? = null
internal var resMultiProjectsRootOverride: String? = null

actual fun appTranslationProjectsRoot(): String =
    translationProjectsRootOverride
        ?: File(
            System.getProperty("user.home"),
            ".android_res_translator${File.separator}translation-projects",
        ).absolutePath

actual fun appCompareProjectsRoot(): String =
    compareProjectsRootOverride
        ?: File(
            System.getProperty("user.home"),
            ".android_res_translator${File.separator}compare-projects",
        ).absolutePath

actual fun appResMultiProjectsRoot(): String =
    resMultiProjectsRootOverride
        ?: File(
            System.getProperty("user.home"),
            ".android_res_translator${File.separator}res-multi-projects",
        ).absolutePath

actual fun readTextFile(path: String): String = File(path).readText(Charsets.UTF_8)

actual fun writeTextFileAtomic(path: String, content: String) {
    val target = File(path)
    target.parentFile?.mkdirs()
    val tmp = File("$path.tmp")
    tmp.writeText(content, Charsets.UTF_8)
    if (target.exists()) target.delete()
    check(tmp.renameTo(target)) { "无法写入 $path" }
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

actual fun copyDirectoryRecursively(sourcePath: String, destinationPath: String): Boolean =
    copyDirectoryRecursivelyJvm(File(sourcePath), File(destinationPath))

internal fun copyDirectoryRecursivelyJvm(source: File, destination: File): Boolean {
    if (!source.exists()) return false
    if (source.isFile) {
        destination.parentFile?.mkdirs()
        source.copyTo(destination, overwrite = true)
        return true
    }
    if (!destination.exists() && !destination.mkdirs()) return false
    val children = source.listFiles() ?: return false
    return children.fold(true) { ok, child ->
        val target = File(destination, child.name)
        copyDirectoryRecursivelyJvm(child, target) && ok
    }
}
