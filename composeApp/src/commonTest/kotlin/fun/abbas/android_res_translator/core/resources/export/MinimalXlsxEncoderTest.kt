package `fun`.abbas.android_res_translator.core.resources.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MinimalXlsxEncoderTest {
    @Test
    fun encode_producesZipWithXlsxParts() {
        val matrix =
            StringsMatrix(
                columnHeaders = listOf("key", "en", "zh"),
                rows =
                    listOf(
                        StringsMatrixRow("app_name", listOf("app_name", "Hello", "你好")),
                    ),
            )
        val bytes = MinimalXlsxEncoder.encode(matrix)
        assertTrue(bytes.size > 100)
        assertEquals(0x50, bytes[0].toInt()) // PK
        assertEquals(0x4b, bytes[1].toInt())
        val text = bytes.decodeToString()
        assertTrue("sharedStrings.xml" in text || "xl/sharedStrings.xml" in text)
    }
}
