package `fun`.abbas.android_res_translator.ui.screens.resmulti

import `fun`.abbas.android_res_translator.persistence.ResMultiProjectFileStore
import `fun`.abbas.android_res_translator.persistence.ResMultiProjectIndex
import `fun`.abbas.android_res_translator.persistence.ResMultiVersionIndex
import `fun`.abbas.android_res_translator.persistence.ResProjectVersionStore
import `fun`.abbas.android_res_translator.persistence.toIndexEntry
import `fun`.abbas.android_res_translator.persistence.toMeta
import `fun`.abbas.android_res_translator.persistence.toProject
import `fun`.abbas.android_res_translator.util.currentEpochMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface ResMultiProjectRepository {
    val projects: StateFlow<List<ResMultiProject>>

    suspend fun reloadFromDisk()

    fun createProject(displayName: String): ResMultiProject

    suspend fun initializeFromSource(
        projectId: String,
        sourceResPath: String,
    ): ResMultiProject

    fun readProject(projectId: String): ResMultiProject?

    fun deleteProject(projectId: String)

    suspend fun pushVersion(
        projectId: String,
        displayName: String,
    ): Result<ResMultiVersionIndex>

    suspend fun restoreToVersion(
        projectId: String,
        versionId: String,
    ): Result<Unit>

    suspend fun deleteVersionAndSuccessors(
        projectId: String,
        versionId: String,
    ): Result<ResMultiVersionIndex>
}

class PersistentResMultiProjectRepository(
    private val maxItems: Int = 10,
    private val scope: CoroutineScope,
) : ResMultiProjectRepository {
    private val _projects = MutableStateFlow<List<ResMultiProject>>(emptyList())
    override val projects: StateFlow<List<ResMultiProject>> = _projects.asStateFlow()

    init {
        scope.launch { reloadFromDisk() }
    }

    override suspend fun reloadFromDisk() {
        val index = ResMultiProjectFileStore.readIndex()
        _projects.value =
            index.projects.mapNotNull { entry ->
                ResMultiProjectFileStore.readMeta(entry.id)?.toProject()
            }
    }

    override fun createProject(displayName: String): ResMultiProject {
        val project = ResMultiProjectFileStore.createProject(displayName)
        addOrUpdate(project)
        return project
    }

    override suspend fun initializeFromSource(
        projectId: String,
        sourceResPath: String,
    ): ResMultiProject =
        withContext(Dispatchers.Default) {
            markInitializing(projectId)
            val updated = ResMultiProjectFileStore.initializeFromSource(projectId, sourceResPath)
            addOrUpdate(updated)
            updated
        }

    override fun readProject(projectId: String): ResMultiProject? =
        ResMultiProjectFileStore.readMeta(projectId)?.toProject()
            ?: _projects.value.find { it.id == projectId }

    override fun deleteProject(projectId: String) {
        ResMultiProjectFileStore.deleteProjectOnDisk(projectId)
        _projects.value = _projects.value.filterNot { it.id == projectId }
        persistIndex(_projects.value)
    }

    override suspend fun pushVersion(
        projectId: String,
        displayName: String,
    ): Result<ResMultiVersionIndex> =
        withContext(Dispatchers.Default) {
            ResProjectVersionStore.pushVersion(projectId, displayName).onSuccess {
                reloadFromDisk()
            }
        }

    override suspend fun restoreToVersion(
        projectId: String,
        versionId: String,
    ): Result<Unit> =
        withContext(Dispatchers.Default) {
            ResProjectVersionStore.restoreToVersion(projectId, versionId).onSuccess {
                reloadFromDisk()
            }
        }

    override suspend fun deleteVersionAndSuccessors(
        projectId: String,
        versionId: String,
    ): Result<ResMultiVersionIndex> =
        withContext(Dispatchers.Default) {
            ResProjectVersionStore.deleteVersionAndSuccessors(projectId, versionId).onSuccess {
                reloadFromDisk()
            }
        }

    private fun markInitializing(projectId: String) {
        val current = readProject(projectId) ?: return
        val updating = current.copy(initState = ResMultiInitState.INITIALIZING, initError = null)
        addOrUpdate(updating)
    }

    private fun addOrUpdate(project: ResMultiProject) {
        val without = _projects.value.filterNot { it.id == project.id }
        val merged = project.copy(modifiedAtEpochMs = currentEpochMillis())
        _projects.value = (listOf(merged) + without).take(maxItems)
        persistIndex(_projects.value)
    }

    private fun persistIndex(projects: List<ResMultiProject>) {
        val index = ResMultiProjectIndex(projects.map { it.toIndexEntry() })
        ResMultiProjectFileStore.writeIndex(index)
    }
}

expect fun createResMultiProjectRepository(scope: CoroutineScope): ResMultiProjectRepository
