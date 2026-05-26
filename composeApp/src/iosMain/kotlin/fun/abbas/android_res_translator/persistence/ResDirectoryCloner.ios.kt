package `fun`.abbas.android_res_translator.persistence

actual fun cloneResDirectoryToWorkspace(source: String, workspacePath: String): Boolean =
    copyDirectoryRecursively(source, workspacePath)
