package `fun`.abbas.android_res_translator.core.ports

class IosFileTreePort : FileTreePort {
    private var rootPath = "IosRoot"

    override fun getRootPath(): String = rootPath

    override fun setRootPath(path: String) {
        rootPath = path
    }

    override suspend fun listChildren(dir: String): List<FileNode> {
        val d = dir.trimEnd('/').replace('\\', '/')
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

    override suspend fun readUtf8(path: String): String {
        return when (path.substringAfterLast('/')) {
            "strings_main.xml" -> MOCK_STRINGS_MAIN
            "errors_en.xml" -> MOCK_ERRORS_EN
            "auth_screens.xml" -> MOCK_AUTH_SCREENS
            else -> error("File not found: $path")
        }
    }

    override suspend fun writeUtf8(path: String, content: String) {
        // no-op
    }

    companion object {
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

actual fun createDefaultFileTree(): FileTreePort = IosFileTreePort()
