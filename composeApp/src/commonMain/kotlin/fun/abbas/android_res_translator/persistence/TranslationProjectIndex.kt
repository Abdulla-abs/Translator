package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.ui.screens.main.RecentXmlProject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TranslationProjectIndexEntry(
    val id: String,
    val displayName: String,
    val modifiedAtEpochMs: Long,
    val progressPercent: Float,
    val translatedKeys: Int,
    val totalKeys: Int,
    val isComplete: Boolean,
    val sourceLang: String,
    val targetLang: String,
    val sourcePath: String,
    val resultPath: String,
)

@Serializable
data class TranslationProjectIndex(
    val projects: List<TranslationProjectIndexEntry> = emptyList(),
)

private val indexJson =
    Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

fun TranslationProjectIndexEntry.toRecentXmlProject(): RecentXmlProject =
    RecentXmlProject(
        id = id,
        displayName = displayName,
        modifiedAtEpochMs = modifiedAtEpochMs,
        progressPercent = progressPercent,
        translatedKeys = translatedKeys,
        totalKeys = totalKeys,
        isComplete = isComplete,
        sourceLang = sourceLang,
        targetLang = targetLang,
        sourcePath = sourcePath,
        resultPath = resultPath,
    )

fun RecentXmlProject.toIndexEntry(): TranslationProjectIndexEntry =
    TranslationProjectIndexEntry(
        id = id,
        displayName = displayName,
        modifiedAtEpochMs = modifiedAtEpochMs,
        progressPercent = progressPercent,
        translatedKeys = translatedKeys,
        totalKeys = totalKeys,
        isComplete = isComplete,
        sourceLang = sourceLang,
        targetLang = targetLang,
        sourcePath = sourcePath,
        resultPath = resultPath,
    )

fun encodeIndex(index: TranslationProjectIndex): String = indexJson.encodeToString(index)

fun decodeIndex(raw: String): TranslationProjectIndex =
    runCatching { indexJson.decodeFromString<TranslationProjectIndex>(raw) }
        .getOrElse { TranslationProjectIndex() }
