package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.core.resources.model.StringEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import `fun`.abbas.android_res_translator.core.resources.planner.IncrementalTranslationPlanner
import `fun`.abbas.android_res_translator.core.resources.planner.PlannedEntryAction
import `fun`.abbas.android_res_translator.core.resources.planner.TranslationWorkflowMode
import `fun`.abbas.android_res_translator.core.resources.xml.StringsXmlCodec
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.EntryStatus
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorSessionSnapshot
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorState
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.XmlEntryUi
import `fun`.abbas.android_res_translator.ui.screens.main.RecentXmlProject
import `fun`.abbas.android_res_translator.ui.screens.main.countTranslatableKeys
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object TranslationProjectFileStore {
    private const val SOURCE_FILE = "source.xml"
    private const val RESULT_FILE = "result.xml"
    private const val TARGET_BASELINE_FILE = "target-baseline.xml"
    private const val INDEX_FILE = "index.json"

    fun indexFilePath(): String = "${appTranslationProjectsRoot()}/$INDEX_FILE"

    fun projectDirectory(projectId: String): String = "${appTranslationProjectsRoot()}/$projectId"

    fun sourcePath(projectId: String): String = "${projectDirectory(projectId)}/$SOURCE_FILE"

    fun resultPath(projectId: String): String = "${projectDirectory(projectId)}/$RESULT_FILE"

    fun targetBaselinePath(projectId: String): String = "${projectDirectory(projectId)}/$TARGET_BASELINE_FILE"

    fun writeTargetBaseline(
        projectId: String,
        xml: String,
    ) {
        writeTextFileAtomic(targetBaselinePath(projectId), xml)
    }

    fun readTargetBaseline(project: RecentXmlProject): String =
        if (project.targetBaselinePath.isNotBlank() && fileExists(project.targetBaselinePath)) {
            readTextFile(project.targetBaselinePath)
        } else {
            ""
        }

    fun readIndex(): TranslationProjectIndex {
        val path = indexFilePath()
        if (!fileExists(path)) return TranslationProjectIndex()
        return decodeIndex(readTextFile(path))
    }

    fun writeIndex(index: TranslationProjectIndex) {
        ensureDirectory(appTranslationProjectsRoot())
        writeTextFileAtomic(indexFilePath(), encodeIndex(index))
    }

    @OptIn(ExperimentalTime::class)
    fun createIncrementalProjectFromSource(
        sourceXml: String,
        displayName: String,
        sourceLang: String,
        targetLang: String,
    ): RecentXmlProject {
        val id = "${displayName}_${Random.nextInt(1_000_000)}"
        val dir = projectDirectory(id)
        ensureDirectory(dir)
        val source = sourcePath(id)
        val result = resultPath(id)
        writeTextFileAtomic(source, sourceXml)
        writeTextFileAtomic(result, buildResultTemplate(sourceXml))
        val total = countTranslatableKeys(sourceXml).coerceAtLeast(1)
        return RecentXmlProject(
            id = id,
            displayName = displayName,
            modifiedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
            progressPercent = 0f,
            translatedKeys = 0,
            totalKeys = total,
            isComplete = false,
            sourceLang = sourceLang,
            targetLang = targetLang,
            sourcePath = source,
            resultPath = result,
            workflowMode = TranslationWorkflowMode.INCREMENTAL,
            targetDisplayName = null,
            hasTargetBaseline = false,
            targetBaselinePath = "",
        )
    }

    @OptIn(ExperimentalTime::class)
    fun attachIncrementalTarget(
        project: RecentXmlProject,
        targetXml: String,
        targetDisplayName: String,
    ): RecentXmlProject {
        val targetPath = targetBaselinePath(project.id)
        writeTextFileAtomic(targetPath, targetXml)
        writeTextFileAtomic(project.resultPath, targetXml)
        val sourceXml = readSourceXml(project)
        val total =
            IncrementalTranslationPlanner
                .plan(
                    StringsXmlCodec.parse(sourceXml),
                    StringsXmlCodec.parse(targetXml),
                    forceTranslation = false,
                ).count { it.action == PlannedEntryAction.TRANSLATE }
                .coerceAtLeast(1)
        return project.copy(
            targetDisplayName = targetDisplayName,
            hasTargetBaseline = true,
            targetBaselinePath = targetPath,
            totalKeys = total,
            translatedKeys = 0,
            progressPercent = 0f,
            isComplete = false,
            modifiedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
        )
    }

    @OptIn(ExperimentalTime::class)
    fun createIncrementalProject(
        sourceXml: String,
        targetXml: String,
        sourceDisplayName: String,
        targetDisplayName: String,
        sourceLang: String,
        targetLang: String,
    ): RecentXmlProject {
        val id = "${sourceDisplayName}_${Random.nextInt(1_000_000)}"
        val dir = projectDirectory(id)
        ensureDirectory(dir)
        val source = sourcePath(id)
        val targetPath = targetBaselinePath(id)
        val result = resultPath(id)
        writeTextFileAtomic(source, sourceXml)
        writeTextFileAtomic(targetPath, targetXml)
        writeTextFileAtomic(result, targetXml)
        return attachIncrementalTarget(
            RecentXmlProject(
                id = id,
                displayName = sourceDisplayName,
                modifiedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                progressPercent = 0f,
                translatedKeys = 0,
                totalKeys = 1,
                isComplete = false,
                sourceLang = sourceLang,
                targetLang = targetLang,
                sourcePath = source,
                resultPath = result,
                workflowMode = TranslationWorkflowMode.INCREMENTAL,
            ),
            targetXml = targetXml,
            targetDisplayName = targetDisplayName,
        )
    }

    @OptIn(ExperimentalTime::class)
    fun createFullProject(
        sourceXml: String,
        displayName: String,
        sourceLang: String,
        targetLang: String,
    ): RecentXmlProject =
        createProjectFromUpload(
            sourceXml = sourceXml,
            displayName = displayName,
            sourceLang = sourceLang,
            targetLang = targetLang,
        ).copy(
            workflowMode = TranslationWorkflowMode.FULL,
            targetDisplayName = null,
            hasTargetBaseline = false,
            targetBaselinePath = "",
        )

    @OptIn(ExperimentalTime::class)
    fun createProjectFromUpload(
        sourceXml: String,
        displayName: String,
        sourceLang: String,
        targetLang: String,
    ): RecentXmlProject {
        val id = "${displayName}_${Random.nextInt(1_000_000)}"
        val dir = projectDirectory(id)
        ensureDirectory(dir)
        val source = sourcePath(id)
        val result = resultPath(id)
        writeTextFileAtomic(source, sourceXml)
        writeTextFileAtomic(result, buildResultTemplate(sourceXml))
        val total = countTranslatableKeys(sourceXml).coerceAtLeast(1)
        return RecentXmlProject(
            id = id,
            displayName = displayName,
            modifiedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
            progressPercent = 0f,
            translatedKeys = 0,
            totalKeys = total,
            isComplete = false,
            sourceLang = sourceLang,
            targetLang = targetLang,
            sourcePath = source,
            resultPath = result,
        )
    }

    fun readSourceXml(project: RecentXmlProject): String = readTextFile(project.sourcePath)

    fun loadSessionSnapshot(project: RecentXmlProject): FileEditorSessionSnapshot? =
        loadSessionSnapshotInternal(project)

    fun writeResultFromEditorState(
        resultPath: String,
        sourceXml: String,
        state: FileEditorState,
    ) {
        val parsed = StringsXmlCodec.parse(sourceXml)
        val merged = mergeTranslatedStrings(parsed, state)
        writeTextFileAtomic(resultPath, StringsXmlCodec.serialize(merged))
        projectIdFromResultPath(resultPath)?.let { projectId ->
            writeSessionFromEditorState(projectId, state)
        }
    }

    fun deleteProjectOnDisk(project: RecentXmlProject) {
        deletePathRecursively(projectDirectory(project.id))
    }

    fun projectIdFromResultPath(resultPath: String): String? {
        val suffix = "/$RESULT_FILE"
        if (!resultPath.endsWith(suffix)) return null
        return resultPath.removeSuffix(suffix).substringAfterLast('/').takeIf { it.isNotEmpty() }
    }

    /** 可译条目译文置空，非 translatable 保留原文，作为 result 初始模板。 */
    fun buildResultTemplate(sourceXml: String): String {
        val source = StringsXmlCodec.parse(sourceXml)
        val templateStrings =
            source.strings.mapValues { (_, entry) ->
                if (entry.translatable) {
                    entry.copy(value = "")
                } else {
                    entry
                }
            }
        val templateArrays =
            source.stringArrays.mapValues { (_, array) ->
                if (array.translatable) {
                    array.copy(items = array.items.map { "" })
                } else {
                    array
                }
            }
        return StringsXmlCodec.serialize(
            source.copy(strings = templateStrings, stringArrays = templateArrays),
        )
    }

    fun buildEntriesFromXml(
        sourceXml: String,
        resultXml: String,
    ): List<XmlEntryUi> {
        val source = StringsXmlCodec.parse(sourceXml)
        val result = StringsXmlCodec.parse(resultXml)
        return source.strings.values.map { entry ->
            val translated = result.strings[entry.name]?.value.orEmpty()
            val status =
                when {
                    !entry.translatable -> EntryStatus.Completed
                    translated.isNotBlank() && translated != entry.value -> EntryStatus.Completed
                    else -> EntryStatus.Pending
                }
            val isRealTranslation =
                entry.translatable &&
                    translated.isNotBlank() &&
                    translated != entry.value
            XmlEntryUi(
                key = entry.name,
                sourceText = entry.value,
                targetText = if (isRealTranslation) translated else null,
                status = status,
                translatable = entry.translatable,
            )
        }
    }

    private fun mergeTranslatedStrings(
        parsed: StringResourceFile,
        state: FileEditorState,
    ): StringResourceFile {
        val updatedStrings =
            parsed.strings.mapValues { (_, entry) ->
                val ui = state.entries.find { it.key == entry.name }
                val text =
                    when {
                        !entry.translatable -> entry.value
                        ui?.status is EntryStatus.Completed && ui.targetText?.isNotBlank() == true ->
                            ui.targetText.orEmpty()
                        ui?.status is EntryStatus.Skipped && ui.targetText?.isNotBlank() == true ->
                            ui.targetText.orEmpty()
                        else -> ""
                    }
                entry.copy(value = text)
            }
        return parsed.copy(strings = updatedStrings)
    }
}
