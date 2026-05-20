package `fun`.abbas.android_res_translator.core.translation.vendors

import `fun`.abbas.android_res_translator.core.ports.FakeSecretsProvider
import `fun`.abbas.android_res_translator.core.translation.TranslationRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class YoudaoVendorTest {
    @Test
    fun supportsLanguagePairFromWhitelist() {
        val vendor =
            YoudaoVendor(
                httpClient = HttpClient(MockEngine { respond("") }),
                secrets = FakeSecretsProvider(emptyMap()),
            )
        assertTrue(vendor.supportsLanguagePair("zh", "en"))
        assertTrue(vendor.supportsLanguagePair("auto", "en"))
        assertFalse(vendor.supportsLanguagePair("zh", "xx"))
    }

    @Test
    fun rejectsUnsupportedPairBeforeNetwork() = runTest {
        val engine = MockEngine { error("should not call API") }
        val vendor =
            YoudaoVendor(
                httpClient = HttpClient(engine),
                secrets =
                    FakeSecretsProvider(
                        mapOf(
                            "youdao.appId" to "app",
                            "youdao.secretKey" to "sec",
                        ),
                    ),
            )
        val r =
            vendor.translate(
                TranslationRequest(sourceText = "test", sourceLanguage = "zh", targetLanguage = "xx"),
            )
        assertNull(r.successOrNull())
        assertNotNull(r.failureOrNull())
    }

    @Test
    fun parsesSuccessJson() = runTest {
        val engine =
            MockEngine { _ ->
                respond(
                    content = """{"errorCode":"0","translation":["Hello"]}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        val client = HttpClient(engine)
        val vendor =
            YoudaoVendor(
                httpClient = client,
                secrets =
                    FakeSecretsProvider(
                        mapOf(
                            "youdao.appId" to "app",
                            "youdao.secretKey" to "sec",
                        ),
                    ),
            )
        val r = vendor.translate(TranslationRequest("hi", "zh", "en"))
        assertTrue(r.successOrNull() != null)
        assertEquals("Hello", r.successOrNull()!!.translatedText)
    }
}
