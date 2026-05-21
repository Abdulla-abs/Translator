package `fun`.abbas.android_res_translator.ui.screens.main

import `fun`.abbas.android_res_translator.core.translation.vendors.BaiduLanguageSupport
import `fun`.abbas.android_res_translator.core.translation.vendors.HuoshanLanguageSupport
import `fun`.abbas.android_res_translator.core.translation.vendors.YoudaoLanguageSupport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanguageDisplayNamesTest {
    @Test
    fun knownHuoshanCodeHasChineseName() {
        assertEquals("阿布哈兹语", LanguageDisplayNames.chineseName("ab"))
    }

    @Test
    fun formatLanguageLabelIncludesChineseNameForObscureCode() {
        assertEquals("阿布哈兹语 (AB)", formatLanguageLabel("ab"))
    }

    @Test
    fun commonCodesStillWork() {
        assertEquals("简体中文 (ZH)", formatLanguageLabel("zh"))
        assertEquals("自动识别 (AUTO)", formatLanguageLabel("auto"))
    }

    @Test
    fun baiduCodesHaveChineseNames() {
        assertEquals("阿拉伯语", LanguageDisplayNames.chineseName("ara"))
        assertEquals("爱尔兰语", LanguageDisplayNames.chineseName("gle"))
    }

    @Test
    fun allHuoshanCodesHaveChineseDisplayNames() {
        val missing =
            HuoshanLanguageSupport.supportedLanguageCodes.filter {
                LanguageDisplayNames.chineseName(it) == null
            }
        assertTrue(missing.isEmpty(), "Missing Huoshan display names: $missing")
    }

    @Test
    fun allYoudaoCodesHaveChineseDisplayNames() {
        val missing =
            YoudaoLanguageSupport.supportedLanguageCodes.filter {
                LanguageDisplayNames.chineseName(it) == null
            }
        assertTrue(missing.isEmpty(), "Missing Youdao display names: $missing")
    }

    @Test
    fun allBaiduCodesHaveChineseDisplayNames() {
        val missing =
            BaiduLanguageSupport.supportedLanguageCodes.filter {
                LanguageDisplayNames.chineseName(it) == null
            }
        assertTrue(missing.isEmpty(), "Missing Baidu display names: $missing")
    }
}
