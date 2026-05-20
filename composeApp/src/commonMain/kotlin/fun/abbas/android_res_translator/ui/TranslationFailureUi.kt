package `fun`.abbas.android_res_translator.ui

import `fun`.abbas.android_res_translator.core.translation.TranslationFailure
import `fun`.abbas.android_res_translator.ui.i18n.AppLocale
import `fun`.abbas.android_res_translator.ui.i18n.AppStrings
fun TranslationFailure.toUserMessage(locale: AppLocale): String =
    AppStrings.translationFailure(this, locale)
