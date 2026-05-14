package `fun`.abbas.android_res_translator.core.translation.vendors

import `fun`.abbas.android_res_translator.core.currentEpochMillis
import `fun`.abbas.android_res_translator.core.ports.SecretsProvider
import `fun`.abbas.android_res_translator.core.translation.TranslationFailure
import `fun`.abbas.android_res_translator.core.translation.TranslationOutcome
import `fun`.abbas.android_res_translator.core.translation.TranslationRequest
import `fun`.abbas.android_res_translator.core.translation.TranslationSuccess
import `fun`.abbas.android_res_translator.core.translation.TranslationVendor
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.kotlincrypto.hash.sha2.SHA256

/**
 * 有道翻译 API v3（与旧工程 `YoudaoTranslation` 对齐：`openapi.youdao.com` + SHA-256 签名）。
 * 不包含旧版中的 `Thread.sleep` 限流；若需限流应在编排层统一处理。
 */
class YoudaoVendor(
    private val httpClient: HttpClient,
    private val secrets: SecretsProvider,
) : TranslationVendor {

    override val name: String = "youdao"

    override fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean =
        isoOrAndroidCode.isNotBlank()

    override suspend fun translate(request: TranslationRequest): TranslationOutcome {
        val appKey = secrets["youdao.appId"]
            ?: return TranslationOutcome.Err(
                TranslationFailure.VendorRejected(name, "missing youdao.appId"),
            )
        val secret = secrets["youdao.secretKey"]
            ?: return TranslationOutcome.Err(
                TranslationFailure.VendorRejected(name, "missing youdao.secretKey"),
            )
        val salt = randomUuidV4String()
        val curtime = (currentEpochMillis() / 1000L).toString()
        val q = request.sourceText
        val ql = q.length
        val signInput =
            if (ql > 20) {
                appKey + q.substring(0, 10) + ql + q.substring(ql - 10, ql) + salt + curtime + secret
            } else {
                appKey + q + salt + curtime + secret
            }
        val sign = sha256Hex(signInput.encodeToByteArray())
        return try {
            val raw =
                httpClient.submitForm(
                    url = YOUDAO_API_URL,
                    formParameters =
                        Parameters.build {
                            append("q", q)
                            append("from", request.sourceLanguage)
                            append("to", request.targetLanguage)
                            append("appKey", appKey)
                            append("salt", salt)
                            append("sign", sign)
                            append("signType", "v3")
                            append("curtime", curtime)
                        },
                ).bodyAsText()
            val parsed =
                runCatching { json.decodeFromString<YoudaoTranslateResponse>(raw) }.getOrElse {
                    return TranslationOutcome.Err(
                        TranslationFailure.VendorRejected(name, "invalid json: $raw"),
                    )
                }
            if (parsed.errorCode != "0" || parsed.translation.isEmpty()) {
                TranslationOutcome.Err(
                    TranslationFailure.VendorRejected(
                        name,
                        "errorCode=${parsed.errorCode} body=$raw",
                    ),
                )
            } else {
                TranslationOutcome.Ok(
                    TranslationSuccess(
                        translatedText = parsed.translation.first(),
                        resolvedSourceLanguage = request.sourceLanguage,
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
        const val YOUDAO_API_URL = "https://openapi.youdao.com/api"

        val json = Json { ignoreUnknownKeys = true; isLenient = true }

        fun sha256Hex(data: ByteArray): String {
            val digest = SHA256()
            digest.update(data)
            return digest.digest().toHexLower()
        }

        fun ByteArray.toHexLower(): String = buildString(size * 2) {
            for (b in this@toHexLower) {
                val v = b.toInt() and 0xFF
                if (v < 16) append('0')
                append(v.toString(16))
            }
        }

        /** RFC 4122 版本 4 UUID 字符串（小写十六进制）。 */
        fun randomUuidV4String(): String {
            val r = kotlin.random.Random.Default
            fun nyb(): Char = "0123456789abcdef"[r.nextInt(16)]
            return buildString(36) {
                repeat(8) { append(nyb()) }
                append('-')
                repeat(4) { append(nyb()) }
                append('-')
                append('4')
                repeat(3) { append(nyb()) }
                append('-')
                append("89ab"[r.nextInt(4)])
                repeat(3) { append(nyb()) }
                append('-')
                repeat(12) { append(nyb()) }
            }
        }
    }
}

@Serializable
private data class YoudaoTranslateResponse(
    @SerialName("errorCode")
    val errorCode: String = "",
    @SerialName("translation")
    val translation: List<String> = emptyList(),
)
