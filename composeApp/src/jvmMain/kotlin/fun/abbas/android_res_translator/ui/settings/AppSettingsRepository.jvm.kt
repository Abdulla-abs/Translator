package `fun`.abbas.android_res_translator.ui.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Properties

actual fun createAppSettingsRepository(): AppSettingsRepository = JvmAppSettingsRepository()

private class JvmAppSettingsRepository : AppSettingsRepository {
    private val file =
        File(
            System.getProperty("user.home"),
            ".android_res_translator${File.separator}settings.properties",
        )

    private val _snapshot = MutableStateFlow(AppSettingsSnapshot())

    override val snapshot: StateFlow<AppSettingsSnapshot> = _snapshot.asStateFlow()

    init {
        loadBlocking()
    }

    private fun loadBlocking() {
        try {
            if (!file.exists()) return
            val props = Properties()
            file.reader(Charsets.UTF_8).use { reader -> props.load(reader) }
            val map = buildMap {
                props.stringPropertyNames().forEach { k ->
                    props.getProperty(k)?.let { put(k, it) }
                }
            }
            _snapshot.value = AppSettingsSnapshot.fromFlatMap(map)
        } catch (_: Exception) {
            // 保持默认
        }
    }

    override suspend fun replaceAll(settings: AppSettingsSnapshot) {
        withContext(Dispatchers.IO) {
            file.parentFile?.mkdirs()
            val props = Properties()
            settings.toPersistenceMap().forEach { (k, v) -> props.setProperty(k, v) }
            file.writer(Charsets.UTF_8).use { writer -> props.store(writer, null) }
        }
        _snapshot.value = settings
    }

    override suspend fun reloadFromDisk() {
        withContext(Dispatchers.IO) { loadBlocking() }
    }
}
