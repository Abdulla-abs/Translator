package `fun`.abbas.android_res_translator.ui.screens.resmulti

enum class ResMultiInitState {
    PENDING,
    INITIALIZING,
    READY,
    FAILED,
}

data class ResMultiLanguageEntry(
    val folderName: String,
    val langCode: String,
    val stringsRelativePath: String,
)

data class ResMultiProject(
    val id: String,
    val displayName: String,
    val createdAtEpochMs: Long,
    val modifiedAtEpochMs: Long,
    val sourceResPath: String = "",
    val initState: ResMultiInitState = ResMultiInitState.PENDING,
    val languages: List<ResMultiLanguageEntry> = emptyList(),
    val initError: String? = null,
    val dirty: Boolean = false,
) {
    val isReady: Boolean
        get() = initState == ResMultiInitState.READY
}
