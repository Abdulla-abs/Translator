package `fun`.abbas.android_res_translator.core.resources.export

import java.io.File
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class StoredZipWriterTest {
    @Test
    fun encode_xlsxUsesStandardZip() {
        val bytes =
            MinimalXlsxEncoder.encode(
                StringsMatrix(
                    columnHeaders = listOf("key", "en", "zh"),
                    rows = listOf(StringsMatrixRow("k", listOf("k", "a", "b"))),
                ),
            )
        val file = File.createTempFile("xlsx-", ".xlsx")
        file.writeBytes(bytes)
        try {
            ZipFile(file).use { zip ->
                assertEquals(7, zip.size())
                zip.getEntry("_rels/.rels")?.let { entry ->
                    zip.getInputStream(entry).use { input ->
                        val text = input.readBytes().decodeToString()
                        assertTrue(text.contains("officeDocument"))
                    }
                } ?: fail("missing _rels/.rels")
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun write_zipReadableByJavaZipInputStream() {
        val matrix =
            StringsMatrix(
                columnHeaders = listOf("key", "en", "zh"),
                rows =
                    listOf(
                        StringsMatrixRow("app_name", listOf("app_name", "Hello", "你好")),
                    ),
            )
        val bytes = MinimalXlsxEncoder.encode(matrix)
        val names = mutableListOf<String>()
        ZipInputStream(bytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                names += entry.name
                val read = zis.readBytes()
                assertTrue(read.isNotEmpty(), "empty entry: ${entry.name}")
                entry = zis.nextEntry
            }
        }
        assertEquals(7, names.size)
        assertTrue("[Content_Types].xml" in names)
        assertTrue("xl/worksheets/sheet1.xml" in names)
    }

    @Test
    fun write_storedZipRoundTrip() {
        val payload = "hello".encodeToByteArray()
        val zip =
            StoredZipWriter.write(
                mapOf(
                    "a.txt" to payload,
                    "b/c.txt" to "world".encodeToByteArray(),
                ),
            )
        ZipInputStream(zip.inputStream()).use { zis ->
            val first = zis.nextEntry ?: fail("no entry")
            assertEquals("a.txt", first.name)
            assertEquals("hello", zis.readBytes().decodeToString())
            val second = zis.nextEntry ?: fail("no second entry")
            assertEquals("b/c.txt", second.name)
            assertEquals("world", zis.readBytes().decodeToString())
            assertEquals(null, zis.nextEntry)
        }
    }
}
