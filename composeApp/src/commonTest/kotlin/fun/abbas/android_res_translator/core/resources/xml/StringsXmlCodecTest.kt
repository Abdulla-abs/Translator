package `fun`.abbas.android_res_translator.core.resources.xml

import kotlin.test.Test
import kotlin.test.assertEquals

class StringsXmlCodecTest {

    private val golden =
        """
        <?xml version="1.0" encoding="utf-8"?>
        <resources>
            <string name="app_name">Demo</string>
            <string-array name="tabs">
                <item>A</item>
                <item>B</item>
            </string-array>
        </resources>
        """.trimIndent()

    @Test
    fun parseGolden_entriesAndArrayReadable() {
        val model = StringsXmlCodec.parse(golden)
        assertEquals("Demo", model.entries["app_name"])
        assertEquals(listOf("A", "B"), model.stringArrays["tabs"]?.items)
    }

    @Test
    fun roundTrip_semanticallyEquivalent() {
        val first = StringsXmlCodec.parse(golden)
        val xml = StringsXmlCodec.serialize(first)
        val second = StringsXmlCodec.parse(xml)
        assertEquals(first.entries, second.entries)
        assertEquals(first.stringArrays.keys, second.stringArrays.keys)
        for (k in first.stringArrays.keys) {
            assertEquals(first.stringArrays[k]?.items, second.stringArrays[k]?.items)
        }
    }

    @Test
    fun parseAndRoundTrip_plurals() {
        val xml =
            """
            <resources>
                <plurals name="errors">
                    <item quantity="one">One error</item>
                    <item quantity="other">%d errors</item>
                </plurals>
            </resources>
            """.trimIndent()
        val parsed = StringsXmlCodec.parse(xml)
        assertEquals("One error", parsed.plurals["errors"]?.items?.get("one"))
        val again = StringsXmlCodec.parse(StringsXmlCodec.serialize(parsed))
        assertEquals(parsed.plurals, again.plurals)
    }
}
