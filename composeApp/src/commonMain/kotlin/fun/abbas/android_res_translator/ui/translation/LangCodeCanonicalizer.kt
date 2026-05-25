package `fun`.abbas.android_res_translator.ui.translation

/**
 * 将各引擎/API 的异名语言码规范为应用内统一码（与 values 目录习惯一致）。
 */
object LangCodeCanonicalizer {
    private val aliases: Map<String, String> =
        mapOf(
            "zh-cn" to "zh",
            "zh-chs" to "zh",
            "zh-hans" to "zh",
            "zh-tw" to "zh-rTW",
            "zh-cht" to "zh-rTW",
            "zh-twn" to "zh-rTW",
            "zh-hant" to "zh-rTW",
            "zh-hk" to "zh-rHK",
            "jp" to "ja",
            "kor" to "ko",
            "fra" to "fr",
            "spa" to "es",
        )

    fun canonical(code: String): String {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return trimmed
        return aliases[trimmed.lowercase()] ?: trimmed
    }

    /** 按规范码去重，保留首次出现的规范形式。 */
    fun mergeDistinct(codes: Iterable<String>): List<String> {
        val seen = linkedSetOf<String>()
        val result = mutableListOf<String>()
        for (code in codes) {
            val c = canonical(code)
            val key = c.lowercase()
            if (key !in seen) {
                seen.add(key)
                result.add(c)
            }
        }
        return result
    }
}
