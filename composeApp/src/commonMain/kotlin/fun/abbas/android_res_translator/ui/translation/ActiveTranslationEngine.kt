package `fun`.abbas.android_res_translator.ui.translation

/** 与 [defaultTranslationVendors] 顺序及 [TranslationVendor.name] 一致。 */
enum class ActiveTranslationEngine(
    val displayName: String,
    val vendorName: String,
) {
    Huoshan("火山引擎", "huoshan"),
    Lingvanex("Lingvanex", "lingvanex"),
    Baidu("百度翻译", "baidu"),
    Youdao("有道翻译", "youdao"),
    Tencent("腾讯翻译", "tencent"),
    ;

    companion object {
        fun fromPersisted(value: String?): ActiveTranslationEngine? =
            entries.firstOrNull { it.name == value }
    }
}
