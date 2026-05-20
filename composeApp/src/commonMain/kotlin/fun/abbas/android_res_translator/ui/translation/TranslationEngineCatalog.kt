package `fun`.abbas.android_res_translator.ui.translation

import `fun`.abbas.android_res_translator.ui.settings.AppSettingsSnapshot

data class TranslationEngineOption(
    val engine: ActiveTranslationEngine,
    val label: String,
    val isConfigured: Boolean,
)

object TranslationEngineCatalog {
    val allEngines: List<ActiveTranslationEngine> = ActiveTranslationEngine.entries

    fun isConfigured(
        snapshot: AppSettingsSnapshot,
        engine: ActiveTranslationEngine,
    ): Boolean =
        when (engine) {
            ActiveTranslationEngine.Huoshan ->
                snapshot.huoshanAccessKeyId.isNotBlank() &&
                    snapshot.huoshanSecretAccessKey.isNotBlank()
            ActiveTranslationEngine.Lingvanex -> snapshot.lingvanexToken.isNotBlank()
            ActiveTranslationEngine.Baidu ->
                snapshot.baiduAppId.isNotBlank() && snapshot.baiduSecretKey.isNotBlank()
            ActiveTranslationEngine.Youdao ->
                snapshot.youdaoAppId.isNotBlank() && snapshot.youdaoSecretKey.isNotBlank()
            ActiveTranslationEngine.Tencent ->
                snapshot.tencentSecretId.isNotBlank() && snapshot.tencentSecretKey.isNotBlank()
        }

    fun engineOptions(snapshot: AppSettingsSnapshot): List<TranslationEngineOption> =
        allEngines.map { engine ->
            TranslationEngineOption(
                engine = engine,
                label =
                    if (isConfigured(snapshot, engine)) {
                        engine.displayName
                    } else {
                        "${engine.displayName}（未配置）"
                    },
                isConfigured = isConfigured(snapshot, engine),
            )
        }

    /** 用户首选引擎（须已配置密钥）；否则回退到链上首个已配置引擎。 */
    fun resolveSelectedEngine(snapshot: AppSettingsSnapshot): ActiveTranslationEngine? {
        val preferred = snapshot.preferredTranslationEngine
        if (preferred != null) {
            if (isConfigured(snapshot, preferred)) return preferred
            return allEngines.firstOrNull { isConfigured(snapshot, it) }
        }
        return allEngines.firstOrNull { isConfigured(snapshot, it) }
    }
}
