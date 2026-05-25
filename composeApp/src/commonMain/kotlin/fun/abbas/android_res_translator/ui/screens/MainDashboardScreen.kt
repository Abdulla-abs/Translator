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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.persistence.TranslationProjectFileStore
import `fun`.abbas.android_res_translator.ui.TranslationServices
import `fun`.abbas.android_res_translator.ui.XmlFileAccess
import `fun`.abbas.android_res_translator.ui.files.DroppedXmlFile
import `fun`.abbas.android_res_translator.ui.files.WithFilePermissions
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorControllerStore
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorProjectSettingsScreen
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorScreen
import `fun`.abbas.android_res_translator.ui.screens.main.DashboardInsightSection
import `fun`.abbas.android_res_translator.ui.screens.main.FileProjectsSection
import `fun`.abbas.android_res_translator.ui.screens.main.QuickTranslateSection
import `fun`.abbas.android_res_translator.ui.screens.main.RecentXmlProject
import `fun`.abbas.android_res_translator.ui.screens.main.TranslationProjectRepository
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
    /** 新建项目后立刻进详情时，projects 列表可能尚未刷新，用此兜底。 */
    var openingProject by remember { mutableStateOf<RecentXmlProject?>(null) }

    fun openEditor(project: RecentXmlProject) {
        openingProject = project
        mode = DashboardUiMode.Editor(project.id)
    }

    fun onIncrementalUpload(
        xml: String,
        displayName: String,
    ) {
        val project =
            projectRepository.addIncrementalFromUpload(
                sourceXml = xml,
                displayName = displayName,
                sourceLang = snap.defaultSourceLang,
                targetLang = snap.defaultTargetLang,
            )
        openEditor(project)
    }

    fun onIncrementalDrop(files: List<DroppedXmlFile>) {
        files.firstOrNull()?.let { file ->
            onIncrementalUpload(file.content, file.displayName)
        }
    }

    fun onFullUpload(
        xml: String,
        displayName: String,
    ) {
        val project =
            projectRepository.addFullFromUpload(
                sourceXml = xml,
                displayName = displayName,
                sourceLang = snap.defaultSourceLang,
                targetLang = snap.defaultTargetLang,
            )
        openEditor(project)
    }

    fun onFullDrop(files: List<DroppedXmlFile>) {
        files.firstOrNull()?.let { file ->
            onFullUpload(file.content, file.displayName)
        }
    }

    val projects by projectRepository.projects.collectAsState()

    WithFilePermissions {
        when (val current = mode) {
            is DashboardUiMode.Editor -> {
                val project =
                    projects.find { it.id == current.projectId }
                        ?: openingProject?.takeIf { it.id == current.projectId }
                if (project != null) {
                    LaunchedEffect(projects, current.projectId) {
                        if (projects.any { it.id == current.projectId }) {
                            openingProject = null
                        }
                    }
                    val projectId = current.projectId
                    val sourceXml = remember(projectId) { projectRepository.readSourceXml(project) }
                    val targetBaseline =
                        remember(projectId, project.hasTargetBaseline) {
                            projectRepository.readTargetBaseline(project).takeIf { it.isNotBlank() }
                        }
                    val initialSession =
                        remember(projectId) { TranslationProjectFileStore.loadSessionSnapshot(project) }
                    val resolvedSourceLang =
                        initialSession?.sourceLang?.takeIf { it.isNotBlank() } ?: project.sourceLang
                    val resolvedTargetLang =
                        initialSession?.targetLang?.takeIf { it.isNotBlank() } ?: project.targetLang
                    val controller =
                        remember(
                            projectId,
                            sourceXml,
                            targetBaseline,
                            project.workflowMode,
                            resolvedSourceLang,
                            resolvedTargetLang,
                        ) {
                            editorControllerStore.getOrCreate(
                                key = projectId,
                                fileName = project.displayName,
                                filePath = "recent/$projectId",
                                sourceLang = resolvedSourceLang,
                                targetLang = resolvedTargetLang,
                                sourceXml = sourceXml,
                                initialSession = initialSession,
                                resultPath = project.resultPath,
                                workflowMode = project.workflowMode,
                                targetBaselineXml = targetBaseline,
                                initialForceTranslation = initialSession?.forceTranslation ?: false,
                                onTargetBaselinePersist = { xml ->
                                    projectRepository.persistTargetBaseline(
                                        projectId = projectId,
                                        targetXml = xml,
                                        targetDisplayName = "target.xml",
                                    )
                                },
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
                    var showProjectSettings by rememberSaveable(projectId) { mutableStateOf(false) }
                    val editorState by controller.state.collectAsState()
                    if (showProjectSettings) {
                        FileEditorProjectSettingsScreen(
                            projectDisplayName = project.displayName,
                            forceTranslation = editorState.forceTranslation,
                            onForceTranslationChange = controller::setForceTranslation,
                            enabled = !editorState.isRunning,
                            onBack = { showProjectSettings = false },
                            modifier = modifier,
                        )
                    } else {
                        FileEditorScreen(
                            controller = controller,
                            workflowMode = project.workflowMode,
                            hasTargetBaseline = project.hasTargetBaseline || controller.hasTargetBaseline,
                            selectedEngine = selectedEngine,
                            xmlFileAccess = xmlFileAccess,
                            uiLocale = snap.uiLocale,
                            onBack = {
                                openingProject = null
                                mode = DashboardUiMode.Home
                            },
                            onEditorStateChange = { state ->
                                projectRepository.syncEditorState(projectId, state)
                            },
                            onOpenProjectSettings = { showProjectSettings = true },
                            modifier = modifier,
                        )
                    }
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
                                onIncrementalUploadClick = {
                                    xmlFileAccess.launchPickXml { result ->
                                        result.onSuccess { xml ->
                                            uploadCounter += 1
                                            val name =
                                                if (uploadCounter == 1) {
                                                    "strings.xml"
                                                } else {
                                                    "strings_$uploadCounter.xml"
                                                }
                                            onIncrementalUpload(xml, name)
                                        }
                                    }
                                },
                                onFullUploadClick = {
                                    xmlFileAccess.launchPickXml { result ->
                                        result.onSuccess { xml ->
                                            uploadCounter += 1
                                            val name =
                                                if (uploadCounter == 1) {
                                                    "strings.xml"
                                                } else {
                                                    "strings_$uploadCounter.xml"
                                                }
                                            onFullUpload(xml, name)
                                        }
                                    }
                                },
                                onIncrementalDrop = ::onIncrementalDrop,
                                onFullDrop = ::onFullDrop,
                                onProjectClick = { openEditor(it) },
                                onDeleteProject = { project ->
                                    editorControllerStore.release(project.id)
                                    projectRepository.deleteProject(project.id)
                                    val currentMode = mode
                                    if (currentMode is DashboardUiMode.Editor && currentMode.projectId == project.id) {
                                        mode = DashboardUiMode.Home
                                    }
                                },
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
