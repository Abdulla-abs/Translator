package `fun`.abbas.android_res_translator.core.translation.vendors

import `fun`.abbas.android_res_translator.core.ports.SecretsProvider
import `fun`.abbas.android_res_translator.core.translation.TranslationDebugLog
import `fun`.abbas.android_res_translator.core.translation.TranslationFailure
import `fun`.abbas.android_res_translator.core.translation.TranslationOutcome
import `fun`.abbas.android_res_translator.core.translation.TranslationRequest
import `fun`.abbas.android_res_translator.core.translation.TranslationSuccess
import `fun`.abbas.android_res_translator.core.translation.TranslationVendor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/**
 * Lingvanex / Backenster 翻译 API（与旧工程 `LingvanexTranslation` 对齐）。
 * 目标语言码见 [LingvanexLanguageSupport]；未解析出的目标语言视为本厂商不支持。
 */
class LingvanexVendor(
    private val httpClient: HttpClient,
    private val secrets: SecretsProvider,
) : TranslationVendor {

    override val name: String = "lingvanex"

    override fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean =
        LingvanexLanguageSupport.supportsAppTargetLanguage(isoOrAndroidCode)

    override suspend fun translate(request: TranslationRequest): TranslationOutcome {
        val token = secrets["lingvanex.token"]
            ?: return TranslationOutcome.Err(
                TranslationFailure.VendorRejected(name, "missing lingvanex.token"),
            )
        val mappedTo =
            LingvanexLanguageSupport.resolveApiTargetCode(request.targetLanguage)
                ?: run {
                    TranslationDebugLog.log(
                        "Lingvanex",
                        "unsupported target=${request.targetLanguage} (no API mapping)",
                    )
                    return TranslationOutcome.Err(
                        TranslationFailure.VendorRejected(
                            name,
                            "unsupported target language: ${request.targetLanguage}",
                        ),
                    )
                }
        TranslationDebugLog.log(
            "Lingvanex",
            "target=${request.targetLanguage} -> apiTo=$mappedTo tokenConfigured=${token.isNotBlank()}",
        )
        return try {
            val resp =
                httpClient.post(LINGVANEX_TRANSLATE_URL) {
                    contentType(ContentType.Application.Json)
                    header("Accept", "application/json")
                    header("Authorization", token)
                    setBody(
                        LingvanexTranslateRequest(
                            data = request.sourceText,
                            to = mappedTo,
                        ),
                    )
                }
            val body = resp.body<LingvanexTranslateResponse>()
            if (body.hasErr()) {
                TranslationOutcome.Err(
                    TranslationFailure.VendorRejected(
                        name,
                        "api err=${body.err} result=${body.result}",
                    ),
                )
            } else {
                TranslationOutcome.Ok(
                    TranslationSuccess(
                        translatedText = body.result,
                        resolvedSourceLanguage = body.from.ifEmpty { request.sourceLanguage },
                        resolvedTargetLanguage = request.targetLanguage,
                    ),
                )
            }
        } catch (e: Exception) {
            TranslationOutcome.Err(
                TranslationFailure.NetworkFailure(e.message ?: e::class.simpleName ?: "network"),
            )
        }
    }

    private companion object {
        private const val LINGVANEX_TRANSLATE_URL = "https://api-b2b.backenster.com/b1/api/v3/translate"
    }
}

@Serializable
private data class LingvanexTranslateRequest(
    val platform: String = "api",
    val data: String,
    val to: String,
)

@Serializable
private data class LingvanexTranslateResponse(
    val result: String = "",
    val from: String = "",
    @SerialName("err")
    val err: JsonElement? = null,
) {
    fun hasErr(): Boolean =
        when (err) {
            null, JsonNull -> false
            else -> true
        }
}
