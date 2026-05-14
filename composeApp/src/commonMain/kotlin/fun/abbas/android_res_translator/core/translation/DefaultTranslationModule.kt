package `fun`.abbas.android_res_translator.core.translation

import `fun`.abbas.android_res_translator.core.ports.SecretsProvider
import `fun`.abbas.android_res_translator.core.translation.vendors.BaiduVendor
import `fun`.abbas.android_res_translator.core.translation.vendors.HuoshanVendor
import `fun`.abbas.android_res_translator.core.translation.vendors.LingvanexVendor
import `fun`.abbas.android_res_translator.core.translation.vendors.TencentVendor
import `fun`.abbas.android_res_translator.core.translation.vendors.YoudaoVendor
import io.ktor.client.HttpClient

/**
 * 默认翻译链装配（D3）：与旧工程 `TranslationManager` 顺序一致 — **Huoshan → Lingvanex → Baidu → Youdao → Tencent**。
 *
 * 使用 [createJsonHttpClient] 创建共享 [HttpClient]；调用方负责在进程退出前关闭 client 的，可后续扩展为接受外部 client。
 */
fun buildDefaultOrchestrator(secrets: SecretsProvider): TranslationOrchestrator =
    TranslationOrchestrator(defaultTranslationVendors(createJsonHttpClient(), secrets))

internal fun defaultTranslationVendors(
    httpClient: HttpClient,
    secrets: SecretsProvider,
): List<TranslationVendor> =
    listOf(
        HuoshanVendor(httpClient, secrets),
        LingvanexVendor(httpClient, secrets),
        BaiduVendor(httpClient, secrets),
        YoudaoVendor(httpClient, secrets),
        TencentVendor(httpClient, secrets),
    )
