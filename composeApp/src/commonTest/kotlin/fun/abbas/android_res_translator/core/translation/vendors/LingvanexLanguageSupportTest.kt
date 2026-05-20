package `fun`.abbas.android_res_translator.core.translation.vendors

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LingvanexLanguageSupportTest {
    @Test
    fun resolvesAliasesToOfficialApiCodes() {
        assertEquals("zh-Hans_CN", LingvanexLanguageSupport.resolveApiTargetCode("zh-CN"))
        assertEquals("zh-Hans_CN", LingvanexLanguageSupport.resolveApiTargetCode("zh"))
        assertEquals("en_US", LingvanexLanguageSupport.resolveApiTargetCode("en-us"))
        assertEquals("fr_FR", LingvanexLanguageSupport.resolveApiTargetCode("fr"))
        assertEquals("pt_BR", LingvanexLanguageSupport.resolveApiTargetCode("pt-rBR"))
        assertEquals("zh-Hant_TW", LingvanexLanguageSupport.resolveApiTargetCode("zh-rTW"))
        assertEquals("zh-Hant_TW", LingvanexLanguageSupport.resolveApiTargetCode("zh-tw"))
    }

    @Test
    fun whitelistSizeMatchesDocumentedLanguages() {
        // 官方 NMT 列表约 100+ 种；应用侧键含 in/iw/pt-rBR 等别名，条数应显著大于旧版 20 条
        assertTrue(LingvanexLanguageSupport.supportedAppCodes.size >= 95)
    }

    @Test
    fun unknownCodeReturnsNull() {
        assertNull(LingvanexLanguageSupport.resolveApiTargetCode("xx-YY"))
    }
}
