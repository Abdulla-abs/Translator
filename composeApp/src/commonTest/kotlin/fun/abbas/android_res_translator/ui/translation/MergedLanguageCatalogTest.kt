package `fun`.abbas.android_res_translator.ui.translation

import `fun`.abbas.android_res_translator.ui.settings.AppSettingsSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MergedLanguageCatalogTest {
    @Test
    fun canonical_mergesZhAliases() {
        assertEquals("zh", LangCodeCanonicalizer.canonical("zh-cn"))
        assertEquals("zh", LangCodeCanonicalizer.canonical("zh-CHS"))
        assertEquals(
            listOf("zh", "en"),
            LangCodeCanonicalizer.mergeDistinct(listOf("zh", "zh-cn", "en")),
        )
    }

    @Test
    fun allSourceOptions_unionsConfiguredEngines() {
        val snap =
            AppSettingsSnapshot(
                huoshanAccessKeyId = "a",
                huoshanSecretAccessKey = "b",
                tencentSecretId = "t1",
                tencentSecretKey = "t2",
            )
        val huoshanOnly = LanguagePickerCatalog.sourceOptions(ActiveTranslationEngine.Huoshan, "en")
        val tencentOnly = LanguagePickerCatalog.sourceOptions(ActiveTranslationEngine.Tencent, "en")
        val merged = MergedLanguageCatalog.allSourceOptions(snap, currentTarget = "en")
        val unionCodes =
            LangCodeCanonicalizer
                .mergeDistinct(
                    (huoshanOnly + tencentOnly).map { it.code },
                ).toSet()
        assertEquals(unionCodes, merged.map { it.code }.toSet())
        assertTrue(merged.any { it.code == "zh" })
    }

    @Test
    fun allSourceOptions_emptyWhenNoEngineConfigured() {
        assertTrue(MergedLanguageCatalog.allSourceOptions(AppSettingsSnapshot(), "en").isEmpty())
    }

    @Test
    fun allTargetOptions_excludesSameAsSourceWhenUnioned() {
        val snap =
            AppSettingsSnapshot(
                huoshanAccessKeyId = "a",
                huoshanSecretAccessKey = "b",
            )
        val targets = MergedLanguageCatalog.allTargetOptions(snap, currentSource = "zh")
        assertFalse(targets.any { it.code.equals("zh", ignoreCase = true) })
    }
}
