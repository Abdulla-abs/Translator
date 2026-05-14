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
import kotlin.test.assertTrue

class TencentVendorTest {
    @Test
    fun postsToTmtHostWithJsonBody() = runTest {
        var sawHost: String? = null
        val engine =
            MockEngine { req ->
                sawHost = req.headers[HttpHeaders.Host]
                respond(
                    content =
                        """{"Response":{"TargetText":"你好","RequestId":"rid"}}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        val client =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true; isLenient = true })
                }
            }
        val vendor =
            TencentVendor(
                httpClient = client,
                secrets =
                    FakeSecretsProvider(
                        mapOf(
                            "tencent.secretId" to "AKID_TEST",
                            "tencent.secretKey" to "SK_TEST",
                        ),
                    ),
            )
        val r =
            vendor.translate(
                TranslationRequest(sourceText = "Hi", sourceLanguage = "en", targetLanguage = "zh"),
            )
        assertEquals("tmt.tencentcloudapi.com", sawHost)
        assertTrue(r.successOrNull() != null)
        assertEquals("你好", r.successOrNull()!!.translatedText)
    }

    @Test
    fun rejectsOnResponseError() = runTest {
        val engine =
            MockEngine { _ ->
                respond(
                    content =
                        """{"Response":{"Error":{"Code":"AuthFailure","Message":"sig"},"RequestId":"r"}}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        val client =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true; isLenient = true })
                }
            }
        val vendor =
            TencentVendor(
                httpClient = client,
                secrets =
                    FakeSecretsProvider(
                        mapOf(
                            "tencent.secretId" to "AK",
                            "tencent.secretKey" to "SK",
                        ),
                    ),
            )
        val r =
            vendor.translate(
                TranslationRequest(sourceText = "a", sourceLanguage = "en", targetLanguage = "zh"),
            )
        assertTrue(r.successOrNull() == null)
    }
}
