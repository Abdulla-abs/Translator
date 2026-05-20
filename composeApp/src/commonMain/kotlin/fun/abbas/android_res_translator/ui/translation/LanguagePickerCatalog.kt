package `fun`.abbas.android_res_translator.ui.translation

import `fun`.abbas.android_res_translator.core.translation.vendors.BaiduLanguageSupport
import `fun`.abbas.android_res_translator.core.translation.vendors.HuoshanLanguageSupport
import `fun`.abbas.android_res_translator.core.translation.vendors.LingvanexLanguageSupport
import `fun`.abbas.android_res_translator.core.translation.vendors.TencentLanguageSupport
import `fun`.abbas.android_res_translator.core.translation.vendors.YoudaoLanguageSupport
import `fun`.abbas.android_res_translator.ui.screens.main.formatLanguageLabel
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsSnapshot

data class LanguagePickerOption(
    val code: String,
    val label: String,
)

object LanguagePickerCatalog {
    fun resolveSelectedEngine(snapshot: AppSettingsSnapshot): ActiveTranslationEngine? =
        TranslationEngineCatalog.resolveSelectedEngine(snapshot)

    fun sourceOptions(
        engine: ActiveTranslationEngine,
        currentTarget: String,
    ): List<LanguagePickerOption> =
        when (engine) {
            ActiveTranslationEngine.Huoshan -> huoshanSourceOptions(currentTarget)
            ActiveTranslationEngine.Baidu -> baiduSourceOptions(currentTarget)
            ActiveTranslationEngine.Lingvanex -> lingvanexOptions(currentTarget)
            ActiveTranslationEngine.Youdao -> youdaoSourceOptions(currentTarget)
            ActiveTranslationEngine.Tencent -> tencentSourceOptions(currentTarget)
        }

    fun targetOptions(
        engine: ActiveTranslationEngine,
        currentSource: String,
    ): List<LanguagePickerOption> =
        when (engine) {
            ActiveTranslationEngine.Huoshan -> huoshanTargetOptions(currentSource)
            ActiveTranslationEngine.Baidu -> baiduTargetOptions(currentSource)
            ActiveTranslationEngine.Lingvanex -> lingvanexOptions(currentSource)
            ActiveTranslationEngine.Youdao -> youdaoTargetOptions(currentSource)
            ActiveTranslationEngine.Tencent -> tencentTargetOptions(currentSource)
        }

    private fun huoshanSourceOptions(currentTarget: String): List<LanguagePickerOption> {
        val target = currentTarget.trim()
        return HuoshanLanguageSupport.sourceLanguageCodes()
            .map { huoshanCodeToAppCode(it) }
            .distinct()
            .filter { code ->
                target.isBlank() || HuoshanLanguageSupport.supportsLanguagePair(code, target)
            }
            .map { toOption(it) }
            .sortedBy { it.label }
    }

    private fun huoshanTargetOptions(currentSource: String): List<LanguagePickerOption> {
        val source = currentSource.trim()
        return HuoshanLanguageSupport.targetLanguageCodes()
            .map { huoshanCodeToAppCode(it) }
            .distinct()
            .filter { code ->
                source.isBlank() || HuoshanLanguageSupport.supportsLanguagePair(source, code)
            }
            .filter { code -> source.isBlank() || !code.equals(source, ignoreCase = true) }
            .map { toOption(it) }
            .sortedBy { it.label }
    }

    private fun huoshanCodeToAppCode(huoshanCode: String): String =
        when (huoshanCode) {
            "zh-Hant-tw" -> "zh-rTW"
            "zh-Hant-hk" -> "zh-rHK"
            else -> huoshanCode
        }

    private fun lingvanexOptions(otherLang: String): List<LanguagePickerOption> {
        val other = otherLang.trim()
        val apiOther = LingvanexLanguageSupport.resolveApiTargetCode(other)
        return LingvanexLanguageSupport.supportedAppCodes
            .filter { code ->
                when {
                    apiOther != null ->
                        LingvanexLanguageSupport.resolveApiTargetCode(code) != apiOther
                    else -> !code.equals(other, ignoreCase = true)
                }
            }
            .map { code -> toOption(code) }
            .sortedBy { it.label }
    }

    private fun tencentSourceOptions(currentTarget: String): List<LanguagePickerOption> {
        val target = currentTarget.trim()
        val codes =
            if (target.isBlank()) {
                TencentLanguageSupport.supportedSources.toList()
            } else {
                val normalizedTarget =
                    TencentLanguageSupport.normalizeToTencentCode(target) ?: return emptyList()
                TencentLanguageSupport.targetsBySource
                    .filter { (_, targets) -> normalizedTarget in targets }
                    .keys
                    .toList()
            }
        return codes
            .map { tencentCodeToAppCode(it) }
            .distinct()
            .filter { app ->
                target.isBlank() ||
                    TencentLanguageSupport.supportsLanguagePair(app, target)
            }
            .map { toOption(it) }
            .sortedBy { it.label }
    }

