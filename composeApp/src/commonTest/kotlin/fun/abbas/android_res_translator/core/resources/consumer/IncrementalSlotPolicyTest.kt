package `fun`.abbas.android_res_translator.core.resources.consumer

import `fun`.abbas.android_res_translator.core.resources.model.StringArrayEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringEntry
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IncrementalSlotPolicyTest {
    @Test
    fun string_missingKey_needsTranslate() {
        assertTrue(IncrementalSlotPolicy.needsTranslateString(target = null))
    }

    @Test
    fun string_blankValue_needsTranslate() {
        assertTrue(
            IncrementalSlotPolicy.needsTranslateString(
                target = StringEntry("k", "  ", translatable = true),
            ),
        )
    }

    @Test
    fun string_nonBlank_skips() {
        assertFalse(
            IncrementalSlotPolicy.needsTranslateString(
                target = StringEntry("k", "Hi", translatable = true),
            ),
        )
    }

    @Test
    fun array_existingWithOneBlankItem_needsArrayWork() {
        val src = StringArrayEntry("a", listOf("A", "B"))
        val tgt = StringArrayEntry("a", listOf("x", ""))
        assertTrue(IncrementalSlotPolicy.needsTranslateStringArray(src, tgt))
        assertFalse(IncrementalSlotPolicy.needsTranslateStringArrayItem("x"))
        assertTrue(IncrementalSlotPolicy.needsTranslateStringArrayItem(""))
    }
}
