package `fun`.abbas.android_res_translator.core.resources.compare

import `fun`.abbas.android_res_translator.core.resources.model.PluralEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringArrayEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompareMatrixBuilderTest {
    @Test
    fun build_marksDifferentStringValues() {
        val left =
            StringResourceFile(
                strings = mapOf("app_name" to StringEntry("app_name", "我的程序")),
            )
        val right =
            StringResourceFile(
                strings = mapOf("app_name" to StringEntry("app_name", "我的应用")),
            )
        val matrix =
            CompareMatrixBuilder.build(
                leftColumnLabel = "zh",
                leftFile = left,
                rightColumnLabel = "zh",
                rightFile = right,
            )
        val row = matrix.rows.single { it.key == "app_name" }
        assertTrue(row.hasDifference)
        assertEquals("我的程序", row.leftValue)
        assertEquals("我的应用", row.rightValue)
    }

    @Test
    fun build_flattensStringArrayAndPlurals() {
        val left =
            StringResourceFile(
                stringArrays = mapOf("tabs" to StringArrayEntry("tabs", listOf("A", "B"))),
                plurals = mapOf("errors" to PluralEntry("errors", mapOf("one" to "1 err", "other" to "%d errs"))),
            )
        val right =
            StringResourceFile(
                stringArrays = mapOf("tabs" to StringArrayEntry("tabs", listOf("A", "B"))),
                plurals = mapOf("errors" to PluralEntry("errors", mapOf("one" to "1 err", "other" to "%d errors"))),
            )
        val matrix =
            CompareMatrixBuilder.build(
                leftColumnLabel = "A",
                leftFile = left,
                rightColumnLabel = "B",
                rightFile = right,
            )
        assertFalse(matrix.rows.first { it.key == "tabs0" }.hasDifference)
        assertTrue(matrix.rows.first { it.key == "errors#other" }.hasDifference)
    }

    @Test
    fun normalizeForCompare_collapsesWhitespace() {
        assertEquals("a b", CompareMatrixBuilder.normalizeForCompare("  a   b  "))
    }
}
