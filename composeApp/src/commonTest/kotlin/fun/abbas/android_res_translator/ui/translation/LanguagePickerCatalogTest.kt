package `fun`.abbas.android_res_translator.ui.translation

import `fun`.abbas.android_res_translator.ui.settings.AppSettingsSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LanguagePickerCatalogTest {
    @Test
    fun resolvesPreferredEngineOverFirstConfigured() {
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
