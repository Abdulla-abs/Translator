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

class HuoshanVendorTest {
    @Test
    fun parsesSuccessTranslationList() = runTest {
        val engine =
            MockEngine { _ ->
                respond(
                    content =
                        """{"TranslationList":[{"Translation":"Hello"}],"ResponseMetadata":{}}""",
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
            HuoshanVendor(
                httpClient = client,
                secrets =
                    FakeSecretsProvider(
                        mapOf(
                            "huoshan.accessKeyID" to "AK_TEST",
                            "huoshan.secretAccessKey" to "SK_TEST",
                        ),
                    ),
            )
        val r =
            vendor.translate(
                TranslationRequest(sourceText = "你好", sourceLanguage = "zh", targetLanguage = "en"),
            )
        assertTrue(r.successOrNull() != null)
        assertEquals("Hello", r.successOrNull()!!.translatedText)
    }

    @Test
    fun rejectsWhenResponseMetadataHasError() = runTest {
        val engine =
            MockEngine { _ ->
                respond(
                    content =
                        """{"TranslationList":[],"ResponseMetadata":{"Error":{"Code":"X"}}}""",
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
            HuoshanVendor(
                httpClient = client,
                secrets =
                    FakeSecretsProvider(
                        mapOf(
                            "huoshan.accessKeyID" to "AK",
                            "huoshan.secretAccessKey" to "SK",
                        ),
                    ),
            )
        val r =
            vendor.translate(
                TranslationRequest(sourceText = "a", sourceLanguage = "zh", targetLanguage = "en"),
            )
        assertTrue(r.successOrNull() == null)
    }
}
