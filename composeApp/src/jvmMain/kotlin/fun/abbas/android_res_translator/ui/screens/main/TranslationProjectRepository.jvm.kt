package `fun`.abbas.android_res_translator.ui.screens.main

import kotlinx.coroutines.CoroutineScope

actual fun createTranslationProjectRepository(scope: CoroutineScope): TranslationProjectRepository =
    PersistentTranslationProjectRepository(scope = scope)
