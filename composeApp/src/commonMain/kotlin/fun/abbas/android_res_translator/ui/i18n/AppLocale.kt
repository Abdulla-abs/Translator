package `fun`.abbas.android_res_translator.ui.i18n

enum class AppLocale(val tag: String) {
    En("en"),
    Zh("zh"),
    ;

    companion object {
        fun fromTag(tag: String?): AppLocale =
            entries.firstOrNull { it.tag.equals(tag?.trim(), ignoreCase = true) } ?: En
    }
}
