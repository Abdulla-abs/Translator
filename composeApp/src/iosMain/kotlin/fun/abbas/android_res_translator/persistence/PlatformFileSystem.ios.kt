@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package `fun`.abbas.android_res_translator.persistence

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

actual fun appTranslationProjectsRoot(): String {
    val urls =
        NSFileManager.defaultManager.URLsForDirectory(
            directory = platform.Foundation.NSApplicationSupportDirectory,
            inDomains = NSUserDomainMask,
        )
    val base = (urls.firstOrNull() as? platform.Foundation.NSURL)?.path ?: error("无法解析应用目录")
    return "$base/translation-projects"
}

actual fun appCompareProjectsRoot(): String {
    val urls =
        NSFileManager.defaultManager.URLsForDirectory(
            directory = platform.Foundation.NSApplicationSupportDirectory,
            inDomains = NSUserDomainMask,
        )
    val base = (urls.firstOrNull() as? platform.Foundation.NSURL)?.path ?: error("无法解析应用目录")
    return "$base/compare-projects"
}

actual fun appResMultiProjectsRoot(): String {
    val urls =
        NSFileManager.defaultManager.URLsForDirectory(
            directory = platform.Foundation.NSApplicationSupportDirectory,
            inDomains = NSUserDomainMask,
        )
    val base = (urls.firstOrNull() as? platform.Foundation.NSURL)?.path ?: error("无法解析应用目录")
    return "$base/res-multi-projects"
}

actual fun readTextFile(path: String): String {
    val content =
        NSString.stringWithContentsOfFile(
            path = path,
            encoding = NSUTF8StringEncoding,
            error = null,
        ) as String?
    return content ?: error("无法读取 $path")
}

actual fun writeTextFileAtomic(path: String, content: String) {
    val parent = path.substringBeforeLast('/', missingDelimiterValue = "")
    if (parent.isNotEmpty()) ensureDirectory(parent)
    val tmp = "$path.tmp"
    (content as NSString).writeToFile(
        path = tmp,
        atomically = true,
        encoding = NSUTF8StringEncoding,
        error = null,
    )
    NSFileManager.defaultManager.removeItemAtPath(path, null)
    NSFileManager.defaultManager.moveItemAtPath(tmp, path, null)
}

actual fun ensureDirectory(path: String) {
    NSFileManager.defaultManager.createDirectoryAtPath(
        path = path,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
}

actual fun fileExists(path: String): Boolean =
    NSFileManager.defaultManager.fileExistsAtPath(path)

actual fun deletePathRecursively(path: String): Boolean {
    if (!fileExists(path)) return true
    NSFileManager.defaultManager.removeItemAtPath(path, error = null)
    return !fileExists(path)
}

actual fun listFileNamesInDirectory(directoryPath: String): List<String> {
    if (!isDirectoryPath(directoryPath)) return emptyList()
    val names =
        NSFileManager.defaultManager.contentsOfDirectoryAtPath(
            directoryPath,
            error = null,
        ) as? List<*>
    return names?.mapNotNull { it as? String } ?: emptyList()
}

actual fun isDirectoryPath(path: String): Boolean {
    if (!fileExists(path)) return false
    val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(path, error = null)
    val type = attrs?.get(platform.Foundation.NSFileType) as? String
    return type == platform.Foundation.NSFileTypeDirectory
}

actual fun copyDirectoryRecursively(sourcePath: String, destinationPath: String): Boolean {
    if (!fileExists(sourcePath) || !isDirectoryPath(sourcePath)) return false
    if (fileExists(destinationPath)) deletePathRecursively(destinationPath)
    return NSFileManager.defaultManager.copyItemAtPath(sourcePath, destinationPath, error = null)
}
