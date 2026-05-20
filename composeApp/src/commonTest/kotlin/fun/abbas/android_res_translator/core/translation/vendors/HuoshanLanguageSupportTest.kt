package `fun`.abbas.android_res_translator.core.translation.vendors

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HuoshanLanguageSupportTest {
    @Test
    fun supportsBidirectionalZhEn() {
        assertTrue(HuoshanLanguageSupport.supportsLanguagePair("zh", "en"))
        assertTrue(HuoshanLanguageSupport.supportsLanguagePair("en", "zh"))
    }

    @Test
    fun slovakOnlyAsTarget() {
        assertTrue(HuoshanLanguageSupport.supportsLanguagePair("en", "sk"))
        assertFalse(HuoshanLanguageSupport.supportsLanguagePair("sk", "en"))
    }

    @Test
    fun chineseDialectsOnlyToSimplifiedChinese() {
        assertTrue(HuoshanLanguageSupport.supportsLanguagePair("bo", "zh"))
        assertFalse(HuoshanLanguageSupport.supportsLanguagePair("bo", "en"))
        assertFalse(HuoshanLanguageSupport.supportsLanguagePair("zh", "bo"))
        assertTrue(HuoshanLanguageSupport.supportsLanguagePair("yue", "zh"))
    }

    @Test
    fun normalizesAndroidAliases() {
        assertTrue(HuoshanLanguageSupport.supportsLanguagePair("zh-rTW", "en"))
        assertTrue(HuoshanLanguageSupport.supportsLanguagePair("zh", "en"))
    }
}
