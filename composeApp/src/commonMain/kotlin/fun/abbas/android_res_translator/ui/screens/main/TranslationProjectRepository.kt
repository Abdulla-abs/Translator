package `fun`.abbas.android_res_translator.ui.screens.main

import `fun`.abbas.android_res_translator.core.resources.planner.TranslationWorkflowMode
import `fun`.abbas.android_res_translator.persistence.TranslationProjectFileStore
import `fun`.abbas.android_res_translator.persistence.TranslationProjectIndex
import `fun`.abbas.android_res_translator.persistence.toIndexEntry
import `fun`.abbas.android_res_translator.persistence.toRecentXmlProject
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorState
import `fun`.abbas.android_res_translator.util.currentEpochMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface TranslationProjectRepository {
    val projects: StateFlow<List<RecentXmlProject>>

    suspend fun reloadFromDisk()

    fun addOrUpdate(project: RecentXmlProject)

    fun addOrUpdateFromUpload(
        sourceXml: String,
        displayName: String,
        sourceLang: String,
        targetLang: String,
    ): RecentXmlProject

    fun addIncrementalFromUpload(
        sourceXml: String,
        displayName: String,
        sourceLang: String,
        targetLang: String,
    ): RecentXmlProject

    fun addFullFromUpload(
        sourceXml: String,
        displayName: String,
        sourceLang: String,
        targetLang: String,
    ): RecentXmlProject

    fun syncEditorState(
        projectId: String,
        editorState: FileEditorState,
    )

    fun readSourceXml(project: RecentXmlProject): String

    fun readTargetBaseline(project: RecentXmlProject): String

    fun persistTargetBaseline(
        projectId: String,
        targetXml: String,
        targetDisplayName: String = "target.xml",
    )

    fun deleteProject(projectId: String)
}

class InMemoryRecentXmlProjectRepository(
    private val maxItems: Int = 10,
) : TranslationProjectRepository {
    private val _projects = MutableStateFlow<List<RecentXmlProject>>(emptyList())
    private val sourceXmlById = mutableMapOf<String, String>()
    private val targetBaselineById = mutableMapOf<String, String>()
    override val projects: StateFlow<List<RecentXmlProject>> = _projects.asStateFlow()

    override suspend fun reloadFromDisk() = Unit

    override fun addOrUpdate(project: RecentXmlProject) {
        val without = _projects.value.filterNot { it.id == project.id }
        _projects.value = (listOf(project) + without).take(maxItems)
    }

    fun addOrUpdateWithSource(
        project: RecentXmlProject,
        sourceXml: String,
        targetXml: String? = null,
    ) {
        sourceXmlById[project.id] = sourceXml
        if (targetXml != null) {
            targetBaselineById[project.id] = targetXml
        }
        addOrUpdate(project)
    }

    override fun addOrUpdateFromUpload(
        sourceXml: String,
        displayName: String,
        sourceLang: String,
        targetLang: String,
    ): RecentXmlProject = addFullFromUpload(sourceXml, displayName, sourceLang, targetLang)

    override fun addIncrementalFromUpload(
        sourceXml: String,
        displayName: String,
        sourceLang: String,
        targetLang: String,
    ): RecentXmlProject {
        val project =
            RecentXmlProject(
                id = displayName + "_" + kotlin.random.Random.nextInt(),
                displayName = displayName,
                modifiedAtEpochMs = currentEpochMillis(),
                progressPercent = 0f,
                translatedKeys = 0,
                totalKeys = countTranslatableKeys(sourceXml).coerceAtLeast(1),
                isComplete = false,
                sourceLang = sourceLang,
                targetLang = targetLang,
                workflowMode = TranslationWorkflowMode.INCREMENTAL,
                hasTargetBaseline = false,
            )
        addOrUpdateWithSource(project, sourceXml)
        return project
    }

    override fun addFullFromUpload(
        sourceXml: String,
        displayName: String,
        sourceLang: String,
        targetLang: String,
    ): RecentXmlProject {
        val project =
            RecentXmlProject(
                id = displayName + "_" + kotlin.random.Random.nextInt(),
                displayName = displayName,
                modifiedAtEpochMs = currentEpochMillis(),
                progressPercent = 0f,
                translatedKeys = 0,
                totalKeys = countTranslatableKeys(sourceXml).coerceAtLeast(1),
                isComplete = false,
                sourceLang = sourceLang,
                targetLang = targetLang,
                workflowMode = TranslationWorkflowMode.FULL,
                hasTargetBaseline = false,
            )
        addOrUpdateWithSource(project, sourceXml)
        return project
    }

    override fun syncEditorState(
        projectId: String,
        editorState: FileEditorState,
    ) {
        _projects.value =
            _projects.value.map { project ->
                if (project.id == projectId) project.withEditorState(editorState) else project
            }
    }

    override fun readSourceXml(project: RecentXmlProject): String = sourceXmlById[project.id].orEmpty()

    override fun readTargetBaseline(project: RecentXmlProject): String = targetBaselineById[project.id].orEmpty()

    override fun persistTargetBaseline(
        projectId: String,
        targetXml: String,
        targetDisplayName: String,
    ) {
        targetBaselineById[projectId] = targetXml
        _projects.value =
            _projects.value.map { project ->
                if (project.id != projectId) {
                    project
                } else {
                    project.copy(
                        hasTargetBaseline = true,
                        targetDisplayName = targetDisplayName,
                        targetBaselinePath = "memory://$projectId/target-baseline.xml",
                    )
                }
            }
    }

    override fun deleteProject(projectId: String) {
        sourceXmlById.remove(projectId)
        targetBaselineById.remove(projectId)
        _projects.value = _projects.value.filterNot { it.id == projectId }
    }
}

