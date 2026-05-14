package `fun`.abbas.android_res_translator.core.translation

import `fun`.abbas.android_res_translator.core.ports.SecretsProvider
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * **可选** 实网冒烟：CI 默认不跑（spec D4）。
 *
 * 本地启用：`-Dlive.vendor.smoke=true`，并设置环境变量 `TENCENT_SECRET_ID`、`TENCENT_SECRET_KEY`（链上靠前厂商无密钥时会跳过直至腾讯）。
 */
class LiveVendorSmokeJvmTest {

    @Test
    fun optionalTencentViaDefaultChain() {
        if (!"true".equals(System.getProperty("live.vendor.smoke"), ignoreCase = true)) {
            return
        }
        val sid = System.getenv("TENCENT_SECRET_ID") ?: return
        val sk = System.getenv("TENCENT_SECRET_KEY") ?: return
        val secrets =
            SecretsProvider { key ->
                when (key) {
                    "tencent.secretId" -> sid
                    "tencent.secretKey" -> sk
                    else -> null
                }
            }
        runBlocking {
            val orch = buildDefaultOrchestrator(secrets)
            val r =
                orch.translate(
                    TranslationRequest(
                        sourceText = "hello",
                        sourceLanguage = "en",
                        targetLanguage = "zh",
                    ),
                )
            assertTrue(r.successOrNull() != null, "expected success, got $r")
        }
    }
}
