package `fun`.abbas.android_res_translator.core.translation

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class StubVendor(
    override val name: String,
    private val supports: Boolean,
    private val outcome: TranslationOutcome,
) : TranslationVendor {
    override fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean = supports

    override fun supportsLanguagePair(
        sourceLanguage: String,
        targetLanguage: String,
    ): Boolean = supports

    override suspend fun translate(request: TranslationRequest): TranslationOutcome = outcome
}

class TranslationOrchestratorTest {
    @Test
    fun skipsUnsupportedThenUsesSecond() = runTest {
        val ok = TranslationOutcome.Ok(TranslationSuccess("X", "zh", "en"))
        val chain = TranslationOrchestrator(
            listOf(
                StubVendor(
                    "a",
                    supports = false,
                    outcome = TranslationOutcome.Err(TranslationFailure.VendorRejected("a", "skip")),
                ),
                StubVendor("b", supports = true, outcome = ok),
            ),
        )
        val r = chain.translate(TranslationRequest("你好", "zh", "en"))
        assertEquals("X", r.successOrNull()!!.translatedText)
    }

    @Test
    fun prefersSelectedVendorFirst() = runTest {
        var firstCalled: String? = null
        val vendorA =
            object : TranslationVendor {
                override val name = "a"

                override fun supportsTargetLanguage(isoOrAndroidCode: String) = true

                override suspend fun translate(request: TranslationRequest): TranslationOutcome {
                    firstCalled = name
                    return TranslationOutcome.Err(TranslationFailure.VendorRejected(name, "fail"))
                }
            }
        val vendorB =
            object : TranslationVendor {
                override val name = "b"

                override fun supportsTargetLanguage(isoOrAndroidCode: String) = true

                override suspend fun translate(request: TranslationRequest): TranslationOutcome {
                    firstCalled = name
                    return TranslationOutcome.Ok(TranslationSuccess("OK", "zh", "en"))
                }
            }
        val chain = TranslationOrchestrator(listOf(vendorA, vendorB))
        chain.translate(TranslationRequest("x", "zh", "en"), preferredVendorName = "b")
        assertEquals("b", firstCalled)
    }
}
