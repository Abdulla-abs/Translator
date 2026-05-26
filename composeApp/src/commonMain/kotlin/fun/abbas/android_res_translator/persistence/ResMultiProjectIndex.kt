package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiInitState
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiLanguageEntry
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiProject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ResMultiLanguageEntryDto(
    val folderName: String,
    val langCode: String,
    val stringsRelativePath: String,
)

@Serializable
data class ResMultiProjectMeta(
    val id: String,
    val displayName: String,
    val createdAtEpochMs: Long,
    val modifiedAtEpochMs: Long,
    val sourceResPath: String = "",
    val initState: String = ResMultiInitState.PENDING.name,
    val languages: List<ResMultiLanguageEntryDto> = emptyList(),
    val initError: String? = null,
    val dirty: Boolean = false,
)

@Serializable
data class ResMultiProjectIndexEntry(
    val id: String,
    val displayName: String,
    val modifiedAtEpochMs: Long,
    val initState: String = ResMultiInitState.PENDING.name,
    val languageCount: Int = 0,
)

@Serializable
data class ResMultiProjectIndex(
    val projects: List<ResMultiProjectIndexEntry> = emptyList(),
)

@Serializable
data class ResMultiVersionIndexEntry(
    val id: String,
    val displayName: String,
    val createdAtEpochMs: Long,
)

@Serializable
data class ResMultiVersionIndex(
    val headId: String? = null,
    val dirty: Boolean = false,
    val versions: List<ResMultiVersionIndexEntry> = emptyList(),
)

private val json =
    Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

fun ResMultiLanguageEntryDto.toModel(): ResMultiLanguageEntry =
    ResMultiLanguageEntry(
        folderName = folderName,
        langCode = langCode,
        stringsRelativePath = stringsRelativePath,
    )

fun ResMultiLanguageEntry.toDto(): ResMultiLanguageEntryDto =
    ResMultiLanguageEntryDto(
        folderName = folderName,
        langCode = langCode,
        stringsRelativePath = stringsRelativePath,
    )

fun ResMultiProjectMeta.toProject(): ResMultiProject {
    val state = runCatching { ResMultiInitState.valueOf(initState) }.getOrDefault(ResMultiInitState.PENDING)
    return ResMultiProject(
        id = id,
        displayName = displayName,
        createdAtEpochMs = createdAtEpochMs,
        modifiedAtEpochMs = modifiedAtEpochMs,
        sourceResPath = sourceResPath,
        initState = state,
        languages = languages.map { it.toModel() },
        initError = initError,
        dirty = dirty,
    )
}

fun ResMultiProject.toMeta(): ResMultiProjectMeta =
    ResMultiProjectMeta(
        id = id,
        displayName = displayName,
        createdAtEpochMs = createdAtEpochMs,
        modifiedAtEpochMs = modifiedAtEpochMs,
        sourceResPath = sourceResPath,
        initState = initState.name,
        languages = languages.map { it.toDto() },
        initError = initError,
        dirty = dirty,
    )

fun ResMultiProject.toIndexEntry(): ResMultiProjectIndexEntry =
    ResMultiProjectIndexEntry(
        id = id,
        displayName = displayName,
        modifiedAtEpochMs = modifiedAtEpochMs,
        initState = initState.name,
        languageCount = languages.size,
    )

fun encodeResMultiIndex(index: ResMultiProjectIndex): String = json.encodeToString(index)

fun decodeResMultiIndex(raw: String): ResMultiProjectIndex =
    runCatching { json.decodeFromString<ResMultiProjectIndex>(raw) }
        .getOrElse { ResMultiProjectIndex() }

fun encodeResMultiMeta(meta: ResMultiProjectMeta): String = json.encodeToString(meta)

fun decodeResMultiMeta(raw: String): ResMultiProjectMeta =
    runCatching { json.decodeFromString<ResMultiProjectMeta>(raw) }
        .getOrElse { error("Invalid meta.json") }

fun encodeResMultiVersionIndex(index: ResMultiVersionIndex): String = json.encodeToString(index)

fun decodeResMultiVersionIndex(raw: String): ResMultiVersionIndex =
    runCatching { json.decodeFromString<ResMultiVersionIndex>(raw) }
        .getOrElse { ResMultiVersionIndex() }
