package `fun`.abbas.android_res_translator.ui.i18n

import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.error_lingvanex_payment
import androidrestranslator.composeapp.generated.resources.error_network
import androidrestranslator.composeapp.generated.resources.error_no_vendor_for_target
import androidrestranslator.composeapp.generated.resources.error_vendor_api
import androidrestranslator.composeapp.generated.resources.error_vendor_generic
import androidrestranslator.composeapp.generated.resources.error_vendor_missing_key
import androidrestranslator.composeapp.generated.resources.error_vendor_unsupported
import androidrestranslator.composeapp.generated.resources.vendor_baidu
import androidrestranslator.composeapp.generated.resources.vendor_huoshan
import androidrestranslator.composeapp.generated.resources.vendor_lingvanex
import androidrestranslator.composeapp.generated.resources.vendor_tencent
import androidrestranslator.composeapp.generated.resources.vendor_youdao
import `fun`.abbas.android_res_translator.core.translation.TranslationFailure
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

object AppStrings {
    fun translationFailure(
        failure: TranslationFailure,
        locale: AppLocale,
    ): String =
        runBlocking {
            withPlatformLocale(locale.tag) {
                when (failure) {
                    is TranslationFailure.NoVendorForTarget ->
                        getString(Res.string.error_no_vendor_for_target, failure.targetLanguage)
                    is TranslationFailure.NetworkFailure ->
                        getString(Res.string.error_network, failure.message)
                    is TranslationFailure.VendorRejected ->
                        vendorRejectedMessage(failure.vendorName, failure.detail)
                }
            }
        }

    private suspend fun vendorRejectedMessage(
        vendorName: String,
        detail: String,
    ): String {
        val display = vendorDisplayName(vendorName)
        if (vendorName == "lingvanex" && detail.contains("payment method", ignoreCase = true)) {
            return getString(Res.string.error_lingvanex_payment, display)
        }
        if (detail.startsWith("missing ")) {
            return getString(Res.string.error_vendor_missing_key, display, detail)
        }
        if (detail.startsWith("unsupported ")) {
            return getString(Res.string.error_vendor_unsupported, display, detail)
        }
        if (detail.startsWith("api err=")) {
            val apiMsg =
                Regex("""api err="([^"]*)"""")
                    .find(detail)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.takeIf { it.isNotBlank() }
            return if (apiMsg != null) {
                getString(Res.string.error_vendor_api, display, apiMsg)
            } else {
                getString(Res.string.error_vendor_generic, display, detail)
            }
        }
        return getString(Res.string.error_vendor_generic, display, detail)
    }

    private suspend fun vendorDisplayName(vendorName: String): String =
        when (vendorName) {
            "lingvanex" -> getString(Res.string.vendor_lingvanex)
            "huoshan" -> getString(Res.string.vendor_huoshan)
            "baidu" -> getString(Res.string.vendor_baidu)
            "youdao" -> getString(Res.string.vendor_youdao)
            "tencent" -> getString(Res.string.vendor_tencent)
            else -> vendorName
        }
}

expect suspend fun <T> withPlatformLocale(languageTag: String, block: suspend () -> T): T
