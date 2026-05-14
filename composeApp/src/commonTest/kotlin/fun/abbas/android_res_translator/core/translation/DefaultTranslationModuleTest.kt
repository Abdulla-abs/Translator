package `fun`.abbas.android_res_translator.core.translation

import `fun`.abbas.android_res_translator.core.ports.FakeSecretsProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTranslationModuleTest {
    @Test
    fun defaultVendorChainOrder_matchesTranslationManager() {
        val engine =
            MockEngine {
                respond(
                    content = "{}",
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        HttpClient(engine).use { http ->
            val chain = defaultTranslationVendors(http, FakeSecretsProvider(emptyMap()))
            assertEquals(
                listOf("huoshan", "lingvanex", "baidu", "youdao", "tencent"),
                chain.map { it.name },
            )
        }
    }
}
