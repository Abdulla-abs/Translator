package `fun`.abbas.android_res_translator.core.translation.vendors

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BaiduLanguageSupportTest {
    @Test
    fun supportsZhToEnPair() {
        assertTrue(BaiduLanguageSupport.supportsLanguagePair("zh", "en"))
    }

    @Test
    fun supportsAutoToEnPair() {
        assertTrue(BaiduLanguageSupport.supportsLanguagePair("auto", "en"))
    }

    @Test
    fun normalizesAndroidAliases() {
        assertTrue(BaiduLanguageSupport.supportsLanguagePair("zh-rTW", "en"))
        assertTrue(BaiduLanguageSupport.supportsLanguagePair("ja", "zh"))
    }

    @Test
    fun rejectsSameLanguagePair() {
        assertFalse(BaiduLanguageSupport.supportsLanguagePair("zh", "zh"))
    }

    @Test
    fun rejectsUnknownCode() {
        assertFalse(BaiduLanguageSupport.supportsTargetLanguage("not-a-lang"))
    }
}
