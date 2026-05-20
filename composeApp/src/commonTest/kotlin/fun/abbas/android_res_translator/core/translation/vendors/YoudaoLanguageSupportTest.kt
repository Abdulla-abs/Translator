package `fun`.abbas.android_res_translator.core.translation.vendors

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class YoudaoLanguageSupportTest {
    @Test
    fun normalizesAndroidAliases() {
        assertEquals("zh-CHS", YoudaoLanguageSupport.normalizeToYoudaoCode("zh"))
        assertEquals("zh-CHT", YoudaoLanguageSupport.normalizeToYoudaoCode("zh-rTW"))
        assertEquals("id", YoudaoLanguageSupport.normalizeToYoudaoCode("in"))
        assertEquals("en", YoudaoLanguageSupport.normalizeToYoudaoCode("en"))
        assertEquals(YoudaoLanguageSupport.AUTO, YoudaoLanguageSupport.normalizeToYoudaoCode("auto"))
    }

    @Test
    fun supportsZhChsToEnPair() {
        assertTrue(YoudaoLanguageSupport.supportsLanguagePair("zh", "en"))
    }

    @Test
    fun supportsAutoToEnPair() {
        assertTrue(YoudaoLanguageSupport.supportsLanguagePair("auto", "en"))
    }

    @Test
    fun rejectsSameLanguagePair() {
        assertFalse(YoudaoLanguageSupport.supportsLanguagePair("zh", "zh"))
        assertFalse(YoudaoLanguageSupport.supportsLanguagePair("zh-CHS", "zh-CHS"))
    }

    @Test
    fun rejectsUnknownCode() {
        assertFalse(YoudaoLanguageSupport.supportsTargetLanguage("xx"))
        assertFalse(YoudaoLanguageSupport.supportsLanguagePair("zh", "xx"))
    }

    @Test
    fun includesSerbianVariants() {
        assertTrue(YoudaoLanguageSupport.supportedLanguageCodes.contains("sr-Cyrl"))
        assertTrue(YoudaoLanguageSupport.supportedLanguageCodes.contains("sr-Latn"))
        assertTrue(YoudaoLanguageSupport.supportsLanguagePair("sr-Cyrl", "en"))
    }
}
