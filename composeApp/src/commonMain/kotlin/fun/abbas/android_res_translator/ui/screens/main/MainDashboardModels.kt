package `fun`.abbas.android_res_translator.ui.screens.main

import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorSessionSnapshot
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorState
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.toSessionSnapshot

data class RecentXmlProject(
    val id: String,
    val displayName: String,
    val modifiedAtEpochMs: Long,
    val progressPercent: Float,
    val translatedKeys: Int,
    val totalKeys: Int,
    val isComplete: Boolean,
    val sourceXml: String = "",
    val editorSession: FileEditorSessionSnapshot? = null,
)

fun RecentXmlProject.withEditorState(state: FileEditorState): RecentXmlProject {
    val total = state.totalCount.coerceAtLeast(1)
    val done = state.completedCount
    return copy(
        progressPercent = if (total == 0) 0f else done.toFloat() / total.toFloat(),
        translatedKeys = done,
        totalKeys = total,
        isComplete = total > 0 && done >= total,
        modifiedAtEpochMs = System.currentTimeMillis(),
        editorSession = state.toSessionSnapshot(),
    )
}
