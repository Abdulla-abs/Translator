package `fun`.abbas.android_res_translator.ui

import `fun`.abbas.android_res_translator.core.resources.consumer.TranslationSegmentPort
import `fun`.abbas.android_res_translator.core.text.TranslatePlainTextUseCase
import `fun`.abbas.android_res_translator.core.translation.TranslationOrchestrator
import `fun`.abbas.android_res_translator.core.translation.TranslationRequest
import `fun`.abbas.android_res_translator.core.translation.createJsonHttpClient
import `fun`.abbas.android_res_translator.core.translation.defaultTranslationVendors
import `fun`.abbas.android_res_translator.core.ports.SecretsProvider
import io.ktor.client.HttpClient

/**
 * 进程内共享 [HttpClient] 与 [TranslationOrchestrator]；[secrets] 每次请求时读取当前配置。
 */
class TranslationServices(
    secrets: SecretsProvider,
    val httpClient: HttpClient = createJsonHttpClient(),
) {
    private val orchestrator =
        TranslationOrchestrator(defaultTranslationVendors(httpClient, secrets))

    private val translatePlainTextUseCase = TranslatePlainTextUseCase(orchestrator)

    suspend fun translatePlainText(
        text: String,
        from: String,
        to: String,
        preferredVendorName: String? = null,
    ) = translatePlainTextUseCase(text, from, to, preferredVendorName)

    fun segmentPort(preferredVendorName: String? = null): TranslationSegmentPort =
        TranslationSegmentPort { text, from, to ->
            orchestrator.translate(TranslationRequest(text, from, to), preferredVendorName)
        }

    fun close() {
        httpClient.close()
    }
}
