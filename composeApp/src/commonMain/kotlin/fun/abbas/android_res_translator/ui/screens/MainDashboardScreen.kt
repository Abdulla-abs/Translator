package `fun`.abbas.android_res_translator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.TranslationServices
import `fun`.abbas.android_res_translator.ui.XmlFileAccess
import `fun`.abbas.android_res_translator.ui.screens.main.DashboardInsightSection
import `fun`.abbas.android_res_translator.ui.screens.main.FileProjectsSection
import `fun`.abbas.android_res_translator.persistence.TranslationProjectFileStore
import `fun`.abbas.android_res_translator.ui.screens.main.QuickTranslateSection
import `fun`.abbas.android_res_translator.ui.screens.main.TranslationProjectRepository
import `fun`.abbas.android_res_translator.ui.files.WithFilePermissions
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorControllerStore
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorScreen
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsRepository
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import `fun`.abbas.android_res_translator.ui.translation.LanguagePickerCatalog

@Composable
fun MainDashboardScreen(
    settings: AppSettingsRepository,
    services: TranslationServices,
    xmlFileAccess: XmlFileAccess,
    projectRepository: TranslationProjectRepository,
    editorControllerStore: FileEditorControllerStore,
    onNavigateToFiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snap by settings.snapshot.collectAsState()
    val selectedEngine = remember(snap) { LanguagePickerCatalog.resolveSelectedEngine(snap) }
    val scroll = rememberScrollState()
    var uploadCounter by remember { mutableIntStateOf(0) }
    var mode by remember { mutableStateOf<DashboardUiMode>(DashboardUiMode.Home) }

    fun onUploadXml(xml: String) {
        uploadCounter += 1
        val name = if (uploadCounter == 1) "strings.xml" else "strings_$uploadCounter.xml"
        projectRepository.addOrUpdateFromUpload(
            sourceXml = xml,
            displayName = name,
            sourceLang = snap.defaultSourceLang,
            targetLang = snap.defaultTargetLang,
        )
    }

    val projects by projectRepository.projects.collectAsState()

    WithFilePermissions {
    when (val current = mode) {
        is DashboardUiMode.Editor -> {
            val project = projects.find { it.id == current.projectId }
            if (project != null) {
                val projectId = current.projectId
                val sourceXml = remember(projectId) { projectRepository.readSourceXml(project) }
                val initialSession =
                    remember(projectId) { TranslationProjectFileStore.loadSessionSnapshot(project) }
                val controller =
                    remember(projectId, sourceXml) {
                        editorControllerStore.getOrCreate(
                            key = projectId,
                            fileName = project.displayName,
                            filePath = "recent/$projectId",
                            sourceLang = project.sourceLang,
                            targetLang = project.targetLang,
                            sourceXml = sourceXml,
                            initialSession = initialSession,
                            resultPath = project.resultPath,
                            onStateChange = { editorState ->
                                projectRepository.syncEditorState(projectId, editorState)
                            },
                            onPersistResult = {
                                editorControllerStore.currentState(projectId)?.let { editorState ->
                                    projectRepository.syncEditorState(projectId, editorState)
                                }
                            },
                        )
                    }
                FileEditorScreen(
                    controller = controller,
                    selectedEngine = selectedEngine,
                    xmlFileAccess = xmlFileAccess,
                    onBack = { mode = DashboardUiMode.Home },
                    onEditorStateChange = { editorState ->
                        projectRepository.syncEditorState(projectId, editorState)
                    },
                    modifier = modifier,
                )
            }
        }

        DashboardUiMode.Home ->
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = 1280.dp)
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .padding(AppSpacing.gutter),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
            ) {
                QuickTranslateSection(
                    settings = settings,
                    services = services,
                    defaultFrom = snap.defaultSourceLang,
                    defaultTo = snap.defaultTargetLang,
                )
                FileProjectsSection(
                    repository = projectRepository,
                    onViewAllClick = onNavigateToFiles,
                    onUploadClick = {
                        xmlFileAccess.launchPickXml { result ->
                            result.onSuccess(::onUploadXml)
                        }
                    },
                    onProjectClick = { mode = DashboardUiMode.Editor(it.id) },
                )
                DashboardInsightSection()
                Spacer(Modifier.height(AppSpacing.lg))
            }
        }
    }
    }
    }
}

private sealed interface DashboardUiMode {
    data object Home : DashboardUiMode

    data class Editor(
        val projectId: String,
    ) : DashboardUiMode
}
