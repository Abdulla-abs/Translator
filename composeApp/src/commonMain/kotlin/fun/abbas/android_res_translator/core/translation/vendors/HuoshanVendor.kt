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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/**
 * 火山引擎机器翻译 `TranslateText`（OpenAPI，与旧工程 `HuoshanTranslation` 语义对齐：`TextList`、源/目标语言）。
 * 使用 `open.volcengineapi.com` + HMAC-SHA256 签名（见 [VolcengineOpenApiSign]）。
 */
class HuoshanVendor(
    private val httpClient: HttpClient,
    private val secrets: SecretsProvider,
) : TranslationVendor {

    override val name: String = "huoshan"

    override fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean =
        isoOrAndroidCode.isNotBlank()

    override suspend fun translate(request: TranslationRequest): TranslationOutcome {
        val accessKeyId =
            secrets["huoshan.accessKeyID"]
                ?: return TranslationOutcome.Err(
                    TranslationFailure.VendorRejected(name, "missing huoshan.accessKeyID"),
                )
        val secretAccessKey =
            secrets["huoshan.secretAccessKey"]
                ?: return TranslationOutcome.Err(
                    TranslationFailure.VendorRejected(name, "missing huoshan.secretAccessKey"),
                )
        val bodyModel =
            HuoshanTranslateBody(
                sourceLanguage = request.sourceLanguage.ifBlank { null },
                targetLanguage = request.targetLanguage,
                textList = listOf(request.sourceText),
            )
        val bodyBytes = json.encodeToString(HuoshanTranslateBody.serializer(), bodyModel).encodeToByteArray()
        val contentType = "application/json"
        val xDate = volcengineXDateUtc(currentEpochMillis())
        val auth =
            VolcengineOpenApiSign.buildAuthorization(
                accessKeyId = accessKeyId,
                secretAccessKey = secretAccessKey,
                region = HUOSHAN_REGION,
                service = HUOSHAN_SERVICE,
                method = "POST",
                host = HUOSHAN_HOST,
                canonicalUri = "/",
                canonicalQueryString = HUOSHAN_QUERY,
                body = bodyBytes,
                contentType = contentType,
                xDate = xDate,
            )
        val payloadSha = VolcengineOpenApiSign.requestPayloadSha256Hex(bodyBytes)
        return try {
            val resp =
                httpClient.post("https://$HUOSHAN_HOST/?$HUOSHAN_QUERY") {
                    headers {
                        append(HttpHeaders.Host, HUOSHAN_HOST)
                        append("X-Date", xDate)
                        append("X-Content-Sha256", payloadSha)
                        append(HttpHeaders.ContentType, contentType)
                        append(HttpHeaders.Authorization, auth)
                    }
                    setBody(bodyBytes)
                }
            val body = resp.body<HuoshanTranslateResponse>()
            if (body.responseMetadata?.hasError() == true) {
                TranslationOutcome.Err(
                    TranslationFailure.VendorRejected(
                        name,
                        "ResponseMetadata.Error=${body.responseMetadata.error}",
                    ),
                )
            } else {
                val first = body.translationList.firstOrNull()
                if (first == null) {
                    TranslationOutcome.Err(
                        TranslationFailure.VendorRejected(name, "empty TranslationList"),
                    )
                } else {
                    TranslationOutcome.Ok(
                        TranslationSuccess(
                            translatedText = first.translation,
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
        const val HUOSHAN_HOST = "open.volcengineapi.com"
        const val HUOSHAN_REGION = "cn-north-1"
        const val HUOSHAN_SERVICE = "translate"
        const val HUOSHAN_QUERY = "Action=TranslateText&Version=2020-06-01"

        val json =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = false
            }
    }
}

@Serializable
private data class HuoshanTranslateBody(
    @SerialName("SourceLanguage") val sourceLanguage: String? = null,
    @SerialName("TargetLanguage") val targetLanguage: String,
    @SerialName("TextList") val textList: List<String>,
)

@Serializable
private data class HuoshanTranslateResponse(
    @SerialName("TranslationList") val translationList: List<HuoshanTranslationItem> = emptyList(),
    @SerialName("ResponseMetadata") val responseMetadata: HuoshanResponseMetadata? = null,
)

@Serializable
private data class HuoshanTranslationItem(
    @SerialName("Translation") val translation: String = "",
)

@Serializable
private data class HuoshanResponseMetadata(
    @SerialName("Error") val error: JsonElement? = null,
) {
    fun hasError(): Boolean =
        when (error) {
            null, JsonNull -> false
            else -> true
        }
}

/** Volcengine `X-Date`：UTC `yyyyMMdd'T'HHmmss'Z'`（与 OpenAPI 示例一致）。 */
private fun volcengineXDateUtc(epochMillis: Long): String {
    require(epochMillis >= 0L) { "epochMillis must be non-negative" }
    val secondsTotal = epochMillis / 1000L
    val dayNumber = secondsTotal / 86400L
    val sod = (secondsTotal % 86400L).toInt()
    val hour = sod / 3600
    val minute = (sod % 3600) / 60
    val second = sod % 60
    val (year, month, day) = utcYmdFromDaysSince1970(dayNumber)
    fun p2(n: Int) = n.toString().padStart(2, '0')
    return "${year}${p2(month)}${p2(day)}T${p2(hour)}${p2(minute)}${p2(second)}Z"
}

private fun utcYmdFromDaysSince1970(dayNumber: Long): Triple<Int, Int, Int> {
    var d = dayNumber
    var y = 1970
    while (d >= daysInYear(y)) {
        d -= daysInYear(y)
        y++
    }
    var m = 1
    while (d >= daysInMonth(y, m)) {
        d -= daysInMonth(y, m)
        m++
    }
    return Triple(y, m, d.toInt() + 1)
}

private fun isLeapYear(y: Int): Boolean = y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)

private fun daysInYear(y: Int): Long = if (isLeapYear(y)) 366L else 365L

private fun daysInMonth(y: Int, m: Int): Long =
    when (m) {
        2 -> if (isLeapYear(y)) 29L else 28L
        4, 6, 9, 11 -> 30L
        else -> 31L
    }
