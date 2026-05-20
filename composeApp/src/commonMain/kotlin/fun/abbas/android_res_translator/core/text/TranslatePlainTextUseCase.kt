package `fun`.abbas.android_res_translator.core.text

import `fun`.abbas.android_res_translator.core.translation.TranslationOrchestrator
import `fun`.abbas.android_res_translator.core.translation.TranslationRequest

class TranslatePlainTextUseCase(
    private val orchestrator: TranslationOrchestrator,
) {
    suspend operator fun invoke(
        text: String,
        from: String,
        to: String,
        preferredVendorName: String? = null,
    ) = orchestrator.translate(TranslationRequest(text, from, to), preferredVendorName)
}
