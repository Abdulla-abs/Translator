package `fun`.abbas.android_res_translator.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.core.ports.createDefaultFileTree
import `fun`.abbas.android_res_translator.ui.TranslationServices
import `fun`.abbas.android_res_translator.ui.XmlFileAccess
import `fun`.abbas.android_res_translator.ui.files.WithFilePermissions
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorControllerStore
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorScreen
import `fun`.abbas.android_res_translator.ui.screens.files.FileBrowserItem
import `fun`.abbas.android_res_translator.ui.screens.files.FileBrowserScreen
import `fun`.abbas.android_res_translator.ui.screens.files.FileBrowserStore
import `fun`.abbas.android_res_translator.ui.screens.main.InMemoryRecentXmlProjectRepository
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsRepository
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing

@Composable
fun FilesScreen(
    settings: AppSettingsRepository,
    services: TranslationServices,
    xmlFileAccess: XmlFileAccess,
    projectRepository: InMemoryRecentXmlProjectRepository,
    editorControllerStore: FileEditorControllerStore,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fileTree = remember { createDefaultFileTree() }
    val store = remember(projectRepository, fileTree) { FileBrowserStore(fileTree, projectRepository) }
    var mode by remember { mutableStateOf<FilesUiMode>(FilesUiMode.Browse) }
    val snap by settings.snapshot.collectAsState()
    var compactSearch by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty() || compactSearch.isEmpty()) {
            store.setSearchQuery(searchQuery)
        }
    }

    WithFilePermissions {
    when (val current = mode) {
        FilesUiMode.Browse ->
            BoxWithConstraints(modifier = modifier.fillMaxSize()) {
                val useCompactSearch = maxWidth < 600.dp
                if (useCompactSearch) {
                    Column(Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = compactSearch,
                            onValueChange = {
                                compactSearch = it
                                onSearchQueryChange(it)
                                store.setSearchQuery(it)
                            },
                            placeholder = { Text("Search files...") },
                            singleLine = true,
                            shape = AppControlShape,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppSpacing.gutter, vertical = AppSpacing.sm),
                        )
                        FileBrowserScreen(
                            store = store,
                            recentProjects = projectRepository,
                            xmlFileAccess = xmlFileAccess,
                            onOpenFile = { file -> mode = FilesUiMode.Detail(file) },
                            showCompactSearch = true,
                            compactSearchQuery = compactSearch,
                            onCompactSearchChange = { compactSearch = it },
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    LaunchedEffect(searchQuery) {
                        store.setSearchQuery(searchQuery)
                    }
                    FileBrowserScreen(
                        store = store,
                        recentProjects = projectRepository,
                        xmlFileAccess = xmlFileAccess,
                        onOpenFile = { file -> mode = FilesUiMode.Detail(file) },
                        showCompactSearch = false,
                        compactSearchQuery = "",
                        onCompactSearchChange = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

        is FilesUiMode.Detail -> {
            var fileContent by remember(current.file.id) { mutableStateOf<String?>(null) }
            LaunchedEffect(current.file.id) {
                fileContent = store.loadXmlContent(current.file.id)
            }
            val content = fileContent
            if (content != null) {
                val fileKey = "file:${current.file.id}"
                val controller =
                    remember(fileKey) {
                        editorControllerStore.getOrCreate(
                            key = fileKey,
                            fileName = current.file.name,
                            filePath = current.file.path,
                            sourceLang = snap.defaultSourceLang,
                            targetLang = snap.defaultTargetLang,
                            sourceXml = content,
                        )
                    }
                FileEditorScreen(
                    controller = controller,
                    xmlFileAccess = xmlFileAccess,
                    onBack = { mode = FilesUiMode.Browse },
                    modifier = modifier,
                )
            } else {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    } // end when
    } // end WithFilePermissions
}

private sealed interface FilesUiMode {
    data object Browse : FilesUiMode

    data class Detail(
        val file: FileBrowserItem.XmlFile,
    ) : FilesUiMode
}
