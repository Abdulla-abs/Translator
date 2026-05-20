package `fun`.abbas.android_res_translator.core.translation.vendors

import `fun`.abbas.android_res_translator.core.currentEpochMillis
import `fun`.abbas.android_res_translator.core.ports.SecretsProvider
import `fun`.abbas.android_res_translator.core.translation.TranslationFailure
import `fun`.abbas.android_res_translator.core.translation.TranslationOutcome
import `fun`.abbas.android_res_translator.core.translation.TranslationRequest
import `fun`.abbas.android_res_translator.core.translation.TranslationSuccess
import `fun`.abbas.android_res_translator.core.translation.TranslationVendor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 腾讯云机器翻译 `TextTranslate`（API 3.0），与旧工程 `TencentTranslation` 字段对齐：`SourceText` / `Source` / `Target` / `ProjectId`。
 */
class TencentVendor(
    private val httpClient: HttpClient,
    private val secrets: SecretsProvider,
) : TranslationVendor {

    override val name: String = "tencent"

    override fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean =
        TencentLanguageSupport.supportsTargetLanguage(isoOrAndroidCode)

    override fun supportsLanguagePair(
        sourceLanguage: String,
        targetLanguage: String,
    ): Boolean = TencentLanguageSupport.supportsLanguagePair(sourceLanguage, targetLanguage)

    override suspend fun translate(request: TranslationRequest): TranslationOutcome {
        val tencentSource =
            TencentLanguageSupport.normalizeToTencentCode(request.sourceLanguage)
        val tencentTarget =
            TencentLanguageSupport.normalizeToTencentCode(request.targetLanguage)
        if (tencentSource == null || tencentTarget == null) {
            return TranslationOutcome.Err(
                TranslationFailure.VendorRejected(
                    name,
                    "unsupported language code: source=${request.sourceLanguage}, target=${request.targetLanguage}",
                ),
            )
        }
        if (!TencentLanguageSupport.supportsLanguagePair(request.sourceLanguage, request.targetLanguage)) {
            return TranslationOutcome.Err(
                TranslationFailure.VendorRejected(
                    name,
                    "unsupported language pair: $tencentSource -> $tencentTarget",
                ),
            )
        }
        val secretId =
            secrets["tencent.secretId"]
                ?: return TranslationOutcome.Err(
                    TranslationFailure.VendorRejected(name, "missing tencent.secretId"),
                )
        val secretKey =
            secrets["tencent.secretKey"]
                ?: return TranslationOutcome.Err(
                    TranslationFailure.VendorRejected(name, "missing tencent.secretKey"),
                )
        val region = secrets["tencent.region"] ?: DEFAULT_REGION
        val bodyModel =
            TextTranslateBody(
                sourceText = request.sourceText,
                source = tencentSource,
                target = tencentTarget,
                projectId = 0L,
            )
        val bodyBytes = json.encodeToString(TextTranslateBody.serializer(), bodyModel).encodeToByteArray()
        val contentType = CONTENT_TYPE_JSON_UTF8
        val timestampSeconds = currentEpochMillis() / 1000L
        val signedHeaders =
            listOf(
                "content-type" to contentType,
                "host" to TMT_HOST,
            )
        val authorization =
            TencentTc3Signer.buildAuthorization(
                secretId = secretId,
                secretKey = secretKey,
                timestampSeconds = timestampSeconds,
                service = TMT_SERVICE,
                requestPayload = bodyBytes,
                signedHeaders = signedHeaders,
            )
        return try {
            val resp =
                httpClient.post("https://$TMT_HOST/") {
                    headers {
                        append(HttpHeaders.Host, TMT_HOST)
                        append(HttpHeaders.ContentType, contentType)
                        append(HttpHeaders.Authorization, authorization)
                        append("X-TC-Action", TMT_ACTION)
                        append("X-TC-Version", TMT_VERSION)
                        append("X-TC-Timestamp", timestampSeconds.toString())
                        append("X-TC-Region", region)
                    }
                    setBody(bodyBytes)
                }
            val envelope = resp.body<TencentTmtEnvelope>()
            val inner = envelope.response
            val err = inner?.error
            if (err != null && err.code != null) {
                TranslationOutcome.Err(
                    TranslationFailure.VendorRejected(
                        name,
                        "Tencent Error ${err.code}: ${err.message}",
                    ),
                )
            } else {
                val text = inner?.targetText
                if (text.isNullOrEmpty()) {
                    TranslationOutcome.Err(
                        TranslationFailure.VendorRejected(name, "empty TargetText"),
                    )
                } else {
                    TranslationOutcome.Ok(
                        TranslationSuccess(
                            translatedText = text,
                            resolvedSourceLanguage = request.sourceLanguage,
                            resolvedTargetLanguage = request.targetLanguage,
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            TranslationOutcome.Err(
                TranslationFailure.NetworkFailure(e.message ?: e::class.simpleName ?: "network"),
            )
        }
    }

    private companion object {
        const val TMT_HOST = "tmt.tencentcloudapi.com"
        const val TMT_SERVICE = "tmt"
        const val TMT_ACTION = "TextTranslate"
        const val TMT_VERSION = "2018-03-21"
        const val DEFAULT_REGION = "ap-guangzhou"
        const val CONTENT_TYPE_JSON_UTF8 = "application/json; charset=utf-8"

        val json =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
    }
}

@Serializable
private data class TextTranslateBody(
    @SerialName("SourceText") val sourceText: String,
    @SerialName("Source") val source: String,
    @SerialName("Target") val target: String,
    @SerialName("ProjectId") val projectId: Long,
)

@Serializable
private data class TencentTmtEnvelope(
    @SerialName("Response") val response: TencentTmtResponse? = null,
)

@Serializable
private data class TencentTmtResponse(
    @SerialName("TargetText") val targetText: String? = null,
    @SerialName("Error") val error: TencentTmtError? = null,
)

@Serializable
private data class TencentTmtError(
    @SerialName("Code") val code: String? = null,
    @SerialName("Message") val message: String? = null,
)
