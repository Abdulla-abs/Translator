package `fun`.abbas.android_res_translator.persistence

/**
 * 将用户选择的 res 根目录克隆到应用沙箱 [workspacePath]。
 * [source]：JVM 为绝对路径；Android 为 `content://` 树 URI 或文件路径。
 */
expect fun cloneResDirectoryToWorkspace(source: String, workspacePath: String): Boolean
