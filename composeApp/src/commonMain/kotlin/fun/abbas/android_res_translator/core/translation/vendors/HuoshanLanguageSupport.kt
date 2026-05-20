package `fun`.abbas.android_res_translator.core.translation.vendors

/**
 * 火山引擎文本翻译语种白名单（与官方最新支持列表一致）。
 *
 * - 默认：列表内语种**全向互译**
 * - [TARGET_ONLY_SLOVAK]：仅支持其它语种 → 斯洛伐克语（`sk` 不可作源语言）
 * - [SOURCE_ONLY_TO_SIMPLIFIED_CHINESE]：仅支持该语种 → 简体中文 `zh`
 */
internal object HuoshanLanguageSupport {
    /** 仅可作目标：其它语种 → sk */
    private const val TARGET_ONLY_SLOVAK = "sk"

    /** 仅可作源：语种 → zh（简体） */
    private val SOURCE_ONLY_TO_SIMPLIFIED_CHINESE: Set<String> =
        setOf("bo", "nan", "wuu", "yue", "cmn", "ug")

    private const val SIMPLIFIED_CHINESE = "zh"

    /**
     * 火山 API 语种代号全集。
     */
    val supportedLanguageCodes: Set<String> =
        setOf(
            "zh", "zh-Hant", "zh-Hant-hk", "zh-Hant-tw",
            "tn", "vi", "iu", "it", "id", "hi", "en", "ho", "he", "es", "el", "uk", "ur",
            "tk", "tr", "ti", "ty", "tl", "to", "th", "ta", "te", "sl", "sk", "ss", "eo", "sm",
            "sg", "st", "sv", "ja", "tw", "qu", "pt", "pa", "no", "nb", "nr", "my", "bn", "mn",
            "mh", "mk", "ml", "mr", "ms", "lu", "ro", "lt", "lv", "lo", "kj", "hr", "kn", "ki",
            "cs", "ca", "nl", "ko", "ht", "gu", "ka", "kl", "km", "lg", "kg", "fi", "fj", "fr",
            "ru", "ng", "de", "tt", "da", "ts", "cv", "fa", "bs", "pl", "bi", "nd", "ba", "bg",
            "az", "ar", "af", "sq", "ab", "os", "ee", "et", "ay", "lzh", "am", "ckb", "cy", "gl",
            "ha", "hy", "ig", "kmr", "ln", "nso", "ny", "om", "sn", "so", "sr", "sw", "xh", "yo",
            "zu", "bo", "nan", "wuu", "yue", "cmn", "ug", "fuv", "hu", "kam", "luo", "rw", "umb",
            "wo",
        )

    /** 可参与全向互译的语种（排除 sk 与仅→中文 的源语种）。 */
    private val bidirectionalCodes: Set<String> =
        supportedLanguageCodes - SOURCE_ONLY_TO_SIMPLIFIED_CHINESE - TARGET_ONLY_SLOVAK

    private val aliasToHuoshan: Map<String, String> =
        buildMap {
            supportedLanguageCodes.forEach { put(it.lowercase(), it) }
            put("zh-cn", "zh")
            put("zh-hans", "zh")
            put("zh-rtw", "zh-Hant-tw")
            put("zh-rhk", "zh-Hant-hk")
            put("zh-rmo", "zh-Hant-hk")
            put("zh-hant", "zh-Hant")
            put("in", "id")
        }

    fun normalizeToHuoshanCode(isoOrAndroidCode: String): String? {
        val trimmed = isoOrAndroidCode.trim()
        if (trimmed.isEmpty()) return null
        return aliasToHuoshan[trimmed.lowercase().replace('_', '-')]
    }

    fun supportsLanguagePair(
        sourceLanguage: String,
        targetLanguage: String,
    ): Boolean {
        val source = normalizeToHuoshanCode(sourceLanguage) ?: return false
        val target = normalizeToHuoshanCode(targetLanguage) ?: return false
        if (source == target) return false
        if (source !in supportedLanguageCodes || target !in supportedLanguageCodes) return false

        if (source in SOURCE_ONLY_TO_SIMPLIFIED_CHINESE) {
            return target == SIMPLIFIED_CHINESE
        }
        if (source == TARGET_ONLY_SLOVAK) return false
        if (target == TARGET_ONLY_SLOVAK) {
            return source !in SOURCE_ONLY_TO_SIMPLIFIED_CHINESE
        }
        return source in bidirectionalCodes && target in bidirectionalCodes
    }

    fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean {
        val target = normalizeToHuoshanCode(isoOrAndroidCode) ?: return false
        if (target !in supportedLanguageCodes) return false
        if (target == TARGET_ONLY_SLOVAK) return true
        if (target == SIMPLIFIED_CHINESE) return true
        return target in bidirectionalCodes
    }

    /** 可作为源语言的火山码（用于 UI 列表粗筛）。 */
    fun sourceLanguageCodes(): Set<String> =
        supportedLanguageCodes - setOf(TARGET_ONLY_SLOVAK)

    /** 可作为目标语言的火山码（用于 UI 列表粗筛）。 */
    fun targetLanguageCodes(): Set<String> =
        supportedLanguageCodes - SOURCE_ONLY_TO_SIMPLIFIED_CHINESE
}
