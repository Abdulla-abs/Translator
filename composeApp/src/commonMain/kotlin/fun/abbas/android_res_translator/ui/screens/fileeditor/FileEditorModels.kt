package `fun`.abbas.android_res_translator.ui.screens.fileeditor

sealed interface EntryStatus {
    data object Pending : EntryStatus

    data object Translating : EntryStatus

    data object Completed : EntryStatus

    /** 增量模式：目标已有非空译文，不调用翻译 API。 */
    data object Skipped : EntryStatus

    data class Error(
        val message: String,
    ) : EntryStatus
}

data class XmlEntryUi(
    val key: String,
    val sourceText: String,
    val targetText: String? = null,
    val status: EntryStatus = EntryStatus.Pending,
    val translatable: Boolean = true,
)

/** 可持久化的编辑器会话，用于首页卡片与再次进入编辑器时恢复。 */
data class FileEditorSessionSnapshot(
    val entries: List<XmlEntryUi>,
    val keyFilter: String = "",
    val isPaused: Boolean = false,
)

fun FileEditorState.toSessionSnapshot(): FileEditorSessionSnapshot =
    FileEditorSessionSnapshot(
        entries = entries,
        keyFilter = keyFilter,
        isPaused = isPaused,
    )

data class FileEditorState(
    val fileName: String = "strings.xml",
    val filePath: String = "/src/commonMain/resources/",
    val sourceLang: String = "en",
    val targetLang: String = "zh",
    val entries: List<XmlEntryUi> = emptyList(),
    val keyFilter: String = "",
    val isPaused: Boolean = false,
    val isRunning: Boolean = false,
    val exportMessage: String? = null,
) {
    val translatableEntries: List<XmlEntryUi>
        get() = entries.filter { it.translatable }

    val totalCount: Int
        get() = translatableEntries.size

    val completedCount: Int
        get() = translatableEntries.count { it.status is EntryStatus.Completed }

    /** 无需再译的条目（API 已译完或增量跳过）。 */
    val finishedCount: Int
        get() =
            translatableEntries.count {
                it.status is EntryStatus.Completed || it.status is EntryStatus.Skipped
            }

    val pendingCount: Int
        get() = translatableEntries.count { it.status is EntryStatus.Pending }

    val skippedCount: Int
        get() = translatableEntries.count { it.status is EntryStatus.Skipped }

    val errorCount: Int
        get() = translatableEntries.count { it.status is EntryStatus.Error }

    val progressPercent: Float
        get() = if (totalCount == 0) 0f else finishedCount.toFloat() / totalCount.toFloat()

    /** 无待译/错误且未在运行时，可导出（含增量已跳过条目）。 */
    val isExportReady: Boolean
        get() = totalCount > 0 && errorCount == 0 && pendingCount == 0 && !isRunning

    val filteredEntries: List<XmlEntryUi>
        get() {
            val q = keyFilter.trim().lowercase()
            if (q.isBlank()) return entries
            return entries.filter { it.key.lowercase().contains(q) }
        }
}
