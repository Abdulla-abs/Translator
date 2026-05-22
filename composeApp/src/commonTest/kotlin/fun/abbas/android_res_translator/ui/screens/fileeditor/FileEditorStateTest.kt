package `fun`.abbas.android_res_translator.ui.screens.fileeditor

import kotlin.test.Test
import kotlin.test.assertEquals

class FileEditorStateTest {
    @Test
    fun progressPercent_countsSkippedAsFinished() {
        val state =
            FileEditorState(
                entries =
                    listOf(
                        XmlEntryUi("k1", "Hello", "Hi", EntryStatus.Skipped),
                        XmlEntryUi("k2", "World", "世界", EntryStatus.Completed),
                    ),
            )
        assertEquals(2, state.finishedCount)
        assertEquals(1f, state.progressPercent)
        assertEquals(true, state.isExportReady)
    }

    @Test
    fun progressPercent_partialWithSkipped() {
        val state =
            FileEditorState(
                entries =
                    listOf(
                        XmlEntryUi("k1", "A", "已译", EntryStatus.Skipped),
                        XmlEntryUi("k2", "B", null, EntryStatus.Pending),
                    ),
            )
        assertEquals(0.5f, state.progressPercent)
    }
}
