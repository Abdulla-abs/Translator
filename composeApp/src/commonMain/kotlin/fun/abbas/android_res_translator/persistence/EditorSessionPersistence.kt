package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.ui.screens.fileeditor.EntryStatus
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorSessionSnapshot
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorState
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.XmlEntryUi
import `fun`.abbas.android_res_translator.ui.screens.main.RecentXmlProject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val SESSION_FILE = "session.json"

private val sessionJson =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

@Serializable
private data class PersistedEditorSession(
    val entries: List<PersistedXmlEntry> = emptyList(),
    val keyFilter: String = "",
    val isPaused: Boolean = false,
    val sourceLang: String? = null,
    val targetLang: String? = null,
)

@Serializable
private data class PersistedXmlEntry(
    val key: String,
    val sourceText: String,
    val targetText: String? = null,
    val statusKind: String,
    val errorMessage: String? = null,
    val translatable: Boolean = true,
)

fun TranslationProjectFileStore.sessionPath(projectId: String): String =
    "${projectDirectory(projectId)}/$SESSION_FILE"

fun TranslationProjectFileStore.writeSessionFromEditorState(
    projectId: String,
    state: FileEditorState,
) {
    val normalized =
        state.copy(
            entries =
                state.entries.map { entry ->
                    if (entry.status is EntryStatus.Translating) {
                        entry.copy(status = EntryStatus.Pending)
                    } else {
                        entry
                    }
                },
            isRunning = false,
        )
    val payload =
        PersistedEditorSession(
            entries = normalized.entries.map { it.toPersisted() },
            keyFilter = normalized.keyFilter,
            isPaused = normalized.isPaused,
            sourceLang = normalized.sourceLang,
            targetLang = normalized.targetLang,
        )
    writeTextFileAtomic(sessionPath(projectId), sessionJson.encodeToString(payload))
}

private fun XmlEntryUi.toPersisted(): PersistedXmlEntry =
    when (val s = status) {
        EntryStatus.Pending ->
            PersistedXmlEntry(key, sourceText, targetText, "pending", translatable = translatable)
        EntryStatus.Translating ->
            PersistedXmlEntry(key, sourceText, targetText, "pending", translatable = translatable)
        EntryStatus.Completed ->
            PersistedXmlEntry(key, sourceText, targetText, "completed", translatable = translatable)
        EntryStatus.Skipped ->
            PersistedXmlEntry(key, sourceText, targetText, "skipped", translatable = translatable)
        is EntryStatus.Error ->
            PersistedXmlEntry(
                key,
                sourceText,
                targetText,
                "error",
                errorMessage = s.message,
                translatable = translatable,
            )
    }

private fun PersistedXmlEntry.toUi(): XmlEntryUi =
    XmlEntryUi(
        key = key,
        sourceText = sourceText,
        targetText = targetText,
        status =
            when (statusKind) {
                "completed" -> EntryStatus.Completed
                "skipped" -> EntryStatus.Skipped
                "error" -> EntryStatus.Error(errorMessage.orEmpty())
                else -> EntryStatus.Pending
            },
        translatable = translatable,
    )

private fun loadPersistedSession(projectId: String): FileEditorSessionSnapshot? {
    val path = TranslationProjectFileStore.sessionPath(projectId)
    if (!fileExists(path)) return null
    return runCatching {
        val raw = readTextFile(path)
        val parsed = sessionJson.decodeFromString<PersistedEditorSession>(raw)
        FileEditorSessionSnapshot(
            entries = parsed.entries.map { it.toUi() },
            keyFilter = parsed.keyFilter,
            isPaused = parsed.isPaused,
        )
    }.getOrNull()
}

internal fun TranslationProjectFileStore.loadSessionSnapshotInternal(project: RecentXmlProject): FileEditorSessionSnapshot? {
    loadPersistedSession(project.id)?.let { return it }
    if (!fileExists(project.sourcePath)) return null
    val sourceXml = readTextFile(project.sourcePath)
    val resultXml =
        if (fileExists(project.resultPath)) {
            readTextFile(project.resultPath)
        } else {
            buildResultTemplate(sourceXml)
        }
    return FileEditorSessionSnapshot(
        entries = buildEntriesFromXml(sourceXml, resultXml),
    )
}
