package `fun`.abbas.android_res_translator.ui

import `fun`.abbas.android_res_translator.core.translation.TranslationFailure

fun TranslationFailure.toUserMessage(): String =
    when (this) {
        is TranslationFailure.NoVendorForTarget ->
            "没有翻译服务支持目标语言：$targetLanguage"
        is TranslationFailure.VendorRejected ->
            vendorRejectedUserMessage(vendorName, detail)
        is TranslationFailure.NetworkFailure ->
            "网络错误：$message"
    }

private fun vendorRejectedUserMessage(vendorName: String, detail: String): String {
    val display =
        when (vendorName) {
            "lingvanex" -> "Lingvanex"
            "huoshan" -> "火山引擎"
            "baidu" -> "百度翻译"
            "youdao" -> "有道翻译"
            "tencent" -> "腾讯翻译"
            else -> vendorName
        }
    if (vendorName == "lingvanex" && detail.contains("payment method", ignoreCase = true)) {
        return "$display：请在 Lingvanex 后台绑定支付方式（Billing）后再试。API 提示需绑定付款方式。"
    }
    if (detail.startsWith("missing ")) {
        return "$display：未配置 API 密钥（$detail）"
    }
    if (detail.startsWith("unsupported ")) {
        return "$display：不支持当前语言或语言对（$detail）"
    }
    if (detail.startsWith("api err=")) {
        val apiMsg =
            Regex("""api err="([^"]*)"""")
                .find(detail)
                ?.groupValues
                ?.getOrNull(1)
                ?.takeIf { it.isNotBlank() }
        return if (apiMsg != null) "$display：$apiMsg" else "$display：$detail"
    }
    return "$display：$detail"
}
