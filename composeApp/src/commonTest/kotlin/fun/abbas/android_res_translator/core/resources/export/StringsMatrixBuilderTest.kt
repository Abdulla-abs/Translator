package `fun`.abbas.android_res_translator.core.resources.export

import `fun`.abbas.android_res_translator.core.resources.model.StringArrayEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.EntryStatus
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.XmlEntryUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StringsMatrixBuilderTest {
    @Test
    fun build_includesStringEntriesAndFlattenedArrays() {
        val source =
            StringResourceFile(
                strings = emptyMap(),
                stringArrays = mapOf("tabs" to StringArrayEntry("tabs", listOf("A", "B"))),
            )
        val target =
            StringResourceFile(
                stringArrays = mapOf("tabs" to StringArrayEntry("tabs", listOf("甲", "乙"))),
            )
        val entries =
            listOf(
                XmlEntryUi(
                    key = "app_name",
                    sourceText = "Hello",
                    targetText = "你好",
                    status = EntryStatus.Completed,
                ),
            )
        val matrix =
            StringsMatrixBuilder.buildForFileEditor(
                sourceFile = source,
                targetBaseline = target,
                sourceLang = "en",
                targetLang = "zh",
                entries = entries,
            )
        assertEquals(listOf("key", "en", "zh"), matrix.columnHeaders)
        val appRow = matrix.rows.first { it.key == "app_name" }
        assertEquals(listOf("app_name", "Hello", "你好"), appRow.valuesByColumn)
        val tab0 = matrix.rows.first { it.key == "tabs0" }
        assertEquals(listOf("tabs0", "A", "甲"), tab0.valuesByColumn)
        val tab1 = matrix.rows.first { it.key == "tabs1" }
        assertEquals(listOf("tabs1", "B", "乙"), tab1.valuesByColumn)
    }

    @Test
    fun build_failedEntryFallsBackToSourceText() {
        val entries =
            listOf(
                XmlEntryUi(
                    key = "greet",
                    sourceText = "Hi",
                    targetText = null,
                    status = EntryStatus.Pending,
                ),
            )
        val matrix =
            StringsMatrixBuilder.buildForFileEditor(
                sourceFile = StringResourceFile(),
                targetBaseline = null,
                sourceLang = "en",
                targetLang = "zh",
                entries = entries,
            )
        val row = matrix.rows.single()
        assertEquals("Hi", row.valuesByColumn[2])
    }
}
