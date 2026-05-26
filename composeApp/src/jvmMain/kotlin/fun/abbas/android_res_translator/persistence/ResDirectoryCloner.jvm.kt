package `fun`.abbas.android_res_translator.persistence

import java.io.File

actual fun cloneResDirectoryToWorkspace(source: String, workspacePath: String): Boolean {
    val src = File(source)
    if (!src.exists() || !src.isDirectory) return false
    val dest = File(workspacePath)
    if (dest.exists()) deletePathRecursively(workspacePath)
    return copyDirectoryRecursivelyJvm(src, dest)
}
