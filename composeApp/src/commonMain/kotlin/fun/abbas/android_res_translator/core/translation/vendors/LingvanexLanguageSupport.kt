package `fun`.abbas.android_res_translator.core.translation.vendors

/**
 * Lingvanex Translation API 目标语言白名单（与官方 Neural MT 文档一致）。
 *
 * [APP_TO_API_TARGET] 的 key 为应用内使用的语言码（Android 资源风格 + ISO 639-1），
 * value 为请求体 `to` 字段的 **Language code**（如 `en_US`、`zh-Hans_CN`）。
 *
 * 文档说明：列表内任意语言可互译；此处仅维护「目标侧」映射与别名归一。
 */
object LingvanexLanguageSupport {
    /** 供语言选择器等使用的、本厂商支持的应用侧语言码集合。 */
    val supportedAppCodes: Set<String>
        get() = APP_TO_API_TARGET.keys

    /**
     * 将应用传入的源/目标语言码归一后解析为 Lingvanex API 的 `to` 参数值。
     */
    fun resolveApiTargetCode(isoOrAndroidCode: String): String? {
        val canonical = canonicalAppCode(isoOrAndroidCode) ?: return null
        return APP_TO_API_TARGET[canonical]
    }

    fun supportsAppTargetLanguage(isoOrAndroidCode: String): Boolean =
        resolveApiTargetCode(isoOrAndroidCode) != null

    private fun canonicalAppCode(raw: String): String? {
        val t = raw.trim()
        if (t.isEmpty()) return null
        APP_TO_API_TARGET.keys.firstOrNull { it.equals(t, ignoreCase = true) }?.let { return it }
        val lower = t.lowercase().replace('_', '-')
        val fromAlias = ALIAS_TO_CANONICAL_APP[lower] ?: return null
        return APP_TO_API_TARGET.keys.firstOrNull { it.equals(fromAlias, ignoreCase = true) }
            ?: fromAlias.takeIf { APP_TO_API_TARGET.containsKey(it) }
    }

    /**
     * 应用侧语言码 → Lingvanex `to`（官方 Language code 列）。
     * 键与 [LanguagePickerCatalog] 中 Lingvanex 选项保持一致；含 `in`（Android 印尼语）等别名。
     */
    internal val APP_TO_API_TARGET: Map<String, String> =
        mapOf(
            "af" to "af_ZA",
            "sq" to "sq_AL",
            "am" to "am_ET",
            "ar" to "ar_SA",
            "hy" to "hy_AM",
            "az" to "az_AZ",
            "eu" to "eu_ES",
            "be" to "be_BY",
            "bn" to "bn_BD",
            "bs" to "bs_BA",
            "bg" to "bg_BG",
            "ca" to "ca_ES",
            "ceb" to "ceb_PH",
            "zh" to "zh-Hans_CN",
            "zh-rHK" to "zh-Hant_TW",
            "zh-rMO" to "zh-Hant_TW",
            "zh-rTW" to "zh-Hant_TW",
            "co" to "co_FR",
            "hr" to "hr_HR",
            "cs" to "cs_CZ",
            "da" to "da_DK",
            "nl" to "nl_NL",
            "en" to "en_US",
            "eo" to "eo_WORLD",
            "et" to "et_EE",
            "fi" to "fi_FI",
            "fr" to "fr_FR",
            "fy" to "fy_NL",
            "gl" to "gl_ES",
            "ka" to "ka_GE",
            "de" to "de_DE",
            "el" to "el_GR",
            "gu" to "gu_IN",
            "ht" to "ht_HT",
            "ha" to "ha_NE",
            "haw" to "haw_US",
            "he" to "he_IL",
            "iw" to "he_IL",
            "hi" to "hi_IN",
            "hmn" to "hmn_CN",
            "hu" to "hu_HU",
            "is" to "is_IS",
            "ig" to "ig_NG",
            "id" to "id_ID",
            "in" to "id_ID",
            "ga" to "ga_IE",
            "it" to "it_IT",
            "ja" to "ja_JP",
            "jv" to "jv_ID",
            "kn" to "kn_IN",
            "kk" to "kk_KZ",
            "km" to "km_KH",
            "rw" to "rw_RW",
            "ko" to "ko_KR",
            "ku" to "ku_IR",
            "ky" to "ky_KG",
            "lo" to "lo_LA",
            "la" to "la_VAT",
            "lv" to "lv_LV",
            "lt" to "lt_LT",
            "lb" to "lb_LU",
            "mk" to "mk_MK",
            "mg" to "mg_MG",
            "ms" to "ms_MY",
            "ml" to "ml_IN",
            "mt" to "mt_MT",
            "mi" to "mi_NZ",
            "mr" to "mr_IN",
            "mn" to "mn_MN",
            "my" to "my_MM",
            "ne" to "ne_NP",
            "ny" to "ny_MW",
            "no" to "no_NO",
            "nb" to "no_NO",
            "nn" to "no_NO",
            "or" to "or_OR",
            "ps" to "ps_AF",
            "fa" to "fa_IR",
            "pl" to "pl_PL",
            "pt" to "pt_PT",
            "pt-rBR" to "pt_BR",
            "pt-rPT" to "pt_PT",
            "pa" to "pa_PK",
            "ro" to "ro_RO",
            "ro-rRO" to "ro_RO",
            "ru" to "ru_RU",
            "sm" to "sm_WS",
            "gd" to "gd_GB",
            "sr" to "sr-Cyrl_RS",
            "st" to "st_LS",
            "sn" to "sn_ZW",
            "sd" to "sd_PK",
            "si" to "si_LK",
            "sk" to "sk_SK",
            "sl" to "sl_SI",
            "so" to "so_SO",
            "es" to "es_ES",
            "su" to "su_ID",
            "sw" to "sw_TZ",
            "sv" to "sv_SE",
            "tl" to "tl_PH",
            "fil" to "tl_PH",
            "tg" to "tg_TJ",
            "ta" to "ta_IN",
            "tt" to "tt_TT",
            "te" to "te_IN",
            "th" to "th_TH",
            "tr" to "tr_TR",
            "tk" to "tk_TM",
            "uk" to "uk_UA",
            "ur" to "ur_PK",
            "ug" to "ug_CN",
            "uz" to "uz_UZ",
            "vi" to "vi_VN",
            "cy" to "cy_GB",
            "xh" to "xh_ZA",
            "yi" to "yi_IL",
            "yo" to "yo_NG",
            "zu" to "zu_ZA",
        )

    /** BCP-47 / 常见变体 → [APP_TO_API_TARGET] 中的键（小写 + `-`）。 */
    private val ALIAS_TO_CANONICAL_APP: Map<String, String> =
        mapOf(
            "zh-cn" to "zh",
            "zh-hans" to "zh",
            "zh-chs" to "zh",
            "zh-tw" to "zh-rTW",
            "zh-hant" to "zh-rTW",
            "zh-hk" to "zh-rHK",
            "zh-mo" to "zh-rMO",
            "en-us" to "en",
            "en-gb" to "en",
            "pt-br" to "pt-rBR",
            "pt-pt" to "pt-rPT",
            "he-il" to "he",
            "iw-il" to "iw",
            "fil-ph" to "fil",
            "tl-ph" to "tl",
            "no-no" to "no",
            "nb-no" to "nb",
            "nn-no" to "nn",
            "ro-ro" to "ro",
        )
}
