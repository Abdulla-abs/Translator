package `fun`.abbas.android_res_translator.ui.screens.main

internal fun formatLanguageLabel(code: String): String {
    val trimmed = code.trim()
    if (trimmed.isBlank()) return "English (EN)"
    val key = trimmed.lowercase().replace('_', '-')
    val name =
        when (key) {
            "auto" -> "自动识别"
            "en", "english" -> "英语"
            "zh", "zh-cn", "zh-chs", "chinese" -> "简体中文"
            "cht" -> "繁体中文"
            "wyw" -> "文言文"
            "jp" -> "日语"
            "kor" -> "韩语"
            "fra" -> "法语"
            "spa" -> "西班牙语"
            "vie" -> "越南语"
            "per" -> "波斯语"
            "heb" -> "希伯来语"
            "zh-tw", "zh-cht", "zh-rtw", "zh-rhk", "zh-rmo", "zh-hant", "zh-hant-tw", "zh-hant-hk" -> "繁体中文"
            "bo" -> "藏语"
            "yue" -> "粤语"
            "wuu" -> "吴语"
            "nan" -> "闽南语"
            "cmn" -> "西南官话"
            "ug" -> "维吾尔语"
            "lzh" -> "文言文"
            "sk" -> "斯洛伐克语"
            "ja", "japanese" -> "日语"
            "ko", "korean" -> "韩语"
            "fr", "french" -> "法语"
            "de", "german" -> "德语"
            "es", "spanish" -> "西班牙语"
            "it", "italian" -> "意大利语"
            "pt", "portuguese" -> "葡萄牙语"
            "ru", "russian" -> "俄语"
            "ar", "arabic" -> "阿拉伯语"
            "hi", "hindi" -> "印地语"
            "id", "in", "indonesian" -> "印尼语"
            "th", "thai" -> "泰语"
            "vi", "vietnamese" -> "越南语"
            "ms", "malay" -> "马来语"
            "tr", "turkish" -> "土耳其语"
            "nl", "dutch" -> "荷兰语"
            "cs", "czech" -> "捷克语"
            "el", "greek" -> "希腊语"
            "fi", "finnish" -> "芬兰语"
            "hr", "croatian" -> "克罗地亚语"
            "ro-rro", "romanian" -> "罗马尼亚语"
            "sl", "slovenian" -> "斯洛文尼亚语"
            "sr", "serbian" -> "塞尔维亚语"
            "sr-cyrl", "sr-latn" -> "塞尔维亚语"
            else -> trimmed.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    val suffix = trimmed.uppercase().take(8)
    return "$name ($suffix)"
}
