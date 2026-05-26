package `fun`.abbas.android_res_translator.core.resources.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MinimalXlsxDecoderTest {
    @Test
    fun storedZipReader_roundTripsWriter() {
        val payload = mapOf("test.txt" to "payload".encodeToByteArray())
        val zip = StoredZipWriter.write(payload)
        assertEquals("payload", readZipArchive(zip)["test.txt"]?.decodeToString())
    }

    @Test
    fun storedZipReader_readsTwoEntries() {
        val zip =
            StoredZipWriter.write(
                mapOf(
                    "a.txt" to byteArrayOf(1),
                    "b.txt" to byteArrayOf(2),
                ),
            )
        assertEquals(2, readZipArchive(zip).size)
    }

    @Test
    fun storedZipReader_readsOoxmlLayout() {
        val names =
            listOf(
                "[Content_Types].xml",
                "_rels/.rels",
                "xl/_rels/workbook.xml.rels",
                "xl/workbook.xml",
                "xl/styles.xml",
                "xl/sharedStrings.xml",
                "xl/worksheets/sheet1.xml",
            )
        val zip = StoredZipWriter.write(names.associateWith { "<x/>".encodeToByteArray() })
        assertEquals(7, StoredZipReader.read(zip).size)
    }

    @Test
    fun storedZipReader_readsEncodedMatrixZip() {
        val bytes =
            MinimalXlsxEncoder.encode(
                StringsMatrix(
                    columnHeaders = listOf("key", "en", "zh"),
                    rows =
                        listOf(
                            StringsMatrixRow("appName", listOf("appName", "My App", "我的程序")),
                        ),
                ),
            )
        assertEquals(7, readZipArchive(bytes).size)
    }

    @Test
    fun decode_roundTripsEncoderOutput() {
        val original =
            StringsMatrix(
                columnHeaders = listOf("key", "en", "zh"),
                rows =
                    listOf(
                        StringsMatrixRow("appName", listOf("appName", "My App", "我的程序")),
                        StringsMatrixRow("greet", listOf("greet", "Hi", "你好")),
                    ),
            )
        val bytes = MinimalXlsxEncoder.encode(original)
        val decoded = MinimalXlsxDecoder.decode(bytes)
        assertEquals(original.columnHeaders, decoded.columnHeaders)
        assertEquals(2, decoded.rows.size)
        assertEquals("My App", decoded.rows.first { it.key == "appName" }.valuesByColumn[1])
        assertEquals("你好", decoded.rows.first { it.key == "greet" }.valuesByColumn[2])
    }

    @Test
    fun parseSharedStrings_readsSiElements() {
        val xml =
            """
            <sst count="2" uniqueCount="2">
              <si><t>key</t></si>
              <si><t xml:space="preserve"> hello </t></si>
            </sst>
            """.trimIndent()
        val strings = MinimalXlsxDecoder.parseSharedStrings(xml)
        assertEquals(listOf("key", " hello "), strings)
    }

    @Test
    fun columnIndexFromRef_parsesExcelColumns() {
        assertEquals(0, MinimalXlsxDecoder.columnIndexFromRef("A1"))
        assertEquals(1, MinimalXlsxDecoder.columnIndexFromRef("B2"))
        assertEquals(25, MinimalXlsxDecoder.columnIndexFromRef("Z9"))
        assertTrue(MinimalXlsxDecoder.columnIndexFromRef("AA1") >= 26)
    }
}
