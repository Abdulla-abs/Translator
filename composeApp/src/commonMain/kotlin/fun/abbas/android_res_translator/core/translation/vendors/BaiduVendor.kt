package `fun`.abbas.android_res_translator.core.translation.vendors

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
import org.kotlincrypto.hash.md.MD5

/**
 * 百度通用翻译 VIP 接口（与旧工程 `BaiduTranslation` 对齐：表单 POST + MD5 签名）。
 */
class BaiduVendor(
    private val httpClient: HttpClient,
    private val secrets: SecretsProvider,
) : TranslationVendor {

    override val name: String = "baidu"

    override fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean =
        isoOrAndroidCode.isNotBlank()

    override suspend fun translate(request: TranslationRequest): TranslationOutcome {
        val appId = secrets["baidu.appId"]
            ?: return TranslationOutcome.Err(
                TranslationFailure.VendorRejected(name, "missing baidu.appId"),
            )
        val secretKey = secrets["baidu.secretKey"]
            ?: return TranslationOutcome.Err(
                TranslationFailure.VendorRejected(name, "missing baidu.secretKey"),
            )
        val salt = kotlin.random.Random.nextInt(20)
        val signPlain = appId + request.sourceText + salt + secretKey
        val sign = md5LowerHex(signPlain)
        return try {
            val raw = httpClient.submitForm(
                url = BAIDU_TRANSLATE_URL,
                formParameters = Parameters.build {
                    append("q", request.sourceText)
                    append("from", request.sourceLanguage)
                    append("to", request.targetLanguage)
                    append("appid", appId)
                    append("salt", salt.toString())
                    append("sign", sign)
                },
            ).bodyAsText()
            val parsed = runCatching { json.decodeFromString<BaiduTranslateResponse>(raw) }.getOrElse {
                return TranslationOutcome.Err(
                    TranslationFailure.VendorRejected(name, "invalid json: $raw"),
                )
            }
            val first = parsed.transResult.firstOrNull()
            if (first == null) {
                TranslationOutcome.Err(
                    TranslationFailure.VendorRejected(name, "empty trans_result: $raw"),
                )
            } else {
                TranslationOutcome.Ok(
                    TranslationSuccess(
                        translatedText = first.dst,
                        resolvedSourceLanguage = parsed.from.ifEmpty { request.sourceLanguage },
                        resolvedTargetLanguage = parsed.to.ifEmpty { request.targetLanguage },
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
        const val BAIDU_TRANSLATE_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate"

        val json = Json { ignoreUnknownKeys = true; isLenient = true }

        fun md5LowerHex(plain: String): String {
            val digest = MD5()
            digest.update(plain.encodeToByteArray())
            val out = digest.digest()
            return out.toHexLower()
        }

        fun ByteArray.toHexLower(): String = buildString(size * 2) {
            for (b in this@toHexLower) {
                val v = b.toInt() and 0xFF
                if (v < 16) append('0')
                append(v.toString(16))
            }
        }
    }
}

@Serializable
private data class BaiduTranslateResponse(
    val from: String = "",
    val to: String = "",
    @SerialName("trans_result")
    val transResult: List<BaiduTransItem> = emptyList(),
)

@Serializable
private data class BaiduTransItem(
    val src: String = "",
    val dst: String = "",
)
