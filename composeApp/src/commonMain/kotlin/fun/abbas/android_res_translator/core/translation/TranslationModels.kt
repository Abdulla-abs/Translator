package `fun`.abbas.android_res_translator.core.translation

data class TranslationRequest(
    val sourceText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
)

data class TranslationSuccess(
    val translatedText: String,
    val resolvedSourceLanguage: String,
    val resolvedTargetLanguage: String,
)

sealed class TranslationFailure {
    data class NoVendorForTarget(
        val targetLanguage: String,
    ) : TranslationFailure()

    data class VendorRejected(
        val vendorName: String,
        val detail: String,
    ) : TranslationFailure()

    data class NetworkFailure(
        val message: String,
    ) : TranslationFailure()
}

sealed class TranslationOutcome {
    data class Ok(
        val value: TranslationSuccess,
    ) : TranslationOutcome()

    data class Err(
        val failure: TranslationFailure,
    ) : TranslationOutcome()

    fun successOrNull(): TranslationSuccess? = (this as? Ok)?.value

    fun failureOrNull(): TranslationFailure? = (this as? Err)?.failure
}
