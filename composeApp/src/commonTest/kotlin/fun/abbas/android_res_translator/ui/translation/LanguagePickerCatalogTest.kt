package `fun`.abbas.android_res_translator.ui.translation

import `fun`.abbas.android_res_translator.ui.settings.AppSettingsSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LanguagePickerCatalogTest {
    @Test
    fun resolvesPreferredEngineWhenConfigured() {
        val snap =
            AppSettingsSnapshot(
                huoshanAccessKeyId = "a",
                huoshanSecretAccessKey = "b",
                youdaoAppId = "y",
                youdaoSecretKey = "z",
                preferredTranslationEngine = ActiveTranslationEngine.Youdao,
            )
        assertEquals(ActiveTranslationEngine.Youdao, LanguagePickerCatalog.resolveSelectedEngine(snap))
    }

    @Test
    fun fallsBackWhenPreferredEngineNotConfigured() {
        val snap =
            AppSettingsSnapshot(
                lingvanexToken = "token",
                preferredTranslationEngine = ActiveTranslationEngine.Tencent,
            )
        assertEquals(ActiveTranslationEngine.Lingvanex, LanguagePickerCatalog.resolveSelectedEngine(snap))
    }

    @Test
    fun resolvesFirstConfiguredWhenNoPreference() {
        val snap =
            AppSettingsSnapshot(
                huoshanAccessKeyId = "a",
                huoshanSecretAccessKey = "b",
                youdaoAppId = "y",
                youdaoSecretKey = "z",
            )
        assertEquals(ActiveTranslationEngine.Huoshan, LanguagePickerCatalog.resolveSelectedEngine(snap))
    }

    @Test
    fun tencentSourceOptionsRespectTarget() {
        val options =
            LanguagePickerCatalog.sourceOptions(ActiveTranslationEngine.Tencent, currentTarget = "fr")
        assertTrue(options.any { it.code == "zh" })
        assertFalse(options.any { it.code == "ja" })
    }

    @Test
    fun simplifiedChineseAvailableAsSourceWhenTargetIsZh() {
        val engines =
            listOf(
                ActiveTranslationEngine.Huoshan,
                ActiveTranslationEngine.Baidu,
                ActiveTranslationEngine.Youdao,
                ActiveTranslationEngine.Tencent,
                ActiveTranslationEngine.Lingvanex,
            )
        for (engine in engines) {
            val options =
                LanguagePickerCatalog.sourceOptions(engine, currentTarget = "zh")
            assertTrue(
                options.any { it.code == "zh" },
                "engine=$engine should list zh as source when target is zh",
            )
        }
    }

    @Test
    fun adjustTargetWhenSourceEqualsTarget() {
        assertEquals("en", LanguagePickerCatalog.adjustTargetWhenSourceEqualsTarget("zh", "zh"))
        assertEquals("zh", LanguagePickerCatalog.adjustTargetWhenSourceEqualsTarget("en", "en"))
        assertEquals("fr", LanguagePickerCatalog.adjustTargetWhenSourceEqualsTarget("zh", "fr"))
    }

    @Test
    fun youdaoIncludesAutoForSource() {
        val options =
            LanguagePickerCatalog.sourceOptions(ActiveTranslationEngine.Youdao, currentTarget = "en")
        assertTrue(options.any { it.code == "auto" })
    }

    @Test
    fun huoshanReturnsOptionsWhenConfigured() {
        assertTrue(
            LanguagePickerCatalog.sourceOptions(ActiveTranslationEngine.Huoshan, "en").isNotEmpty(),
        )
        assertTrue(
            LanguagePickerCatalog.targetOptions(ActiveTranslationEngine.Huoshan, "zh").isNotEmpty(),
        )
        assertFalse(
            LanguagePickerCatalog.targetOptions(ActiveTranslationEngine.Huoshan, "bo")
                .any { it.code == "en" },
        )
    }

    @Test
    fun optionsSortedByCodeAz() {
        val options =
            LanguagePickerCatalog.sourceOptions(ActiveTranslationEngine.Huoshan, currentTarget = "en")
        val codes = options.map { it.code.lowercase() }
        assertEquals(codes.sorted(), codes)
        assertTrue(options.all { it.label.startsWith("(") })
    }

    @Test
    fun baiduReturnsLanguageOptions() {
        assertTrue(
            LanguagePickerCatalog.sourceOptions(ActiveTranslationEngine.Baidu, "en").isNotEmpty(),
        )
        assertTrue(
            LanguagePickerCatalog.sourceOptions(ActiveTranslationEngine.Baidu, "en")
                .any { it.code == "auto" },
        )
        assertTrue(
            LanguagePickerCatalog.targetOptions(ActiveTranslationEngine.Baidu, "zh").isNotEmpty(),
        )
    }
}
