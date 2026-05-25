package `fun`.abbas.android_res_translator.ui.screens.compare

data class CompareProject(
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
) {
    val isReadyToCompare: Boolean
        get() = hasLeftFile && hasRightFile
}

fun langLabelFromFileName(displayName: String): String =
    displayName
        .substringAfterLast('/')
        .substringAfterLast('\\')
        .removeSuffix(".xml")
        .removeSuffix(".XML")
        .ifBlank { displayName }
