package `fun`.abbas.android_res_translator.core.resources.diff

import `fun`.abbas.android_res_translator.core.resources.model.StringArrayEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import kotlin.test.Test
import kotlin.test.assertTrue

class ResourceDiffAnalyzerTest {

    @Test
    fun detectsMissingExtraAndMismatch() {
        val base =
            StringResourceFile(
                strings =
                    mapOf(
                        "a" to StringEntry("a", "1"),
                        "b" to StringEntry("b", "2"),
                    ),
                stringArrays = mapOf("t" to StringArrayEntry("t", listOf("x"))),
            )
        val zh =
            StringResourceFile(
                strings =
                    mapOf(
                        "b" to StringEntry("b", "changed"),
                        "c" to StringEntry("c", "3"),
                    ),
                stringArrays = mapOf("t" to StringArrayEntry("t", listOf("y"))),
            )
        val rows = ResourceDiffAnalyzer.diffOne(base, "zh", zh)
        assertTrue(rows.filterIsInstance<DiffRow.MissingInTarget>().any { it.key == "a" })
        assertTrue(rows.filterIsInstance<DiffRow.ExtraInTarget>().any { it.key == "c" })
        assertTrue(
            rows.filterIsInstance<DiffRow.ValueMismatch>().any { it.key == "b" && !it.isStringArray },
        )
        assertTrue(
            rows.filterIsInstance<DiffRow.ValueMismatch>().any { it.key == "t" && it.isStringArray },
        )
    }
}
