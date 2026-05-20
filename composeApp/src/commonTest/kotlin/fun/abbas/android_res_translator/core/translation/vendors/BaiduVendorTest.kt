package `fun`.abbas.android_res_translator.core.translation.vendors

import `fun`.abbas.android_res_translator.core.ports.FakeSecretsProvider
import `fun`.abbas.android_res_translator.core.translation.TranslationRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BaiduVendorTest {
    @Test
    fun supportsLanguagePairFromWhitelist() {
        val vendor =
            BaiduVendor(
                httpClient = HttpClient(MockEngine { respond("") }),
                secrets = FakeSecretsProvider(emptyMap()),
            )
        assertTrue(vendor.supportsLanguagePair("zh", "en"))
        assertTrue(vendor.supportsLanguagePair("auto", "en"))
        assertFalse(vendor.supportsTargetLanguage("invalid"))
    }

    @Test
    fun rejectsUnsupportedPairBeforeNetwork() = runTest {
        val engine = MockEngine { error("should not call API") }
        val vendor =
            BaiduVendor(
                httpClient = HttpClient(engine),
                secrets =
                    FakeSecretsProvider(
                        mapOf(
                            "baidu.appId" to "id",
                            "baidu.secretKey" to "sec",
                        ),
                    ),
            )
        val r = vendor.translate(TranslationRequest("x", "zh", "not-a-lang"))
        assertNull(r.successOrNull())
        assertNotNull(r.failureOrNull())
    }

    @Test
    fun parsesSuccessJson() = runTest {
        val engine =
            MockEngine { _ ->
                respond(
                    content = """{"from":"zh","to":"en","trans_result":[{"src":"hi","dst":"HELLO"}]}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        val client =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        },
                    )
                }
            }
        val vendor =
            BaiduVendor(
                httpClient = client,
                secrets =
                    FakeSecretsProvider(
                        mapOf(
                            "baidu.appId" to "id",
                            "baidu.secretKey" to "sec",
                        ),
                    ),
            )
        val r = vendor.translate(TranslationRequest("hi", "zh", "en"))
        assertTrue(r.successOrNull() != null)
        assertEquals("HELLO", r.successOrNull()!!.translatedText)
    }
}
