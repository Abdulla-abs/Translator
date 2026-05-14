package `fun`.abbas.android_res_translator.core.translation.vendors

import `fun`.abbas.android_res_translator.core.ports.SecretsProvider
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
 * 目标语言码使用 [ANDROID_TO_LINGVANEX_TARGET]；未配置的目标语言视为本厂商不支持。
 */
class LingvanexVendor(
    private val httpClient: HttpClient,
    private val secrets: SecretsProvider,
) : TranslationVendor {

    override val name: String = "lingvanex"

    override fun supportsTargetLanguage(isoOrAndroidCode: String): Boolean =
        ANDROID_TO_LINGVANEX_TARGET.containsKey(isoOrAndroidCode)

    override suspend fun translate(request: TranslationRequest): TranslationOutcome {
        val token = secrets["lingvanex.token"]
            ?: return TranslationOutcome.Err(
                TranslationFailure.VendorRejected(name, "missing lingvanex.token"),
            )
        val mappedTo =
            ANDROID_TO_LINGVANEX_TARGET[request.targetLanguage]
                ?: return TranslationOutcome.Err(
                    TranslationFailure.VendorRejected(
                        name,
                        "unsupported target language: ${request.targetLanguage}",
                    ),
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
        const val LINGVANEX_TRANSLATE_URL = "https://api-b2b.backenster.com/b1/api/v3/translate"

        /** 与旧工程 `LingvanexTranslation.initSupportComparisonTable` 一致（key = 安卓侧语言/地区码）。 */
        val ANDROID_TO_LINGVANEX_TARGET: Map<String, String> =
            mapOf(
                "en" to "en_US",
                "cs" to "cs_CZ",
                "de" to "de_DE",
                "el" to "el_GR",
                "fi" to "fi_FI",
                "fr" to "fr_CA",
                "hr" to "hr_HR",
                "in" to "id_ID",
                "it" to "it_IT",
                "ja" to "ja_JP",
                "ko" to "ko_KR",
                "nl" to "nl_NL",
                "pt" to "pt_PT",
                "ro-rRO" to "ro_RO",
                "ru" to "ru_RU",
                "sl" to "sl_SI",
                "sr" to "sr-Cyrl_RS",
                "zh" to "zh-Hans_CN",
                "zh-rHK" to "zh-Hant_TW",
                "zh-rMO" to "zh-Hant_TW",
                "zh-rTW" to "zh-Hant_TW",
                "es" to "es_ES",
            )
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
