package `fun`.abbas.android_res_translator.ui.screens.compare

import kotlinx.coroutines.CoroutineScope

actual fun createCompareProjectRepository(scope: CoroutineScope): CompareProjectRepository =
    PersistentCompareProjectRepository(scope = scope)
