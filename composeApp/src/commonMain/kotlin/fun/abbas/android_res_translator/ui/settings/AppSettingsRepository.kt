package `fun`.abbas.android_res_translator.ui.settings

import `fun`.abbas.android_res_translator.core.ports.SecretsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
interface AppSettingsRepository {
    val snapshot: StateFlow<AppSettingsSnapshot>

    suspend fun replaceAll(settings: AppSettingsSnapshot)

    suspend fun reloadFromDisk()
}

class RepositorySecretsProvider(
    private val snapshotFlow: StateFlow<AppSettingsSnapshot>,
) : SecretsProvider {
    override operator fun get(key: String): String? = snapshotFlow.value.toSecretsMap()[key]
}

class InMemoryAppSettingsRepository(
    initial: AppSettingsSnapshot = AppSettingsSnapshot(),
) : AppSettingsRepository {
    private val _snapshot = MutableStateFlow(initial)

    override val snapshot: StateFlow<AppSettingsSnapshot> = _snapshot.asStateFlow()

    override suspend fun replaceAll(settings: AppSettingsSnapshot) {
        _snapshot.value = settings
    }

    override suspend fun reloadFromDisk() {
        // no-op
    }
}

/** 桌面预览或未注入仓库时的占位。 */
fun previewAppSettingsRepository(): AppSettingsRepository = InMemoryAppSettingsRepository()

expect fun createAppSettingsRepository(): AppSettingsRepository
