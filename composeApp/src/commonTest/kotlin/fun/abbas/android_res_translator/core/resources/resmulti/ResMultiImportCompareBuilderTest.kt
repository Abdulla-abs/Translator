package `fun`.abbas.android_res_translator.core.resources.resmulti

import `fun`.abbas.android_res_translator.core.resources.export.StringsMatrix
import `fun`.abbas.android_res_translator.core.resources.export.StringsMatrixRow
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiLanguageEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResMultiImportCompareBuilderTest {
    private val en =
        ResMultiLanguageEntry("values-en", "en", "values-en/strings.xml")
    private val zh =
        ResMultiLanguageEntry("values-zh", "zh", "values-zh/strings.xml")

    @Test
    fun build_marksDifferingCells() {
        val imported =
            StringsMatrix(
                columnHeaders = listOf("key", "en", "zh"),
                rows =
                    listOf(
                        StringsMatrixRow("appName", listOf("appName", "My App", "我的程序")),
                        StringsMatrixRow("onlyImport", listOf("onlyImport", "NEW", "")),
                    ),
            )
        val workspace =
            listOf(
                ResMultiLanguageFlat(en, mapOf("appName" to "My App"), emptyMap()),
                ResMultiLanguageFlat(zh, mapOf("appName" to "旧名称"), emptyMap()),
            )
        val matrix =
            ResMultiImportCompareBuilder.build(
                imported = imported,
                workspaceFlats = workspace,
                languages = listOf(en, zh),
            )
        assertEquals(2, matrix.columns.size)
        val appRow = matrix.rows.first { it.key == "appName" }
        assertFalse(appRow.cells[0].hasDifference)
        assertTrue(appRow.cells[1].hasDifference)
        assertEquals(2, matrix.diffCellCount)
    }

    @Test
    fun build_skipsUnknownLanguageColumns() {
        val imported =
            StringsMatrix(
                columnHeaders = listOf("key", "en", "jp"),
                rows = listOf(StringsMatrixRow("k", listOf("k", "v", "x"))),
            )
        val workspace = listOf(ResMultiLanguageFlat(en, mapOf("k" to "v"), emptyMap()))
        val matrix =
            ResMultiImportCompareBuilder.build(
                imported = imported,
                workspaceFlats = workspace,
                languages = listOf(en),
            )
        assertEquals(listOf("jp"), matrix.skippedHeaders)
        assertEquals(1, matrix.columns.size)
    }

    @Test
    fun matchLanguageHeader_parsesDisambiguatedLabel() {
        val a = en.copy(folderName = "values-en-rUS")
        val b = en.copy(folderName = "values-en-rGB", stringsRelativePath = "values-en-rGB/strings.xml")
        val header = ResMultiStringsMatrixBuilder.columnLabel(b, listOf(a, b))
        assertEquals(b, ResMultiImportCompareBuilder.matchLanguageHeader(header, listOf(a, b)))
    }
}
