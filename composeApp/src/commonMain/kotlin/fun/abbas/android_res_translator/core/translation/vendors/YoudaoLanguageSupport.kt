package `fun`.abbas.android_res_translator.core.translation.vendors

/**
 * 有道翻译 API 支持语言白名单（与官方语言列表代码一致）。
 *
 * 任意两种已支持语言之间可互译（无定向语言对表）；源语言可为 [AUTO] 表示自动识别。
 * 应用内 Android 风格码经 [normalizeToYoudaoCode] 映射（如 `zh` → `zh-CHS`，`zh-rTW` → `zh-CHT`）。
 *
 * @see <a href="https://ai.youdao.com/DOCSIRMA/html/trans/api/wbfy/index.html">有道翻译 API 语言列表</a>
 */
internal object YoudaoLanguageSupport {
    /** 自动识别源语言（`from=auto`）。 */
    const val AUTO: String = "auto"

    /**
     * 有道 API 语言代码全集（表中「代码」列，去重）。
     */
    val supportedLanguageCodes: Set<String> =
        setOf(
            "ar", "de", "en", "es", "fr", "hi", "id", "it", "ja", "ko", "nl", "pt", "ru", "th", "vi",
            "zh-CHS", "zh-CHT",
            "af", "am", "az", "be", "bg", "bn", "bs", "ca", "ceb", "co", "cs", "cy", "da", "el", "eo",
            "et", "eu", "fa", "fi", "fj", "fy", "ga", "gd", "gl", "gu", "ha", "haw", "he", "hr", "ht",
            "hu", "hy", "ig", "is", "jw", "ka", "kk", "km", "kn", "ku", "ky", "la", "lb", "lo", "lt",
            "lv", "mg", "mi", "mk", "ml", "mn", "mr", "ms", "mt", "mww", "my", "ne", "no", "ny", "otq",
            "pa", "pl", "ps", "ro", "sd", "si", "sk", "sl", "sm", "sn", "so", "sq", "sr-Cyrl", "sr-Latn",
            "st", "su", "sv", "sw", "ta", "te", "tg", "tl", "tlh", "to", "tr", "ty", "uk", "ur", "uz",
            "xh", "yi", "yo", "yua", "yue", "zu",
        )

    private val aliasToYoudao: Map<String, String> =
        buildMap {
            supportedLanguageCodes.forEach { code ->
                put(code.lowercase(), code)
            }
            put(AUTO, AUTO)
            // Android / 常见别名 → 有道码
            put("zh", "zh-CHS")
            put("zh-cn", "zh-CHS")
            put("zh-hans", "zh-CHS")
            put("zh-tw", "zh-CHT")
            put("zh-rtw", "zh-CHT")
            put("zh-rhk", "zh-CHT")
            put("zh-rmo", "zh-CHT")
            put("zh-hant", "zh-CHT")
            put("in", "id")
            put("sr", "sr-Latn")
            put("fil", "tl")
        }

    fun normalizeToYoudaoCode(isoOrAndroidCode: String): String? {
        val trimmed = isoOrAndroidCode.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.equals(AUTO, ignoreCase = true)) return AUTO
        return aliasToYoudao[trimmed.lowercase().replace('_', '-')]
    }

    fun supportsLanguagePair(
        sourceLanguage: String,
        targetLanguage: String,
    ): Boolean {
        val source = normalizeToYoudaoCode(sourceLanguage) ?: return false
        val target = normalizeToYoudaoCode(targetLanguage) ?: return false
        if (source == target) return false
        if (target !in supportedLanguageCodes) return false
        return when (source) {
            AUTO -> true
            else -> source in supportedLanguageCodes
        }
    }

    fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean {
        val target = normalizeToYoudaoCode(isoOrAndroidCode) ?: return false
        return target in supportedLanguageCodes
    }
}
