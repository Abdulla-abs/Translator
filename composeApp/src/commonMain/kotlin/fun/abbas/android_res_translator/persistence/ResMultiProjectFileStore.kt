package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.core.resources.resmulti.ResMultiProjectScanner
import `fun`.abbas.android_res_translator.persistence.ResMultiProjectFileStore
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiInitState
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiProject
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object ResMultiProjectFileStore {
    private const val INDEX_FILE = "index.json"
    private const val META_FILE = "meta.json"
    private const val WORKSPACE_DIR = "workspace"
    private const val VERSIONS_DIR = "versions"
    private const val VERSION_INDEX_FILE = "versions-index.json"

    fun indexFilePath(): String = "${appResMultiProjectsRoot()}/$INDEX_FILE"

    fun projectDirectory(projectId: String): String = "${appResMultiProjectsRoot()}/$projectId"

    fun metaPath(projectId: String): String = "${projectDirectory(projectId)}/$META_FILE"

    fun workspacePath(projectId: String): String = "${projectDirectory(projectId)}/$WORKSPACE_DIR"

    fun versionsRoot(projectId: String): String = "${projectDirectory(projectId)}/$VERSIONS_DIR"

    fun versionDirectory(
        projectId: String,
        versionId: String,
    ): String = "${versionsRoot(projectId)}/$versionId"

    fun versionIndexPath(projectId: String): String = "${projectDirectory(projectId)}/$VERSION_INDEX_FILE"

    fun readIndex(): ResMultiProjectIndex {
        val path = indexFilePath()
        if (!fileExists(path)) return ResMultiProjectIndex()
        return decodeResMultiIndex(readTextFile(path))
    }

    fun writeIndex(index: ResMultiProjectIndex) {
        ensureDirectory(appResMultiProjectsRoot())
        writeTextFileAtomic(indexFilePath(), encodeResMultiIndex(index))
    }

    fun readMeta(projectId: String): ResMultiProjectMeta? {
        val path = metaPath(projectId)
        if (!fileExists(path)) return null
        return decodeResMultiMeta(readTextFile(path))
    }

    fun writeMeta(meta: ResMultiProjectMeta) {
        ensureDirectory(projectDirectory(meta.id))
        writeTextFileAtomic(metaPath(meta.id), encodeResMultiMeta(meta))
    }

    @OptIn(ExperimentalTime::class)
    fun createProject(displayName: String): ResMultiProject {
        val id = "${displayName}_${Random.nextInt(1_000_000)}"
        ensureDirectory(projectDirectory(id))
        ensureDirectory(workspacePath(id))
        val now = Clock.System.now().toEpochMilliseconds()
        val meta =
            ResMultiProjectMeta(
                id = id,
                displayName = displayName,
                createdAtEpochMs = now,
                modifiedAtEpochMs = now,
                initState = ResMultiInitState.PENDING.name,
            )
        writeMeta(meta)
        val index = readIndex()
        writeIndex(
            ResMultiProjectIndex(
                projects = listOf(meta.toProject().toIndexEntry()) + index.projects.filterNot { it.id == id },
            ),
        )
        return meta.toProject()
    }

    @OptIn(ExperimentalTime::class)
    fun initializeFromSource(
        projectId: String,
        sourceResPath: String,
    ): ResMultiProject {
        val existing = readMeta(projectId) ?: error("Project not found: $projectId")
        val now = Clock.System.now().toEpochMilliseconds()
        val initializing =
            existing.copy(
                initState = ResMultiInitState.INITIALIZING.name,
                modifiedAtEpochMs = now,
                sourceResPath = sourceResPath,
                initError = null,
            )
        writeMeta(initializing)

        val workspace = workspacePath(projectId)
        val cloned = cloneResDirectoryToWorkspace(sourceResPath, workspace)
        if (!cloned) {
            val failed =
                initializing.copy(
                    initState = ResMultiInitState.FAILED.name,
                    initError = "Failed to clone res directory",
                    modifiedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                )
            writeMeta(failed)
            syncIndexEntry(failed.toProject())
            return failed.toProject()
        }

        val scan = ResMultiProjectScanner.scanWorkspace(workspace)
        if (scan.languages.isEmpty()) {
            val failed =
                initializing.copy(
                    initState = ResMultiInitState.FAILED.name,
                    initError = scan.warnings.lastOrNull() ?: "No language resources found",
                    modifiedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                )
            writeMeta(failed)
            syncIndexEntry(failed.toProject())
            return failed.toProject()
        }

        val ready =
            initializing.copy(
                initState = ResMultiInitState.READY.name,
                languages = scan.languages.map { it.toDto() },
                initError = null,
                modifiedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                dirty = false,
            )
        writeMeta(ready)
        ResProjectVersionStore.createInitSnapshot(projectId)
        syncIndexEntry(ready.toProject())
        return ready.toProject()
    }

    fun deleteProjectOnDisk(projectId: String) {
        deletePathRecursively(projectDirectory(projectId))
    }

    private fun syncIndexEntry(project: ResMultiProject) {
        val index = readIndex()
        val others = index.projects.filterNot { it.id == project.id }
        writeIndex(ResMultiProjectIndex(projects = listOf(project.toIndexEntry()) + others))
    }
}
