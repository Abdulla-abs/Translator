package `fun`.abbas.android_res_translator.core.translation.vendors

/**
 * 腾讯云机器翻译（TMT）源/目标语言白名单。
 *
 * 语言码与 [Tencent TMT 文档](https://cloud.tencent.com/document/product/551/15619) 一致（如 `zh`、`zh-TW`、`en`）。
 * 应用内 Android 风格码（如 `zh-rTW`、`in`）经 [normalizeToTencentCode] 映射后再校验。
 */
internal object TencentLanguageSupport {
    /** 可作为源语言的代码（腾讯云 API 码）。 */
    val supportedSources: Set<String> =
        setOf(
            "zh",
            "zh-TW",
            "en",
            "ja",
            "ko",
            "fr",
            "es",
            "it",
            "de",
            "tr",
            "ru",
            "pt",
            "vi",
            "id",
            "th",
            "ms",
            "ar",
            "hi",
        )

    /**
     * 各源语言允许的目标语言列表（腾讯云 API 码）。
     * 与官方「各源语言的目标语言支持列表」一致。
     */
    val targetsBySource: Map<String, Set<String>> =
        mapOf(
            "zh" to
                setOf(
                    "zh-TW", "en", "ja", "ko", "fr", "es", "it", "de", "tr", "ru", "pt",
                    "vi", "id", "th", "ms", "ar",
                ),
            "zh-TW" to
                setOf(
                    "zh", "en", "ja", "ko", "fr", "es", "it", "de", "tr", "ru", "pt",
                    "vi", "id", "th", "ms", "ar",
                ),
            "en" to
                setOf(
                    "zh", "zh-TW", "ja", "ko", "fr", "es", "it", "de", "tr", "ru", "pt",
                    "vi", "id", "th", "ms", "ar", "hi",
                ),
            "ja" to setOf("zh", "zh-TW", "en", "ko"),
            "ko" to setOf("zh", "zh-TW", "en", "ja"),
            "fr" to setOf("zh", "zh-TW", "en", "es", "it", "de", "tr", "ru", "pt"),
            "es" to setOf("zh", "zh-TW", "en", "fr", "it", "de", "tr", "ru", "pt"),
            "it" to setOf("zh", "zh-TW", "en", "fr", "es", "de", "tr", "ru", "pt"),
            "de" to setOf("zh", "zh-TW", "en", "fr", "es", "it", "tr", "ru", "pt"),
            "tr" to setOf("zh", "zh-TW", "en", "fr", "es", "it", "de", "ru", "pt"),
            "ru" to setOf("zh", "zh-TW", "en", "fr", "es", "it", "de", "tr", "pt"),
            "pt" to setOf("zh", "zh-TW", "en", "fr", "es", "it", "de", "tr", "ru"),
            "vi" to setOf("zh", "zh-TW", "en"),
            "id" to setOf("zh", "zh-TW", "en"),
            "th" to setOf("zh", "zh-TW", "en"),
            "ms" to setOf("zh", "zh-TW", "en"),
            "ar" to setOf("zh", "zh-TW", "en"),
            "hi" to setOf("en"),
        )

    private val allTargetCodes: Set<String> =
        targetsBySource.values.flatten().toSet()

    private val aliasToTencent: Map<String, String> =
        buildMap {
            supportedSources.forEach { put(it.lowercase(), it) }
            allTargetCodes.forEach { put(it.lowercase(), it) }
            put("zh-cn", "zh")
            put("zh-hans", "zh")
            put("zh-rtw", "zh-TW")
            put("zh-rhk", "zh-TW")
            put("zh-rmo", "zh-TW")
            put("zh-hant", "zh-TW")
            put("in", "id")
        }

    fun normalizeToTencentCode(isoOrAndroidCode: String): String? {
        val trimmed = isoOrAndroidCode.trim()
        if (trimmed.isEmpty()) return null
        return aliasToTencent[trimmed.lowercase().replace('_', '-')]
    }

    fun supportsLanguagePair(
        sourceLanguage: String,
        targetLanguage: String,
    ): Boolean {
        val source = normalizeToTencentCode(sourceLanguage) ?: return false
        val target = normalizeToTencentCode(targetLanguage) ?: return false
        if (source == target) return false
        return targetsBySource[source]?.contains(target) == true
    }

    /** 目标码是否出现在任一源语言的目标列表中（用于编排器粗筛）。 */
    fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean {
        val target = normalizeToTencentCode(isoOrAndroidCode) ?: return false
        return target in allTargetCodes
    }
}
