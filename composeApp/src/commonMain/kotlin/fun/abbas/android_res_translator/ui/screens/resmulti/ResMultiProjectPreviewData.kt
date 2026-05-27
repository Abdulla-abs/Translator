package `fun`.abbas.android_res_translator.ui.screens.resmulti

import `fun`.abbas.android_res_translator.persistence.ResMultiVersionIndex
import `fun`.abbas.android_res_translator.persistence.ResMultiVersionIndexEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal val previewResMultiLanguages =
    listOf(
        ResMultiLanguageEntry(
            folderName = "values",
            langCode = "default",
            stringsRelativePath = "values/strings.xml",
        ),
        ResMultiLanguageEntry(
            folderName = "values-zh",
            langCode = "zh",
            stringsRelativePath = "values-zh/strings.xml",
        ),
        ResMultiLanguageEntry(
            folderName = "values-ja",
            langCode = "ja",
            stringsRelativePath = "values-ja/strings.xml",
        ),
    )

internal fun previewResMultiProject(
    initState: ResMultiInitState = ResMultiInitState.READY,
    dirty: Boolean = false,
    initError: String? = null,
): ResMultiProject =
    ResMultiProject(
        id = "preview-project",
        displayName = "Sample Android App",
        createdAtEpochMs = 1_700_000_000_000L,
        modifiedAtEpochMs = 1_700_100_000_000L,
        sourceResPath = "/home/user/projects/sample/app/src/main/res",
        initState = initState,
        languages = if (initState == ResMultiInitState.READY) previewResMultiLanguages else emptyList(),
        initError = initError,
        dirty = dirty,
    )

internal fun previewResMultiVersionEntry(
    id: String = "v0001-initial",
    displayName: String = "Initial snapshot",
    createdAtEpochMs: Long = 1_700_000_000_000L,
): ResMultiVersionIndexEntry =
    ResMultiVersionIndexEntry(
        id = id,
        displayName = displayName,
        createdAtEpochMs = createdAtEpochMs,
    )

internal fun previewResMultiVersionIndex(
    dirty: Boolean = false,
    versionCount: Int = 3,
): ResMultiVersionIndex {
    val versions =
        (1..versionCount).map { index ->
            previewResMultiVersionEntry(
                id = "v000$index-release",
                displayName = "Release $index",
                createdAtEpochMs = 1_700_000_000_000L + index * 86_400_000L,
            )
        }
    return ResMultiVersionIndex(
        headId = versions.lastOrNull()?.id,
        dirty = dirty,
        versions = versions,
    )
}

internal class PreviewResMultiProjectRepository(
    initialProjects: List<ResMultiProject> = listOf(previewResMultiProject()),
) : ResMultiProjectRepository {
    private val _projects = MutableStateFlow(initialProjects)

    override val projects: StateFlow<List<ResMultiProject>> = _projects.asStateFlow()

    override suspend fun reloadFromDisk() = Unit

    override fun createProject(displayName: String): ResMultiProject {
        val project = previewResMultiProject().copy(displayName = displayName)
        _projects.value = listOf(project) + _projects.value
        return project
    }

    override suspend fun initializeFromSource(
        projectId: String,
        sourceResPath: String,
    ): ResMultiProject {
        val updated =
            previewResMultiProject(initState = ResMultiInitState.READY)
                .copy(id = projectId, sourceResPath = sourceResPath)
        _projects.value = _projects.value.map { if (it.id == projectId) updated else it }
        return updated
    }

    override fun readProject(projectId: String): ResMultiProject? =
        _projects.value.find { it.id == projectId }

    override fun deleteProject(projectId: String) {
        _projects.value = _projects.value.filterNot { it.id == projectId }
    }

    override suspend fun pushVersion(
        projectId: String,
        displayName: String,
    ): Result<ResMultiVersionIndex> = Result.success(previewResMultiVersionIndex())

    override suspend fun restoreToVersion(
        projectId: String,
        versionId: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun deleteVersionAndSuccessors(
        projectId: String,
        versionId: String,
    ): Result<ResMultiVersionIndex> = Result.success(previewResMultiVersionIndex())
}

internal fun previewResMultiProjectRepository(
    projects: List<ResMultiProject> = listOf(previewResMultiProject()),
): ResMultiProjectRepository = PreviewResMultiProjectRepository(projects)
