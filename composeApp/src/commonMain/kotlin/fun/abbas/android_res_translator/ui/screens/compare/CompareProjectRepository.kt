package `fun`.abbas.android_res_translator.ui.screens.compare

import `fun`.abbas.android_res_translator.persistence.CompareProjectFileStore
import `fun`.abbas.android_res_translator.persistence.CompareProjectIndex
import `fun`.abbas.android_res_translator.persistence.toCompareProject
import `fun`.abbas.android_res_translator.persistence.toIndexEntry
import `fun`.abbas.android_res_translator.util.currentEpochMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface CompareProjectRepository {
    val projects: StateFlow<List<CompareProject>>

    suspend fun reloadFromDisk()

    fun createProject(displayName: String): CompareProject

    fun attachLeftFile(
        projectId: String,
        xml: String,
        displayName: String,
    ): CompareProject?

    fun attachRightFile(
        projectId: String,
        xml: String,
        displayName: String,
    ): CompareProject?

    fun readLeftXml(project: CompareProject): String

    fun readRightXml(project: CompareProject): String

    fun deleteProject(projectId: String)
}

class PersistentCompareProjectRepository(
    private val maxItems: Int = 10,
    private val scope: CoroutineScope,
) : CompareProjectRepository {
    private val _projects = MutableStateFlow<List<CompareProject>>(emptyList())
    override val projects: StateFlow<List<CompareProject>> = _projects.asStateFlow()

    init {
        scope.launch { reloadFromDisk() }
    }

    override suspend fun reloadFromDisk() {
        val index = CompareProjectFileStore.readIndex()
        _projects.value = index.projects.map { it.toCompareProject() }
    }

    override fun createProject(displayName: String): CompareProject {
        val project = CompareProjectFileStore.createProject(displayName)
        addOrUpdate(project)
        return project
    }

    override fun attachLeftFile(
        projectId: String,
        xml: String,
        displayName: String,
    ): CompareProject? {
        val updated = CompareProjectFileStore.writeLeftFile(projectId, xml, displayName)
        addOrUpdate(updated)
        return updated
    }

    override fun attachRightFile(
        projectId: String,
        xml: String,
        displayName: String,
    ): CompareProject? {
        val updated = CompareProjectFileStore.writeRightFile(projectId, xml, displayName)
        addOrUpdate(updated)
        return updated
    }

    override fun readLeftXml(project: CompareProject): String = CompareProjectFileStore.readLeftXml(project)

    override fun readRightXml(project: CompareProject): String = CompareProjectFileStore.readRightXml(project)

    override fun deleteProject(projectId: String) {
        _projects.value.find { it.id == projectId }?.let { CompareProjectFileStore.deleteProjectOnDisk(it) }
        val updated = _projects.value.filterNot { it.id == projectId }
        _projects.value = updated
        persistIndex(updated)
    }

    private fun addOrUpdate(project: CompareProject) {
        val without = _projects.value.filterNot { it.id == project.id }
        val merged =
            project.copy(
                modifiedAtEpochMs = currentEpochMillis(),
            )
        _projects.value = (listOf(merged) + without).take(maxItems)
        persistIndex(_projects.value)
    }

    private fun persistIndex(projects: List<CompareProject>) {
        val index = CompareProjectIndex(projects.map { it.toIndexEntry() })
        CompareProjectFileStore.writeIndex(index)
    }
}

expect fun createCompareProjectRepository(scope: CoroutineScope): CompareProjectRepository
