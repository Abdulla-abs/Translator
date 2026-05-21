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
    /**
     * 用户将源语言设为与当前目标相同时，自动切换目标，避免 zh→zh 等无效对。
     */
    fun adjustTargetWhenSourceEqualsTarget(
        source: String,
        target: String,
    ): String {
        if (!source.trim().equals(target.trim(), ignoreCase = true)) return target
        return when (source.trim().lowercase()) {
            "zh" -> "en"
            "en" -> "zh"
            else -> "en"
        }
    }

    fun resolveSelectedEngine(snapshot: AppSettingsSnapshot): ActiveTranslationEngine? =
        TranslationEngineCatalog.resolveSelectedEngine(snapshot)

    fun sourceOptions(
        engine: ActiveTranslationEngine,
        currentTarget: String,
    ): List<LanguagePickerOption> =
        when (engine) {
            ActiveTranslationEngine.Huoshan -> huoshanSourceOptions(currentTarget)
            ActiveTranslationEngine.Baidu -> baiduSourceOptions(currentTarget)
            ActiveTranslationEngine.Lingvanex -> lingvanexSourceOptions(currentTarget)
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
            ActiveTranslationEngine.Lingvanex -> lingvanexTargetOptions(currentSource)
            ActiveTranslationEngine.Youdao -> youdaoTargetOptions(currentSource)
            ActiveTranslationEngine.Tencent -> tencentTargetOptions(currentSource)
        }

    private fun huoshanSourceOptions(currentTarget: String): List<LanguagePickerOption> {
        val sources =
            HuoshanLanguageSupport.sourceLanguageCodes()
                .map { huoshanCodeToAppCode(it) }
                .distinct()
        val targets =
            HuoshanLanguageSupport.targetLanguageCodes()
                .map { huoshanCodeToAppCode(it) }
                .distinct()
        return sources
            .filter {
                allowsSourceOption(it, currentTarget, targets, HuoshanLanguageSupport::supportsLanguagePair)
            }
            .map { toOption(it) }
            .sortedByCodeAz()
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
            .sortedByCodeAz()
    }

    private fun huoshanCodeToAppCode(huoshanCode: String): String =
        when (huoshanCode) {
            "zh-Hant-tw" -> "zh-rTW"
            "zh-Hant-hk" -> "zh-rHK"
            else -> huoshanCode
        }

    private fun lingvanexSourceOptions(currentTarget: String): List<LanguagePickerOption> =
        LingvanexLanguageSupport.supportedAppCodes
            .filter { code ->
                allowsSourceOption(
                    code,
                    currentTarget,
                    LingvanexLanguageSupport.supportedAppCodes,
                    ::lingvanexSupportsPair,
                )
            }
            .map { toOption(it) }
            .sortedByCodeAz()

    private fun lingvanexTargetOptions(currentSource: String): List<LanguagePickerOption> {
        val other = currentSource.trim()
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
            .sortedByCodeAz()
    }

    private fun lingvanexSupportsPair(
        source: String,
        target: String,
    ): Boolean {
        val apiSource = LingvanexLanguageSupport.resolveApiTargetCode(source) ?: return false
        val apiTarget = LingvanexLanguageSupport.resolveApiTargetCode(target) ?: return false
        return apiSource != apiTarget
    }

    private fun tencentSourceOptions(currentTarget: String): List<LanguagePickerOption> {
        val targets =
            TencentLanguageSupport.targetsBySource.values
                .flatten()
                .map { tencentCodeToAppCode(it) }
                .distinct()
        return TencentLanguageSupport.supportedSources
            .map { tencentCodeToAppCode(it) }
            .distinct()
            .filter {
                allowsSourceOption(it, currentTarget, targets, TencentLanguageSupport::supportsLanguagePair)
            }
            .map { toOption(it) }
            .sortedByCodeAz()
    }

    private fun tencentTargetOptions(currentSource: String): List<LanguagePickerOption> {
        val source = currentSource.trim()
        if (source.isBlank()) {
            return TencentLanguageSupport.targetsBySource.values
                .flatten()
                .map { tencentCodeToAppCode(it) }
                .distinct()
                .map { toOption(it) }
                .sortedByCodeAz()
        }
        val tencentSource = TencentLanguageSupport.normalizeToTencentCode(source) ?: return emptyList()
        val targets = TencentLanguageSupport.targetsBySource[tencentSource].orEmpty()
        return targets
            .map { tencentCodeToAppCode(it) }
            .filter { TencentLanguageSupport.supportsLanguagePair(source, it) }
            .map { toOption(it) }
            .sortedByCodeAz()
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
                allowsSourceOption(code, target, BAIDU_APP_CODES, BaiduLanguageSupport::supportsLanguagePair)
            }
        return (auto + codes.map { toOption(it) }).sortedByCodeAz()
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
            .sortedByCodeAz()
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
                allowsSourceOption(code, target, YOUDAO_APP_CODES, YoudaoLanguageSupport::supportsLanguagePair)
            }
        return (auto + codes.map { toOption(it) }).sortedByCodeAz()
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
            .sortedByCodeAz()
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

    /**
     * 源语言是否应出现在选择器中。
     * 当当前目标与源相同（如默认 en→zh 下要选源语言 zh）时，只要该源可译向其它目标即保留。
     */
    private fun allowsSourceOption(
        sourceCode: String,
        currentTarget: String,
        candidateTargets: Iterable<String>,
        supportsPair: (String, String) -> Boolean,
    ): Boolean {
        val target = currentTarget.trim()
        if (target.isEmpty()) {
            return candidateTargets.any { t ->
                !sourceCode.equals(t, ignoreCase = true) && supportsPair(sourceCode, t)
            }
        }
        if (sourceCode.equals(target, ignoreCase = true)) {
            return candidateTargets.any { t ->
                !t.equals(sourceCode, ignoreCase = true) && supportsPair(sourceCode, t)
            }
        }
        return supportsPair(sourceCode, target)
    }

    /** 语言选择器列表按语言代码 A–Z（不区分大小写）排序。 */
    private fun List<LanguagePickerOption>.sortedByCodeAz(): List<LanguagePickerOption> =
        sortedBy { it.code.lowercase() }
}
