package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.core.resources.planner.TranslationWorkflowMode
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
    val workflowMode: String = "FULL",
    val targetDisplayName: String? = null,
    val hasTargetBaseline: Boolean = false,
    val targetBaselinePath: String = "",
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

private fun String.toWorkflowMode(): TranslationWorkflowMode =
    when (uppercase()) {
        "INCREMENTAL" -> TranslationWorkflowMode.INCREMENTAL
        else -> TranslationWorkflowMode.FULL
    }

private fun TranslationWorkflowMode.toIndexValue(): String = name

fun TranslationProjectIndexEntry.toRecentXmlProject(): RecentXmlProject {
    val mode = workflowMode.toWorkflowMode()
    val baselinePath = targetBaselinePath
    return RecentXmlProject(
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
        workflowMode = mode,
        targetDisplayName = targetDisplayName,
        hasTargetBaseline = hasTargetBaseline || baselinePath.isNotBlank(),
        targetBaselinePath = baselinePath,
    )
}

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
        workflowMode = workflowMode.toIndexValue(),
        targetDisplayName = targetDisplayName,
        hasTargetBaseline = hasTargetBaseline,
        targetBaselinePath = targetBaselinePath,
    )

fun encodeIndex(index: TranslationProjectIndex): String = indexJson.encodeToString(index)

fun decodeIndex(raw: String): TranslationProjectIndex =
    runCatching { indexJson.decodeFromString<TranslationProjectIndex>(raw) }
        .getOrElse { TranslationProjectIndex() }
