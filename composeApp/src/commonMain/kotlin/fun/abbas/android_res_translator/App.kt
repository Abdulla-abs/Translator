package `fun`.abbas.android_res_translator

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import `fun`.abbas.android_res_translator.ui.AppRoot
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsRepository
import `fun`.abbas.android_res_translator.ui.settings.createAppSettingsRepository
import `fun`.abbas.android_res_translator.ui.settings.previewAppSettingsRepository

@Composable
fun App(settingsRepository: AppSettingsRepository = createAppSettingsRepository()) {
    AppRoot(settingsRepository)
}

@Preview
@Composable
fun AppPreview() {
    AppRoot(previewAppSettingsRepository())
}
