package `fun`.abbas.android_res_translator.ui

import `fun`.abbas.android_res_translator.core.translation.TranslationFailure

fun TranslationFailure.toUserMessage(): String =
    when (this) {
        is TranslationFailure.NoVendorForTarget ->
            "No translation service supports target language: $targetLanguage"
        is TranslationFailure.VendorRejected ->
            "$vendorName: $detail"
        is TranslationFailure.NetworkFailure ->
            "Network error: $message"
    }
