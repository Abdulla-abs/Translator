package `fun`.abbas.android_res_translator.ui.screens.files

sealed interface FileBrowserItem {
    data class Folder(
        val name: String,
        val path: String,
    ) : FileBrowserItem

    data class XmlFile(
        val id: String,
        val name: String,
        val path: String,
        val versionLabel: String,
        val sizeLabel: String,
        val xmlContent: String?,
    ) : FileBrowserItem
}

data class FileBrowserState(
    val pathSegments: List<String> = emptyList(),
    val items: List<FileBrowserItem> = emptyList(),
    val searchQuery: String = "",
    val rootPath: String = "",
)
