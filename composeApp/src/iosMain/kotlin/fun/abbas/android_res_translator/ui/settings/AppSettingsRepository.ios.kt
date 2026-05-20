package `fun`.abbas.android_res_translator.ui.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

actual fun createAppSettingsRepository(): AppSettingsRepository = IosAppSettingsRepository()

private class IosAppSettingsRepository : AppSettingsRepository {
    private val defaults = NSUserDefaults.standardUserDefaults
    private val _snapshot = MutableStateFlow(readFromDefaults())

    override val snapshot: StateFlow<AppSettingsSnapshot> = _snapshot.asStateFlow()

    private fun readFromDefaults(): AppSettingsSnapshot {
        val keys =
            listOf(
                "lingvanex.token",
                "tencent.secretId",
                "tencent.secretKey",
                "tencent.region",
                "baidu.appId",
                "baidu.secretKey",
                "youdao.appId",
                "youdao.secretKey",
                "huoshan.accessKeyID",
                "huoshan.secretAccessKey",
                AppSettingsSnapshot.KEY_DEFAULT_SOURCE,
                AppSettingsSnapshot.KEY_DEFAULT_TARGET,
                AppSettingsSnapshot.KEY_PREFERRED_ENGINE,
                AppSettingsSnapshot.KEY_APP_APPEARANCE,
                AppSettingsSnapshot.KEY_CONSUMER_MODE,
                AppSettingsSnapshot.KEY_FORCE_TRANSLATION,
            )
        val map = keys.associateWith { defaults.stringForKey(it) ?: "" }
        return AppSettingsSnapshot.fromFlatMap(map)
    }

    override suspend fun replaceAll(settings: AppSettingsSnapshot) {
        withContext(Dispatchers.Default) {
            settings.toPersistenceMap().forEach { (k, v) ->
                defaults.setObject(v, forKey = k)
            }
        }
        _snapshot.value = settings
    }

    override suspend fun reloadFromDisk() {
        withContext(Dispatchers.Default) {
            _snapshot.value = readFromDefaults()
        }
    }
}
