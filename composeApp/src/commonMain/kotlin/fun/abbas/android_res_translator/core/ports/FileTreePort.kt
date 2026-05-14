package `fun`.abbas.android_res_translator.core.ports

/**
 * 可替换的文件枚举与读写（阶段二可接真实 FS；核心不依赖 `java.io.File`）。
 */
data class FileNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
)

interface FileTreePort {
    suspend fun listChildren(dir: String): List<FileNode>

    suspend fun readUtf8(path: String): String

    suspend fun writeUtf8(
        path: String,
        content: String,
    )
}
