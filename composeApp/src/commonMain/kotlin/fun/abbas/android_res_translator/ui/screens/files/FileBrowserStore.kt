package `fun`.abbas.android_res_translator.ui.screens.files

import `fun`.abbas.android_res_translator.core.ports.FileTreePort
import `fun`.abbas.android_res_translator.core.ports.FileNode
import `fun`.abbas.android_res_translator.ui.screens.main.InMemoryRecentXmlProjectRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FileBrowserStore(
    private val fileTree: FileTreePort,
    private val recentProjects: InMemoryRecentXmlProjectRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private var pathSegments: List<String> = emptyList()
    private var searchQuery: String = ""

    private val _state = MutableStateFlow(FileBrowserState())
    val state: StateFlow<FileBrowserState> = _state.asStateFlow()

    private val mockXmlById: Map<String, String> =
        mapOf(
            "mock_strings_main" to MOCK_STRINGS_MAIN,
            "mock_errors_en" to MOCK_ERRORS_EN,
            "mock_auth_screens" to MOCK_AUTH_SCREENS,
        )

    init {
        refresh()
    }

    fun getRootPath(): String = fileTree.getRootPath()

    fun changeRootPath(newPath: String) {
        fileTree.setRootPath(newPath)
        pathSegments = emptyList()
        refresh()
    }

    fun navigateHome() {
        pathSegments = emptyList()
        publish()
    }

    fun navigateToSegment(index: Int) {
        if (index < 0) {
            navigateHome()
            return
        }
        pathSegments = pathSegments.take(index + 1)
        publish()
    }

    fun enterFolder(folder: FileBrowserItem.Folder) {
        pathSegments = folder.path.split('/').filter { it.isNotEmpty() }
        publish()
    }

    fun setSearchQuery(query: String) {
        searchQuery = query
        publish()
    }

    fun refresh() {
        publish()
    }

    fun navigateToResources() {
        pathSegments = listOf("src", "commonMain", "resources")
        publish()
    }

    suspend fun loadXmlContent(id: String): String {
        return try {
            fileTree.readUtf8(id)
        } catch (_: Exception) {
            recentProjects.projects.value.find { it.id == id }?.sourceXml
                ?: mockXmlById[id]
                ?: ""
        }
    }

    private fun publish() {
        scope.launch {
            val currentDir = pathSegments.joinToString("/")
            val nodes = try {
                fileTree.listChildren(currentDir)
            } catch (e: Exception) {
                emptyList()
            }

            val itemsFromTree = nodes.map { node ->
                if (node.isDirectory) {
                    FileBrowserItem.Folder(name = node.name, path = node.path)
                } else {
                    FileBrowserItem.XmlFile(
                        id = node.path,
                        name = node.name,
                        path = node.path,
                        versionLabel = "v1.0",
                        sizeLabel = formatSize(node.size),
                        xmlContent = null
                    )
                }
            }

            // Filter to folders and XML files
            var allItems = itemsFromTree.filter {
                it is FileBrowserItem.Folder || (it is FileBrowserItem.XmlFile && it.name.endsWith(".xml", ignoreCase = true))
            }

            // Merging local upload items to simulate resources folder matching
            if (currentDir == "src/commonMain/resources") {
                val uploaded = recentProjects.projects.value.map { project ->
                    FileBrowserItem.XmlFile(
                        id = project.id,
                        name = project.displayName,
                        path = "src/commonMain/resources/${project.displayName}",
                        versionLabel = "v1.0",
                        sizeLabel = if (project.sourceXml.isNotBlank()) formatSize(project.sourceXml.length.toLong()) else "${project.totalKeys} keys",
                        xmlContent = project.sourceXml
                    )
                }
                allItems = allItems + uploaded
            }

            val filtered = if (searchQuery.isBlank()) {
                allItems
            } else {
                val q = searchQuery.trim().lowercase()
                allItems.filter { item ->
                    when (item) {
                        is FileBrowserItem.Folder -> item.name.lowercase().contains(q)
                        is FileBrowserItem.XmlFile -> item.name.lowercase().contains(q)
                    }
                }
            }

            _state.value = FileBrowserState(
                pathSegments = pathSegments,
                items = filtered,
                searchQuery = searchQuery,
                rootPath = fileTree.getRootPath()
            )
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            bytes >= 1024 -> "${bytes / 1024} KB"
            else -> "$bytes B"
        }
    }

    companion object {
        private const val MOCK_STRINGS_MAIN =
            """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">KMP Translator</string>
</resources>"""

        private const val MOCK_ERRORS_EN =
            """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="error_network">Network error</string>
</resources>"""

        private const val MOCK_AUTH_SCREENS =
            """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="login_title">Sign in</string>
</resources>"""
    }
}
