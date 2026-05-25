package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.ui.screens.compare.CompareProject
import `fun`.abbas.android_res_translator.ui.screens.compare.langLabelFromFileName
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object CompareProjectFileStore {
    private const val INDEX_FILE = "index.json"
    private const val LEFT_FILE = "left.xml"
    private const val RIGHT_FILE = "right.xml"

    fun indexFilePath(): String = "${appCompareProjectsRoot()}/$INDEX_FILE"

    fun projectDirectory(projectId: String): String = "${appCompareProjectsRoot()}/$projectId"

    fun leftPath(projectId: String): String = "${projectDirectory(projectId)}/$LEFT_FILE"

    fun rightPath(projectId: String): String = "${projectDirectory(projectId)}/$RIGHT_FILE"

    fun readIndex(): CompareProjectIndex {
        val path = indexFilePath()
        if (!fileExists(path)) return CompareProjectIndex()
        return decodeCompareIndex(readTextFile(path))
    }

    fun writeIndex(index: CompareProjectIndex) {
        ensureDirectory(appCompareProjectsRoot())
        writeTextFileAtomic(indexFilePath(), encodeCompareIndex(index))
    }

    @OptIn(ExperimentalTime::class)
    fun createProject(displayName: String): CompareProject {
        val id = "${displayName}_${Random.nextInt(1_000_000)}"
        ensureDirectory(projectDirectory(id))
        val project =
            CompareProject(
                id = id,
                displayName = displayName,
                modifiedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                leftPath = leftPath(id),
                rightPath = rightPath(id),
            )
        val index = readIndex()
        writeIndex(
            CompareProjectIndex(
                projects = listOf(project.toIndexEntry()) + index.projects.filterNot { it.id == id },
            ),
        )
        return project
    }

    fun writeLeftFile(
        projectId: String,
        xml: String,
        displayName: String,
    ): CompareProject {
        writeTextFileAtomic(leftPath(projectId), xml)
        return updateMetaAfterUpload(
            projectId = projectId,
            side = CompareSide.LEFT,
            displayName = displayName,
        )
    }

    fun writeRightFile(
        projectId: String,
        xml: String,
        displayName: String,
    ): CompareProject {
        writeTextFileAtomic(rightPath(projectId), xml)
        return updateMetaAfterUpload(
            projectId = projectId,
            side = CompareSide.RIGHT,
            displayName = displayName,
        )
    }

    fun readLeftXml(project: CompareProject): String =
        if (project.hasLeftFile && fileExists(project.leftPath)) {
            readTextFile(project.leftPath)
        } else {
            ""
        }

    fun readRightXml(project: CompareProject): String =
        if (project.hasRightFile && fileExists(project.rightPath)) {
            readTextFile(project.rightPath)
        } else {
            ""
        }

    fun deleteProjectOnDisk(project: CompareProject) {
        deletePathRecursively(projectDirectory(project.id))
    }

    private enum class CompareSide {
        LEFT,
        RIGHT,
    }

    @OptIn(ExperimentalTime::class)
    private fun updateMetaAfterUpload(
        projectId: String,
        side: CompareSide,
        displayName: String,
    ): CompareProject {
        val index = readIndex()
        val existing = index.projects.find { it.id == projectId }?.toCompareProject()
        val label = langLabelFromFileName(displayName)
        val updated =
            when (side) {
                CompareSide.LEFT ->
                    (existing ?: CompareProject(id = projectId, displayName = displayName, modifiedAtEpochMs = 0L))
                        .copy(
                            leftDisplayName = displayName,
                            leftLangLabel = label,
                            hasLeftFile = true,
                            leftPath = leftPath(projectId),
                            modifiedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                        )
                CompareSide.RIGHT ->
                    (existing ?: CompareProject(id = projectId, displayName = displayName, modifiedAtEpochMs = 0L))
                        .copy(
                            rightDisplayName = displayName,
                            rightLangLabel = label,
                            hasRightFile = true,
                            rightPath = rightPath(projectId),
                            modifiedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                        )
            }
        val newIndex =
            CompareProjectIndex(
                projects =
                    listOf(updated.toIndexEntry()) +
                        index.projects.filterNot { it.id == projectId },
            )
        writeIndex(newIndex)
        return updated
    }
}
