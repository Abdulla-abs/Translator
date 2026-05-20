package `fun`.abbas.android_res_translator.ui.settings

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import `fun`.abbas.android_res_translator.AndroidSettingsContext

actual fun createAppSettingsRepository(): AppSettingsRepository =
    AndroidAppSettingsRepository(AndroidSettingsContext.require())

private class AndroidAppSettingsRepository(
    private val appContext: Context,
) : AppSettingsRepository {
    private val prefs =
        appContext.getSharedPreferences("android_res_translator_settings", Context.MODE_PRIVATE)

    private val _snapshot = MutableStateFlow(readFromPrefs())

    override val snapshot: StateFlow<AppSettingsSnapshot> = _snapshot.asStateFlow()

    private fun readFromPrefs(): AppSettingsSnapshot {
        val map =
            prefs.all.mapNotNull { (k, v) ->
                if (v is String) k to v else null
            }.toMap()
        return AppSettingsSnapshot.fromFlatMap(map)
    }

    override suspend fun replaceAll(settings: AppSettingsSnapshot) {
        withContext(Dispatchers.IO) {
            val editor = prefs.edit().clear()
            settings.toPersistenceMap().forEach { (k, v) -> editor.putString(k, v) }
            editor.apply()
        }
        _snapshot.value = settings
    }

    override suspend fun reloadFromDisk() {
        withContext(Dispatchers.IO) {
            _snapshot.value = readFromPrefs()
        }
    }
}
