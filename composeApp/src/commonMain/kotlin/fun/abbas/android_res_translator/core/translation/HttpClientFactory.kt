package `fun`.abbas.android_res_translator.core.translation

import io.ktor.client.HttpClient

/** 各平台安装 Ktor Engine + ContentNegotiation(Json)。 */
expect fun createJsonHttpClient(): HttpClient
