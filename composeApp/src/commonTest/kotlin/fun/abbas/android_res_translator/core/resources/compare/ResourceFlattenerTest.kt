package `fun`.abbas.android_res_translator.core.resources.compare

import `fun`.abbas.android_res_translator.core.resources.model.StringArrayEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import kotlin.test.Test
import kotlin.test.assertEquals

class ResourceFlattenerTest {
    @Test
    fun flatten_expandsArrayItemsWithIndexKeys() {
        val file =
            StringResourceFile(
                strings = mapOf("k" to StringEntry("k", "v")),
                stringArrays = mapOf("items" to StringArrayEntry("items", listOf("x", "y"))),
            )
        val flat = ResourceFlattener.flatten(file)
        assertEquals("v", flat["k"]?.value)
        assertEquals("x", flat["items0"]?.value)
        assertEquals("y", flat["items1"]?.value)
    }
}
