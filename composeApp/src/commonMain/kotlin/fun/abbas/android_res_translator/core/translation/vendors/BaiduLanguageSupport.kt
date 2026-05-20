package `fun`.abbas.android_res_translator.core.translation.vendors

/**
 * 百度通用翻译 API 语种白名单（与官方语种列表代码一致）。
 *
 * 列表内语种在 API 层面可互译；源语言可为 [AUTO]（`from=auto`）表示自动检测。
 * 应用内常用码经 [normalizeToBaiduCode] 映射（如 `zh-rTW` → `cht`，`ja` → `jp`）。
 */
internal object BaiduLanguageSupport {
    const val AUTO: String = "auto"

    /** 百度 API 语种代码全集（表中「代码」列，含 [AUTO]）。 */
    val supportedLanguageCodes: Set<String> =
        setOf(
            AUTO,
            // A
            "ara", "gle", "oci", "alb", "arq", "aka", "arg", "amh", "asm", "aym", "aze", "ast",
            "oss", "est", "oji", "ori", "orm",
            // B
            "pl", "per", "bre", "bak", "baq", "pot", "bel", "ber", "pam", "bul", "sme", "ped",
            "bem", "bli", "bis", "bal", "ice", "bos", "bho",
            // C
            "chv", "tso",
            // D
            "dan", "de", "tat", "sha", "tet", "div", "log",
            // E
            "ru",
            // F
            "fra", "fil", "fin", "san", "fri", "ful", "fao",
            // G
            "gla", "kon", "ups", "hkm", "kal", "geo", "guj", "gra", "eno", "grn",
            // H
            "kor", "nl", "hup", "hak", "ht", "mot", "hau",
            // J
            "kir", "glg", "frn", "cat", "cs",
            // K
            "kab", "kan", "kau", "kah", "cor", "xho", "cos", "cre", "cri", "kli", "hrv", "que",
            "kas", "kok", "kur",
            // L
            "lat", "lao", "rom", "lag", "lav", "lim", "lin", "lug", "ltz", "ruy", "kin", "lit",
            "roh", "ro", "loj",
            // M
            "may", "bur", "mar", "mg", "mal", "mac", "mah", "mai", "glv", "mau", "mao", "ben",
            "mlt", "hmn",
            // N
            "nor", "nea", "nbl", "afr", "sot", "nep",
            // P
            "pt", "pan", "pap", "pus",
            // Q
            "nya", "twi", "chr",
            // R
            "jp", "swe",
            // S
            "srd", "sm", "sec", "srp", "sol", "sin", "epo", "nob", "sk", "slo", "swa", "src",
            "som", "sco",
            // T
            "th", "tr", "tgk", "tam", "tgl", "tir", "tel", "tua", "tuk",
            // W
            "ukr", "wln", "wel", "ven", "wol", "urd",
            // X
            "spa", "heb", "el", "hu", "fry", "sil", "hil", "los", "haw", "nno", "nqo", "snd",
            "sna", "ceb", "syr", "sun",
            // Y
            "en", "hi", "id", "it", "vie", "yid", "ina", "ach", "ing", "ibo", "ido", "yor", "arm",
            "iku",
            // Z
            "zh", "cht", "wyw", "yue", "zaz", "frm", "zul", "jav",
        )

    private val translatableCodes: Set<String> = supportedLanguageCodes - AUTO

    private val aliasToBaidu: Map<String, String> =
        buildMap {
            translatableCodes.forEach { put(it.lowercase(), it) }
            put(AUTO, AUTO)
            put("zh-cn", "zh")
            put("zh-hans", "zh")
            put("zh-tw", "cht")
            put("zh-rtw", "cht")
            put("zh-rhk", "cht")
            put("zh-rmo", "cht")
            put("zh-hant", "cht")
            put("ja", "jp")
            put("japanese", "jp")
            put("ko", "kor")
            put("korean", "kor")
            put("fr", "fra")
            put("french", "fra")
            put("es", "spa")
            put("spanish", "spa")
            put("ar", "ara")
            put("arabic", "ara")
            put("vi", "vie")
            put("vietnamese", "vie")
            put("fa", "per")
            put("persian", "per")
            put("he", "heb")
            put("hebrew", "heb")
            put("el", "el")
            put("greek", "el")
            put("ru", "ru")
            put("russian", "ru")
            put("pt", "pt")
            put("portuguese", "pt")
            put("de", "de")
            put("german", "de")
            put("it", "it")
            put("italian", "it")
            put("nl", "nl")
            put("dutch", "nl")
            put("th", "th")
            put("thai", "th")
            put("tr", "tr")
            put("turkish", "tr")
            put("hi", "hi")
            put("hindi", "hi")
            put("in", "id")
            put("indonesian", "id")
            put("ms", "may")
            put("malay", "may")
            put("my", "bur")
            put("burmese", "bur")
            put("ro", "rom")
            put("romanian", "rom")
            put("cs", "cs")
            put("czech", "cs")
            put("sv", "swe")
            put("swedish", "swe")
            put("no", "nor")
            put("norwegian", "nor")
            put("da", "dan")
            put("danish", "dan")
            put("fi", "fin")
            put("finnish", "fin")
            put("pl", "pl")
            put("polish", "pl")
            put("uk", "ukr")
            put("ukrainian", "ukr")
            put("sr", "srp")
            put("serbian", "srp")
            put("hr", "hrv")
            put("croatian", "hrv")
            put("sl", "slo")
            put("slovenian", "slo")
            put("sk", "sk")
            put("slovak", "sk")
            put("bg", "bul")
            put("bulgarian", "bul")
            put("ca", "cat")
            put("catalan", "cat")
            put("eu", "baq")
            put("basque", "baq")
            put("gl", "glg")
            put("galician", "glg")
            put("af", "afr")
            put("sw", "swa")
            put("swahili", "swa")
            put("tl", "tgl")
            put("fil", "fil")
            put("tagalog", "tgl")
        }

    fun normalizeToBaiduCode(isoOrAndroidCode: String): String? {
        val trimmed = isoOrAndroidCode.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.equals(AUTO, ignoreCase = true)) return AUTO
        return aliasToBaidu[trimmed.lowercase().replace('_', '-')]
    }

    fun supportsLanguagePair(
        sourceLanguage: String,
        targetLanguage: String,
    ): Boolean {
        val source = normalizeToBaiduCode(sourceLanguage) ?: return false
        val target = normalizeToBaiduCode(targetLanguage) ?: return false
        if (source == target) return false
        if (target !in translatableCodes) return false
        return when (source) {
            AUTO -> true
            else -> source in translatableCodes
        }
    }

    fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean {
        val target = normalizeToBaiduCode(isoOrAndroidCode) ?: return false
        return target in translatableCodes
    }
}
