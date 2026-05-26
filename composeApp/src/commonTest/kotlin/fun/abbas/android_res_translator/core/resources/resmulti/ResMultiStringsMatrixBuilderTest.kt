package `fun`.abbas.android_res_translator.core.resources.resmulti

import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiLanguageEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResMultiStringsMatrixBuilderTest {
    private val enEntry =
        ResMultiLanguageEntry(
            folderName = "values-en",
            langCode = "en",
            stringsRelativePath = "values-en/strings.xml",
        )
    private val zhEntry =
        ResMultiLanguageEntry(
            folderName = "values-zh",
            langCode = "zh",
            stringsRelativePath = "values-zh/strings.xml",
        )

    @Test
    fun buildFull_unionsKeysAcrossLanguages() {
        val enXml =
            """
            <resources>
                <string name="appName">My App</string>
                <string name="onlyEn">EN</string>
            </resources>
            """.trimIndent()
        val zhXml =
            """
            <resources>
                <string name="appName">我的程序</string>
                <string name="onlyZh">中文</string>
                <plurals name="items">
                    <item quantity="one">一项</item>
                    <item quantity="other">多项</item>
                </plurals>
            </resources>
            """.trimIndent()
        val enFlat = ResMultiStringsMatrixBuilder.parseAndFlatten(enEntry, enXml).getOrThrow()
        val zhFlat = ResMultiStringsMatrixBuilder.parseAndFlatten(zhEntry, zhXml).getOrThrow()
        val matrix =
            ResMultiStringsMatrixBuilder.buildFull(listOf(enFlat, zhFlat)).getOrThrow()

        assertEquals(listOf("key", "en", "zh"), matrix.columnHeaders)
        val appRow = matrix.rows.first { it.key == "appName" }
        assertEquals(listOf("appName", "My App", "我的程序"), appRow.valuesByColumn)
        val onlyEn = matrix.rows.first { it.key == "onlyEn" }
        assertEquals("", onlyEn.valuesByColumn[2])
        val pluralOne = matrix.rows.first { it.key == "items#one" }
        assertEquals("一项", pluralOne.valuesByColumn[2])
    }

    @Test
    fun buildSingle_includesKeyColumnAndOneLanguage() {
        val xml =
            """<resources><string name="k">v</string></resources>"""
        val flat = ResMultiStringsMatrixBuilder.parseAndFlatten(zhEntry, xml).getOrThrow()
        val matrix = ResMultiStringsMatrixBuilder.buildSingle(flat)

        assertEquals(listOf("key", "zh"), matrix.columnHeaders)
        val row = matrix.rows.single()
        assertEquals(listOf("k", "v"), row.valuesByColumn)
    }

    @Test
    fun parseAndFlatten_returnsErrorOnInvalidXml() {
        val result = ResMultiStringsMatrixBuilder.parseAndFlatten(enEntry, "<not-resources/>")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("values-en") == true)
    }

    @Test
    fun columnLabel_disambiguatesDuplicateLangCodes() {
        val a = enEntry.copy(folderName = "values-en-rUS")
        val b = enEntry.copy(folderName = "values-en-rGB", stringsRelativePath = "values-en-rGB/strings.xml")
        assertEquals("en(values-en-rUS)", ResMultiStringsMatrixBuilder.columnLabel(a, listOf(a, b)))
        assertEquals("en(values-en-rGB)", ResMultiStringsMatrixBuilder.columnLabel(b, listOf(a, b)))
    }
}
