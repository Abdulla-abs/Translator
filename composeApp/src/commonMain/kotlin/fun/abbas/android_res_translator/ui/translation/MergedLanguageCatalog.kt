package `fun`.abbas.android_res_translator.ui.translation

import `fun`.abbas.android_res_translator.ui.screens.main.formatLanguageLabel
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsSnapshot

/**
 * 设置页「本地化默认」：合并所有已配置翻译引擎支持的语言列表（与主页按单引擎过滤不同）。
 */
object MergedLanguageCatalog {
    fun configuredEngines(snapshot: AppSettingsSnapshot): List<ActiveTranslationEngine> =
        TranslationEngineCatalog.allEngines.filter { TranslationEngineCatalog.isConfigured(snapshot, it) }

    fun allSourceOptions(
        snapshot: AppSettingsSnapshot,
        currentTarget: String,
    ): List<LanguagePickerOption> {
        val engines = configuredEngines(snapshot)
        if (engines.isEmpty()) return emptyList()
        val codes = linkedSetOf<String>()
        for (engine in engines) {
            LanguagePickerCatalog
                .sourceOptions(engine, currentTarget)
                .forEach { codes.add(it.code) }
        }
        return LangCodeCanonicalizer
            .mergeDistinct(codes)
            .map { code -> LanguagePickerOption(code = code, label = formatLanguageLabel(code)) }
            .sortedBy { it.code.lowercase() }
    }

    fun allTargetOptions(
        snapshot: AppSettingsSnapshot,
        currentSource: String,
    ): List<LanguagePickerOption> {
        val engines = configuredEngines(snapshot)
        if (engines.isEmpty()) return emptyList()
        val codes = linkedSetOf<String>()
        for (engine in engines) {
            LanguagePickerCatalog
                .targetOptions(engine, currentSource)
                .forEach { codes.add(it.code) }
        }
        return LangCodeCanonicalizer
            .mergeDistinct(codes)
            .map { code -> LanguagePickerOption(code = code, label = formatLanguageLabel(code)) }
            .sortedBy { it.code.lowercase() }
    }
}
