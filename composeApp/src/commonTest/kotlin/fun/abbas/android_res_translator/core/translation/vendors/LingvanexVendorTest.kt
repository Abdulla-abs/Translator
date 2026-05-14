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
import kotlin.test.assertTrue

class LingvanexVendorTest {
    @Test
    fun supportsMappedTargetsOnly() {
        val engine =
            MockEngine { _ ->
                respond(
                    content = """{}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        val client = HttpClient(engine) { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }
        val v = LingvanexVendor(client, FakeSecretsProvider(mapOf("lingvanex.token" to "t")))
        assertTrue(v.supportsTargetLanguage("en"))
        assertTrue(v.supportsTargetLanguage("zh"))
        assertFalse(v.supportsTargetLanguage("unknown-locale"))
    }

    @Test
    fun parsesSuccessWithoutErr() = runTest {
        val engine =
            MockEngine { _ ->
                respond(
                    content = """{"result":"Hola","from":"zh"}""",
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
            LingvanexVendor(
                httpClient = client,
                secrets =
                    FakeSecretsProvider(
                        mapOf("lingvanex.token" to "Bearer-test-token"),
                    ),
            )
        val r =
            vendor.translate(
                TranslationRequest(sourceText = "你好", sourceLanguage = "zh", targetLanguage = "es"),
            )
        assertTrue(r.successOrNull() != null)
        assertEquals("Hola", r.successOrNull()!!.translatedText)
    }
}
