package `fun`.abbas.android_res_translator.core.translation

interface TranslationVendor {
    val name: String

    fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean

    /** 是否支持指定源→目标语言对；默认仅检查目标语言。 */
    fun supportsLanguagePair(
        sourceLanguage: String,
        targetLanguage: String,
    ): Boolean = supportsTargetLanguage(targetLanguage)

    suspend fun translate(request: TranslationRequest): TranslationOutcome
}
