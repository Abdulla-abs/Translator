package `fun`.abbas.android_res_translator.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import `fun`.abbas.android_res_translator.core.ports.createDefaultFileTree
import `fun`.abbas.android_res_translator.ui.TranslationServices
import `fun`.abbas.android_res_translator.ui.XmlFileAccess
import `fun`.abbas.android_res_translator.ui.files.WithFilePermissions
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorControllerStore
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorScreen
import `fun`.abbas.android_res_translator.ui.screens.files.FileBrowserItem
import `fun`.abbas.android_res_translator.ui.screens.files.FileBrowserScreen
import `fun`.abbas.android_res_translator.ui.screens.files.FileBrowserStore
import `fun`.abbas.android_res_translator.ui.screens.main.TranslationProjectRepository
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsRepository
import `fun`.abbas.android_res_translator.ui.translation.LanguagePickerCatalog

@Composable
fun FilesScreen(
    settings: AppSettingsRepository,
    services: TranslationServices,
    xmlFileAccess: XmlFileAccess,
    projectRepository: TranslationProjectRepository,
    editorControllerStore: FileEditorControllerStore,
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    val fileTree = remember { createDefaultFileTree() }
    val store = remember(projectRepository, fileTree) { FileBrowserStore(fileTree, projectRepository) }
    var mode by remember { mutableStateOf<FilesUiMode>(FilesUiMode.Browse) }
    val snap by settings.snapshot.collectAsState()
    val selectedEngine = remember(snap) { LanguagePickerCatalog.resolveSelectedEngine(snap) }
    LaunchedEffect(searchQuery) {
        store.setSearchQuery(searchQuery)
    }

    WithFilePermissions {
    when (val current = mode) {
        FilesUiMode.Browse ->
            FileBrowserScreen(
                store = store,
                recentProjects = projectRepository,
                onOpenFile = { file -> mode = FilesUiMode.Detail(file) },
                modifier = modifier.fillMaxSize(),
            )

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
                    selectedEngine = selectedEngine,
                    xmlFileAccess = xmlFileAccess,
                    uiLocale = snap.uiLocale,
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
