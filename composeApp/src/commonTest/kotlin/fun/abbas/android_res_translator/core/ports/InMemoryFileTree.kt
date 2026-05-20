package `fun`.abbas.android_res_translator.core.ports


/**
 * 测试用内存文件树：路径统一为 `/` 分隔、无首尾斜杠（根目录用 `res` 这类单段名）。
 */
class InMemoryFileTree(
    initialDirs: Collection<String> = emptyList(),
    initialFiles: Map<String, String> = emptyMap(),
) : FileTreePort {
    private var rootPath = "res"
    private val directories = mutableSetOf<String>()
    private val files = mutableMapOf<String, String>()

    override fun getRootPath(): String = rootPath

    override fun setRootPath(path: String) {
        rootPath = path
    }

    init {
        initialDirs.forEach { mkdirAll(it) }
        initialFiles.forEach { (p, c) -> putFileInternal(p, c) }
    }

    override suspend fun listChildren(dir: String): List<FileNode> {
            val d = normalize(dir)
            val prefix = if (d.isEmpty()) "" else "$d/"
            val seen = mutableSetOf<String>()
            val out = mutableListOf<FileNode>()
            for (path in directories) {
                if (path == d || !path.startsWith(prefix)) continue
                val rel = path.removePrefix(prefix)
                if (rel.isEmpty() || '/' in rel) continue
                val childPath = join(d, rel)
                if (!seen.add(childPath)) continue
                out.add(FileNode(name = rel, path = childPath, isDirectory = true))
            }
            for (path in files.keys) {
                if (!path.startsWith(prefix)) continue
                val rel = path.removePrefix(prefix)
                if (rel.isEmpty() || '/' in rel) continue
                val childPath = join(d, rel)
                if (!seen.add(childPath)) continue
                out.add(FileNode(name = rel, path = childPath, isDirectory = false))
            }
            return out.sortedBy { it.name }
    }

    override suspend fun readUtf8(path: String): String {
            val p = normalize(path)
            return files[p] ?: error("file not found: $p")
    }

    override suspend fun writeUtf8(
        path: String,
        content: String,
    ) { putFileInternal(path, content) }

    private fun putFileInternal(
        rawPath: String,
        content: String,
    ) {
        val p = normalize(rawPath)
        mkdirAll(parentOf(p))
        files[p] = content
    }

    private fun mkdirAll(rawPath: String) {
        val p = normalize(rawPath)
        if (p.isEmpty()) return
        var acc = ""
        for (seg in p.split('/')) {
            acc = if (acc.isEmpty()) seg else "$acc/$seg"
            directories.add(acc)
        }
    }

    private fun normalize(s: String): String = s.trim().replace('\\', '/').trimEnd('/')

    private fun parentOf(path: String): String {
        val idx = path.lastIndexOf('/')
        return if (idx <= 0) "" else path.substring(0, idx)
    }

    private fun join(
        dir: String,
        child: String,
    ): String =
        when {
            dir.isEmpty() -> child
            child.isEmpty() -> dir
            else -> "$dir/$child"
        }
}
