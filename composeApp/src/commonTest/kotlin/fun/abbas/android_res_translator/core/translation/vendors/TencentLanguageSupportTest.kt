package `fun`.abbas.android_res_translator.core.translation.vendors

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TencentLanguageSupportTest {
    @Test
    fun normalizesAndroidAliases() {
        assertEquals("zh", TencentLanguageSupport.normalizeToTencentCode("zh"))
        assertEquals("zh-TW", TencentLanguageSupport.normalizeToTencentCode("zh-rTW"))
        assertEquals("zh-TW", TencentLanguageSupport.normalizeToTencentCode("zh-TW"))
        assertEquals("id", TencentLanguageSupport.normalizeToTencentCode("in"))
        assertEquals("en", TencentLanguageSupport.normalizeToTencentCode("en"))
    }

    @Test
    fun supportsZhToEnPair() {
        assertTrue(TencentLanguageSupport.supportsLanguagePair("zh", "en"))
    }

    @Test
    fun rejectsHiToZhPair() {
        assertFalse(TencentLanguageSupport.supportsLanguagePair("hi", "zh"))
    }

    @Test
    fun supportsEnToHiPair() {
        assertTrue(TencentLanguageSupport.supportsLanguagePair("en", "hi"))
    }

    @Test
    fun rejectsSameLanguagePair() {
        assertFalse(TencentLanguageSupport.supportsLanguagePair("zh", "zh"))
        assertFalse(TencentLanguageSupport.supportsLanguagePair("en", "en"))
    }

    @Test
    fun supportsTargetLanguageUnion() {
        assertTrue(TencentLanguageSupport.supportsTargetLanguage("en"))
        assertTrue(TencentLanguageSupport.supportsTargetLanguage("hi"))
        assertFalse(TencentLanguageSupport.supportsTargetLanguage("xx"))
    }

    @Test
    fun jaSourceTargetsMatchWhitelist() {
        val expected = setOf("zh", "zh-TW", "en", "ko")
        assertEquals(expected, TencentLanguageSupport.targetsBySource["ja"])
    }
}
