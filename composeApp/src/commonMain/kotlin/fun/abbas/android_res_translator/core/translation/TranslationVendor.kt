package `fun`.abbas.android_res_translator.core.translation

interface TranslationVendor {
    val name: String

    fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean

    suspend fun translate(request: TranslationRequest): TranslationOutcome
}
