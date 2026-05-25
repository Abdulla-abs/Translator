package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.ui.screens.compare.CompareProject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CompareProjectIndexEntry(
    val id: String,
    val displayName: String,
    val modifiedAtEpochMs: Long,
    val leftDisplayName: String? = null,
    val rightDisplayName: String? = null,
    val leftLangLabel: String = "",
    val rightLangLabel: String = "",
    val hasLeftFile: Boolean = false,
    val hasRightFile: Boolean = false,
    val leftPath: String = "",
    val rightPath: String = "",
)

@Serializable
data class CompareProjectIndex(
    val projects: List<CompareProjectIndexEntry> = emptyList(),
)

private val indexJson =
    Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

fun CompareProjectIndexEntry.toCompareProject(): CompareProject =
    CompareProject(
        id = id,
        displayName = displayName,
        modifiedAtEpochMs = modifiedAtEpochMs,
        leftDisplayName = leftDisplayName,
        rightDisplayName = rightDisplayName,
        leftLangLabel = leftLangLabel,
        rightLangLabel = rightLangLabel,
        hasLeftFile = hasLeftFile,
        hasRightFile = hasRightFile,
        leftPath = leftPath,
        rightPath = rightPath,
    )

fun CompareProject.toIndexEntry(): CompareProjectIndexEntry =
    CompareProjectIndexEntry(
        id = id,
        displayName = displayName,
        modifiedAtEpochMs = modifiedAtEpochMs,
        leftDisplayName = leftDisplayName,
        rightDisplayName = rightDisplayName,
        leftLangLabel = leftLangLabel,
        rightLangLabel = rightLangLabel,
        hasLeftFile = hasLeftFile,
        hasRightFile = hasRightFile,
        leftPath = leftPath,
        rightPath = rightPath,
    )

fun encodeCompareIndex(index: CompareProjectIndex): String = indexJson.encodeToString(index)

fun decodeCompareIndex(raw: String): CompareProjectIndex =
    runCatching { indexJson.decodeFromString<CompareProjectIndex>(raw) }
        .getOrElse { CompareProjectIndex() }
