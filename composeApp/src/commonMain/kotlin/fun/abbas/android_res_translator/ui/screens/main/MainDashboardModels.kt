package `fun`.abbas.android_res_translator.ui.screens.main

data class RecentXmlProject(
    val id: String,
    val displayName: String,
    val modifiedAtEpochMs: Long,
    val progressPercent: Float,
    val translatedKeys: Int,
    val totalKeys: Int,
    val isComplete: Boolean,
    val sourceXml: String = "",
)
