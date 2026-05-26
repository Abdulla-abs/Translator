package `fun`.abbas.android_res_translator.core.resources.resmulti

import `fun`.abbas.android_res_translator.core.resources.compare.FlatRowKind
import `fun`.abbas.android_res_translator.core.resources.xml.StringsXmlCodec
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiLanguageEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResMultiImportApplierTest {
    @Test
    fun flatResourceWriter_updatesOnlyTargetKey() {
        val xml =
            """
            <resources>
                <string name="a">1</string>
                <string name="b">2</string>
            </resources>
            """.trimIndent()
        val file = StringsXmlCodec.parse(xml)
        val updated = FlatResourceWriter.setValue(file, "a", FlatRowKind.STRING, "9")
        val serialized = StringsXmlCodec.serialize(updated)
        assertTrue(serialized.contains("""<string name="a">9</string>"""))
        assertTrue(serialized.contains("""<string name="b">2</string>"""))
        val reparsed = StringsXmlCodec.parse(serialized)
        assertEquals("9", reparsed.strings["a"]?.value)
        assertEquals("2", reparsed.strings["b"]?.value)
    }

    @Test
    fun flatResourceWriter_updatesArrayItem() {
        val xml =
            """<resources><string-array name="tabs"><item>A</item><item>B</item></string-array></resources>"""
        val file = StringsXmlCodec.parse(xml)
        val updated = FlatResourceWriter.setValue(file, "tabs1", FlatRowKind.STRING_ARRAY_ITEM, "乙")
        val reparsed = StringsXmlCodec.parse(StringsXmlCodec.serialize(updated))
        assertEquals(listOf("A", "乙"), reparsed.stringArrays["tabs"]?.items)
    }
}
