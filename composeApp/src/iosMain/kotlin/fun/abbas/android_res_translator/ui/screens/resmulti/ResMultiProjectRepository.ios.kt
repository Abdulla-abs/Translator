package `fun`.abbas.android_res_translator.ui.screens.resmulti

import kotlinx.coroutines.CoroutineScope

actual fun createResMultiProjectRepository(scope: CoroutineScope): ResMultiProjectRepository =
    PersistentResMultiProjectRepository(scope = scope)
