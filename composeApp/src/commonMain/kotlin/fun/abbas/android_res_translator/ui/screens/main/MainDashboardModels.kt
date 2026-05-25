package `fun`.abbas.android_res_translator.ui.screens.main

import `fun`.abbas.android_res_translator.core.resources.planner.TranslationWorkflowMode
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorState
import `fun`.abbas.android_res_translator.util.currentEpochMillis

data class RecentXmlProject(
    val id: String,
    val displayName: String,
    val modifiedAtEpochMs: Long,
    val progressPercent: Float,
    val translatedKeys: Int,
    val totalKeys: Int,
    val isComplete: Boolean,
    val sourceLang: String = "en",
    val targetLang: String = "zh",
    /** 磁盘上的 source.xml 绝对路径 */
    val sourcePath: String = "",
    /** 磁盘上的 result.xml 绝对路径 */
    val resultPath: String = "",
    val workflowMode: TranslationWorkflowMode = TranslationWorkflowMode.FULL,
    val targetDisplayName: String? = null,
    val hasTargetBaseline: Boolean = false,
    val targetBaselinePath: String = "",
)

fun RecentXmlProject.withEditorState(state: FileEditorState): RecentXmlProject {
    val total = state.totalCount.coerceAtLeast(1)
    val done = state.finishedCount
    return copy(
        progressPercent = if (total == 0) 0f else done.toFloat() / total.toFloat(),
        translatedKeys = done,
        totalKeys = total,
        isComplete =
            total > 0 &&
                !state.isRunning &&
                state.pendingCount == 0 &&
                state.errorCount == 0,
        modifiedAtEpochMs = currentEpochMillis(),
        sourceLang = state.sourceLang,
        targetLang = state.targetLang,
    )
}
