package `fun`.abbas.android_res_translator.core.ports

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class JvmFileTreePort : FileTreePort {
    private var rootPath = System.getProperty("user.dir").replace('\\', '/')

    override fun getRootPath(): String = rootPath

    override fun setRootPath(path: String) {
        val f = File(path)
        if (f.exists() && f.isDirectory) {
            rootPath = f.absolutePath.replace('\\', '/')
        }
    }

    override suspend fun listChildren(dir: String): List<FileNode> = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        val targetDir = if (dir.isEmpty()) root else File(root, dir)
        if (!targetDir.exists() || !targetDir.isDirectory) {
            return@withContext emptyList()
        }
        val children = targetDir.listFiles() ?: return@withContext emptyList()
        children.map { child ->
            val relPath = child.relativeTo(root).path.replace('\\', '/')
            FileNode(
                name = child.name,
                path = relPath,
                isDirectory = child.isDirectory,
                size = if (child.isFile) child.length() else 0L
            )
        }.sortedWith(
            compareBy<FileNode> { !it.isDirectory }
                .thenBy { it.name.lowercase() }
        )
    }

    override suspend fun readUtf8(path: String): String = withContext(Dispatchers.IO) {
        val file = File(File(rootPath), path)
        if (!file.exists() || !file.isFile) {
            error("File not found: $path")
        }
        file.readText(Charsets.UTF_8)
    }

    override suspend fun writeUtf8(path: String, content: String) = withContext(Dispatchers.IO) {
        val file = File(File(rootPath), path)
        file.parentFile?.mkdirs()
        file.writeText(content, Charsets.UTF_8)
    }
}

actual fun createDefaultFileTree(): FileTreePort = JvmFileTreePort()
