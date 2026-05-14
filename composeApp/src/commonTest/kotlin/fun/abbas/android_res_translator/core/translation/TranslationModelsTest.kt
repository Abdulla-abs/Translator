package `fun`.abbas.android_res_translator.core.translation

import kotlin.test.Test
import kotlin.test.assertEquals

class TranslationModelsTest {
    @Test
    fun okWrapsSuccess() {
        val s = TranslationSuccess("hi", "zh", "en")
        val o: TranslationOutcome = TranslationOutcome.Ok(s)
        assertEquals("hi", o.successOrNull()!!.translatedText)
    }
}