class PersistentTranslationProjectRepository(
    private val maxItems: Int = 10,
    private val scope: CoroutineScope,
) : TranslationProjectRepository {
    private val _projects = MutableStateFlow<List<RecentXmlProject>>(emptyList())
    override val projects: StateFlow<List<RecentXmlProject>> = _projects.asStateFlow()

    init {
        scope.launch { reloadFromDisk() }
    }

    override suspend fun reloadFromDisk() {
        val index = TranslationProjectFileStore.readIndex()
        _projects.value = index.projects.map { it.toRecentXmlProject() }
    }

    override fun addOrUpdate(project: RecentXmlProject) {
        val without = _projects.value.filterNot { it.id == project.id }
        val updated = (listOf(project) + without).take(maxItems)
        _projects.value = updated
        persistIndex(updated)
    }

    override fun addOrUpdateFromUpload(
        sourceXml: String,
        displayName: String,
        sourceLang: String,
        targetLang: String,
    ): RecentXmlProject = addFullFromUpload(sourceXml, displayName, sourceLang, targetLang)

    override fun addIncrementalFromUpload(
        sourceXml: String,
        displayName: String,
        sourceLang: String,
        targetLang: String,
    ): RecentXmlProject {
        val project =
            TranslationProjectFileStore.createIncrementalProjectFromSource(
                sourceXml = sourceXml,
                displayName = displayName,
                sourceLang = sourceLang,
                targetLang = targetLang,
            )
        addOrUpdate(project)
        return project
    }

    override fun addFullFromUpload(
        sourceXml: String,
        displayName: String,
        sourceLang: String,
        targetLang: String,
    ): RecentXmlProject {
        val project =
            TranslationProjectFileStore.createFullProject(
                sourceXml = sourceXml,
                displayName = displayName,
                sourceLang = sourceLang,
                targetLang = targetLang,
            )
        addOrUpdate(project)
        return project
    }

    override fun syncEditorState(
        projectId: String,
        editorState: FileEditorState,
    ) {
        val current = _projects.value.find { it.id == projectId } ?: return
        addOrUpdate(current.withEditorState(editorState))
    }

    override fun readSourceXml(project: RecentXmlProject): String =
        TranslationProjectFileStore.readSourceXml(project)

    override fun readTargetBaseline(project: RecentXmlProject): String =
        TranslationProjectFileStore.readTargetBaseline(project)

    override fun persistTargetBaseline(
        projectId: String,
        targetXml: String,
        targetDisplayName: String,
    ) {
        val current = _projects.value.find { it.id == projectId } ?: return
        val updated =
            if (current.workflowMode == TranslationWorkflowMode.INCREMENTAL) {
                TranslationProjectFileStore.attachIncrementalTarget(
                    project = current,
                    targetXml = targetXml,
                    targetDisplayName = targetDisplayName,
                )
            } else {
                TranslationProjectFileStore.writeTargetBaseline(projectId, targetXml)
                current.copy(
                    hasTargetBaseline = true,
                    targetBaselinePath = TranslationProjectFileStore.targetBaselinePath(projectId),
                    targetDisplayName = targetDisplayName,
                )
            }
        addOrUpdate(updated)
    }

    override fun deleteProject(projectId: String) {
        _projects.value.find { it.id == projectId }?.let { project ->
            if (project.sourcePath.isNotBlank()) {
                TranslationProjectFileStore.deleteProjectOnDisk(project)
            }
        }
        val updated = _projects.value.filterNot { it.id == projectId }
        _projects.value = updated
        persistIndex(updated)
    }

    private fun persistIndex(projects: List<RecentXmlProject>) {
        val index = TranslationProjectIndex(projects.map { it.toIndexEntry() })
        TranslationProjectFileStore.writeIndex(index)
    }
}

expect fun createTranslationProjectRepository(scope: CoroutineScope): TranslationProjectRepository