    private fun tencentTargetOptions(currentSource: String): List<LanguagePickerOption> {
        val source = currentSource.trim()
        if (source.isBlank()) {
            return TencentLanguageSupport.targetsBySource.values
                .flatten()
                .map { tencentCodeToAppCode(it) }
                .distinct()
                .map { toOption(it) }
                .sortedBy { it.label }
        }
        val tencentSource = TencentLanguageSupport.normalizeToTencentCode(source) ?: return emptyList()
        val targets = TencentLanguageSupport.targetsBySource[tencentSource].orEmpty()
        return targets
            .map { tencentCodeToAppCode(it) }
            .filter { TencentLanguageSupport.supportsLanguagePair(source, it) }
            .map { toOption(it) }
            .sortedBy { it.label }
    }

    private fun baiduSourceOptions(currentTarget: String): List<LanguagePickerOption> {
        val target = currentTarget.trim()
        val auto =
            listOf(
                LanguagePickerOption(
                    code = BaiduLanguageSupport.AUTO,
                    label = formatLanguageLabel(BaiduLanguageSupport.AUTO),
                ),
            )
        val codes =
            BAIDU_APP_CODES.filter { code ->
                target.isBlank() || BaiduLanguageSupport.supportsLanguagePair(code, target)
            }
        return auto + codes.map { toOption(it) }.sortedBy { it.label }
    }

    private fun baiduTargetOptions(currentSource: String): List<LanguagePickerOption> {
        val source = currentSource.trim()
        return BAIDU_APP_CODES
            .filter { code ->
                source.isBlank() ||
                    BaiduLanguageSupport.supportsLanguagePair(
                        if (source.isEmpty()) BaiduLanguageSupport.AUTO else source,
                        code,
                    )
            }
            .filter { code -> source.isBlank() || !code.equals(source, ignoreCase = true) }
            .map { toOption(it) }
            .sortedBy { it.label }
    }

    private val BAIDU_APP_CODES: List<String> =
        BaiduLanguageSupport.supportedLanguageCodes
            .filter { it != BaiduLanguageSupport.AUTO }
            .map { baiduCodeToAppCode(it) }
            .distinct()
            .sorted()

    private fun baiduCodeToAppCode(baiduCode: String): String =
        when (baiduCode) {
            "cht" -> "zh-rTW"
            "jp" -> "ja"
            "kor" -> "ko"
            "fra" -> "fr"
            "spa" -> "es"
            "ara" -> "ar"
            "vie" -> "vi"
            "per" -> "fa"
            "heb" -> "he"
            else -> baiduCode
        }

    private fun youdaoSourceOptions(currentTarget: String): List<LanguagePickerOption> {
        val target = currentTarget.trim()
        val auto =
            listOf(
                LanguagePickerOption(
                    code = YoudaoLanguageSupport.AUTO,
                    label = formatLanguageLabel(YoudaoLanguageSupport.AUTO),
                ),
            )
        val codes =
            YOUDAO_APP_CODES.filter { code ->
                target.isBlank() ||
                    YoudaoLanguageSupport.supportsLanguagePair(code, target)
            }
        return auto + codes.map { toOption(it) }.sortedBy { it.label }
    }

    private fun youdaoTargetOptions(currentSource: String): List<LanguagePickerOption> {
        val source = currentSource.trim()
        return YOUDAO_APP_CODES
            .filter { code ->
                source.isBlank() ||
                    YoudaoLanguageSupport.supportsLanguagePair(
                        if (source.isEmpty()) YoudaoLanguageSupport.AUTO else source,
                        code,
                    )
            }
            .filter { code -> source.isBlank() || code != source }
            .map { toOption(it) }
            .sortedBy { it.label }
    }

    /** 有道 API 码 → 应用内常用码（与别名映射一致）。 */
    private val YOUDAO_APP_CODES: List<String> =
        YoudaoLanguageSupport.supportedLanguageCodes
            .map { youdaoCodeToAppCode(it) }
            .distinct()
            .sorted()

    private fun youdaoCodeToAppCode(youdaoCode: String): String =
        when (youdaoCode) {
            "zh-CHS" -> "zh"
            "zh-CHT" -> "zh-rTW"
            else -> youdaoCode
        }

    private fun tencentCodeToAppCode(tencentCode: String): String =
        when (tencentCode) {
            "zh-TW" -> "zh-rTW"
            else -> tencentCode
        }

    private fun toOption(code: String): LanguagePickerOption =
        LanguagePickerOption(code = code, label = formatLanguageLabel(code))
}
