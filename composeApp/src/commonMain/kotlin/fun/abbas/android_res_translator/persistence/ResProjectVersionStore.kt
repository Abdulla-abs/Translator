package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.core.resources.resmulti.ResMultiProjectScanner
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object ResProjectVersionStore {
    private const val INIT_VERSION_ID = "v0001-init"
    private const val INIT_VERSION_NAME = "init"
    private const val COUNTDOWN_DELETE_SECONDS = 5

    val cascadeDeleteCountdownSeconds: Int = COUNTDOWN_DELETE_SECONDS

    @OptIn(ExperimentalTime::class)
    fun createInitSnapshot(projectId: String) {
        val workspace = ResMultiProjectFileStore.workspacePath(projectId)
        val versionDir = ResMultiProjectFileStore.versionDirectory(projectId, INIT_VERSION_ID)
        ensureDirectory(ResMultiProjectFileStore.versionsRoot(projectId))
        if (fileExists(versionDir)) {
            deletePathRecursively(versionDir)
        }
        check(copyDirectoryRecursively(workspace, versionDir)) {
            "Failed to create init version snapshot for $projectId"
        }
        val now = Clock.System.now().toEpochMilliseconds()
        writeVersionIndex(
            projectId,
            ResMultiVersionIndex(
                headId = INIT_VERSION_ID,
                dirty = false,
                versions =
                    listOf(
                        ResMultiVersionIndexEntry(
                            id = INIT_VERSION_ID,
                            displayName = INIT_VERSION_NAME,
                            createdAtEpochMs = now,
                        ),
                    ),
            ),
        )
        syncMetaDirty(projectId, dirty = false)
    }

    fun readVersionIndex(projectId: String): ResMultiVersionIndex {
        val path = ResMultiProjectFileStore.versionIndexPath(projectId)
        if (!fileExists(path)) return ResMultiVersionIndex()
        return decodeResMultiVersionIndex(readTextFile(path))
    }

    fun writeVersionIndex(
        projectId: String,
        index: ResMultiVersionIndex,
    ) {
        writeTextFileAtomic(
            ResMultiProjectFileStore.versionIndexPath(projectId),
            encodeResMultiVersionIndex(index),
        )
    }

    fun isDirty(projectId: String): Boolean {
        val meta = ResMultiProjectFileStore.readMeta(projectId)
        if (meta?.dirty == true) return true
        return readVersionIndex(projectId).dirty
    }

    @OptIn(ExperimentalTime::class)
    fun pushVersion(
        projectId: String,
        displayName: String,
    ): Result<ResMultiVersionIndex> {
        val label = displayName.trim()
        if (label.isEmpty()) {
            return Result.failure(IllegalArgumentException("Version name cannot be empty"))
        }
        val workspace = ResMultiProjectFileStore.workspacePath(projectId)
        if (!isDirectoryPath(workspace)) {
            return Result.failure(IllegalStateException("Workspace is missing"))
        }
        val index = readVersionIndex(projectId)
        if (index.versions.isEmpty()) {
            return Result.failure(IllegalStateException("No version history"))
        }
        val versionId = allocateVersionId(index.versions, label)
        val dest = ResMultiProjectFileStore.versionDirectory(projectId, versionId)
        if (fileExists(dest)) {
            deletePathRecursively(dest)
        }
        if (!copyDirectoryRecursively(workspace, dest)) {
            return Result.failure(IllegalStateException("Failed to snapshot workspace"))
        }
        val now = Clock.System.now().toEpochMilliseconds()
        val entry =
            ResMultiVersionIndexEntry(
                id = versionId,
                displayName = label,
                createdAtEpochMs = now,
            )
        val updated =
            index.copy(
                versions = index.versions + entry,
                headId = versionId,
                dirty = false,
            )
        writeVersionIndex(projectId, updated)
        syncMetaDirty(projectId, dirty = false)
        return Result.success(updated)
    }

    fun restoreToVersion(
        projectId: String,
        versionId: String,
    ): Result<Unit> {
        val index = readVersionIndex(projectId)
        if (index.versions.none { it.id == versionId }) {
            return Result.failure(IllegalArgumentException("Unknown version: $versionId"))
        }
        val snapshot = ResMultiProjectFileStore.versionDirectory(projectId, versionId)
        if (!isDirectoryPath(snapshot)) {
            return Result.failure(IllegalStateException("Snapshot missing on disk: $versionId"))
        }
        val workspace = ResMultiProjectFileStore.workspacePath(projectId)
        if (fileExists(workspace)) {
            deletePathRecursively(workspace)
        }
        ensureDirectory(workspace)
        if (!copyDirectoryRecursively(snapshot, workspace)) {
            return Result.failure(IllegalStateException("Failed to restore workspace"))
        }
        writeVersionIndex(
            projectId,
            index.copy(
                headId = versionId,
                dirty = false,
            ),
        )
        syncMetaDirty(projectId, dirty = false)
        refreshLanguagesFromWorkspace(projectId)
        return Result.success(Unit)
    }

    fun deleteVersionAndSuccessors(
        projectId: String,
        versionId: String,
    ): Result<ResMultiVersionIndex> {
        val index = readVersionIndex(projectId)
        val position = index.versions.indexOfFirst { it.id == versionId }
        if (position < 0) {
            return Result.failure(IllegalArgumentException("Unknown version: $versionId"))
        }
        if (index.versions.size == 1) {
            return Result.failure(IllegalStateException("Cannot delete the only version"))
        }
        val toRemove = index.versions.drop(position)
        for (entry in toRemove) {
            val dir = ResMultiProjectFileStore.versionDirectory(projectId, entry.id)
            if (fileExists(dir)) {
                deletePathRecursively(dir)
            }
        }
        val remaining = index.versions.take(position)
        val newHead = remaining.lastOrNull()?.id
        val updated =
            index.copy(
                versions = remaining,
                headId = newHead,
            )
        writeVersionIndex(projectId, updated)
        return Result.success(updated)
    }

    fun requiresCascadeDeleteCountdown(
        index: ResMultiVersionIndex,
        versionId: String,
    ): Boolean {
        val position = index.versions.indexOfFirst { it.id == versionId }
        if (position < 0) return false
        return position < index.versions.lastIndex
    }

    internal fun allocateVersionId(
        existing: List<ResMultiVersionIndexEntry>,
        displayName: String,
    ): String {
        val nextNumber =
            (existing.mapNotNull { parseVersionSequence(it.id) }.maxOrNull() ?: 0) + 1
        val slug = sanitizeVersionSlug(displayName)
        return "v${nextNumber.toString().padStart(4, '0')}-$slug"
    }

    internal fun parseVersionSequence(versionId: String): Int? {
        if (!versionId.startsWith('v')) return null
        val dash = versionId.indexOf('-')
        val numeric =
            if (dash > 1) {
                versionId.substring(1, dash)
            } else {
                versionId.drop(1)
            }
        return numeric.toIntOrNull()
    }

    internal fun sanitizeVersionSlug(name: String): String {
        val trimmed =
            name
                .trim()
                .replace(Regex("""[\\/:*?"<>|]"""), "-")
                .replace(Regex("""\s+"""), "-")
                .trim('-')
        return trimmed.ifBlank { "snapshot" }
    }

    @OptIn(ExperimentalTime::class)
    private fun syncMetaDirty(
        projectId: String,
        dirty: Boolean,
    ) {
        val meta = ResMultiProjectFileStore.readMeta(projectId) ?: return
        ResMultiProjectFileStore.writeMeta(
            meta.copy(
                dirty = dirty,
                modifiedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
            ),
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun refreshLanguagesFromWorkspace(projectId: String) {
        val meta = ResMultiProjectFileStore.readMeta(projectId) ?: return
        val scan = ResMultiProjectScanner.scanWorkspace(ResMultiProjectFileStore.workspacePath(projectId))
        ResMultiProjectFileStore.writeMeta(
            meta.copy(
                languages = scan.languages.map { it.toDto() },
                modifiedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
            ),
        )
    }
}
