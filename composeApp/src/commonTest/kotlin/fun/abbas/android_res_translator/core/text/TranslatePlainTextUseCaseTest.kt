package `fun`.abbas.android_res_translator.core.text

import `fun`.abbas.android_res_translator.core.translation.TranslationOrchestrator
import `fun`.abbas.android_res_translator.core.translation.TranslationOutcome
import `fun`.abbas.android_res_translator.core.translation.TranslationRequest
import `fun`.abbas.android_res_translator.core.translation.TranslationSuccess
import `fun`.abbas.android_res_translator.core.translation.TranslationVendor
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TranslatePlainTextUseCaseTest {
    @Test
    fun delegatesToOrchestrator() = runTest {
        val vendor =
            object : TranslationVendor {
                override val name = "one"

                override fun supportsTargetLanguage(isoOrAndroidCode: String) = true

                override suspend fun translate(request: TranslationRequest) =
                    TranslationOutcome.Ok(
                        TranslationSuccess("Y", request.sourceLanguage, request.targetLanguage),
                    )
            }
        val uc = TranslatePlainTextUseCase(TranslationOrchestrator(listOf(vendor)))
        val r = uc("a", "zh", "en")
        assertEquals("Y", r.successOrNull()!!.translatedText)
    }
}
