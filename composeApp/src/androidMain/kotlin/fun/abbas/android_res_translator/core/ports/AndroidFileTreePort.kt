package `fun`.abbas.android_res_translator.core.ports

import `fun`.abbas.android_res_translator.AndroidSettingsContext
import `fun`.abbas.android_res_translator.ui.files.hasAllFilesAccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidFileTreePort : FileTreePort {
    private var rootPath: String = determineDefaultRoot()

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

        if (targetDir.exists() && targetDir.isDirectory) {
            val children = targetDir.listFiles()
            if (!children.isNullOrEmpty()) {
                return@withContext children.map { child ->
                    val relPath = child.relativeTo(root).path.replace('\\', '/')
                    FileNode(
                        name = child.name,
                        path = relPath,
                        isDirectory = child.isDirectory,
                        size = if (child.isFile) child.length() else 0L
                    )
                }.sortedWith(
                    compareBy<FileNode> { !it.isDirectory }.thenBy { it.name.lowercase() }
                )
            }
        }

        // Fallback: virtual mock tree for the default project structure
        getMockNodesForDir(dir)
    }

    override suspend fun readUtf8(path: String): String = withContext(Dispatchers.IO) {
        val file = File(File(rootPath), path)
        if (file.exists() && file.isFile) {
            return@withContext file.readText(Charsets.UTF_8)
        }
        // Fallback to mock content
        when (path.substringAfterLast('/')) {
            "strings_main.xml" -> MOCK_STRINGS_MAIN
            "errors_en.xml" -> MOCK_ERRORS_EN
            "auth_screens.xml" -> MOCK_AUTH_SCREENS
            else -> error("File not found: $path")
        }
    }

    override suspend fun writeUtf8(path: String, content: String) = withContext(Dispatchers.IO) {
        val file = File(File(rootPath), path)
        file.parentFile?.mkdirs()
        file.writeText(content, Charsets.UTF_8)
    }

    private fun getMockNodesForDir(dir: String): List<FileNode> {
        val d = dir.trimEnd('/')
        return when (d) {
            "" -> listOf(FileNode("src", "src", true))
            "src" -> listOf(FileNode("commonMain", "src/commonMain", true))
            "src/commonMain" -> listOf(FileNode("resources", "src/commonMain/resources", true))
            "src/commonMain/resources" -> listOf(
                FileNode("layout", "src/commonMain/resources/layout", true),
                FileNode("strings_main.xml", "src/commonMain/resources/strings_main.xml", false, MOCK_STRINGS_MAIN.length.toLong()),
                FileNode("errors_en.xml", "src/commonMain/resources/errors_en.xml", false, MOCK_ERRORS_EN.length.toLong()),
                FileNode("auth_screens.xml", "src/commonMain/resources/auth_screens.xml", false, MOCK_AUTH_SCREENS.length.toLong())
            )
            else -> emptyList()
        }
    }

    companion object {
        private fun determineDefaultRoot(): String {
            return try {
                if (hasAllFilesAccess()) {
                    android.os.Environment.getExternalStorageDirectory().absolutePath.replace('\\', '/')
                } else {
                    AndroidSettingsContext.require().filesDir.absolutePath.replace('\\', '/')
                }
            } catch (_: Exception) {
                "AndroidRoot"
            }
        }

        private const val MOCK_STRINGS_MAIN = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">KMP Translator</string>
</resources>"""

        private const val MOCK_ERRORS_EN = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="error_network">Network error</string>
</resources>"""

        private const val MOCK_AUTH_SCREENS = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="login_title">Sign in</string>
</resources>"""
    }
}

actual fun createDefaultFileTree(): FileTreePort = AndroidFileTreePort()
