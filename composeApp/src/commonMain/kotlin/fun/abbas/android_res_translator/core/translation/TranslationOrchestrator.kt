package `fun`.abbas.android_res_translator.core.translation

/**
 * 多厂商顺序与旧工程 `TranslationManager` 构造一致：
 * Huoshan → Lingvanex → Baidu → Youdao → Tencent（由传入的 [vendors] 列表顺序保证）。
 */
class TranslationOrchestrator(
    private val vendors: List<TranslationVendor>,
) {
    suspend fun translate(
        request: TranslationRequest,
        preferredVendorName: String? = null,
    ): TranslationOutcome {
        var lastFailure: TranslationFailure? = null
        for (v in orderedVendors(preferredVendorName)) {
            if (!v.supportsLanguagePair(request.sourceLanguage, request.targetLanguage)) continue
            when (val o = v.translate(request)) {
                is TranslationOutcome.Ok -> return o
                is TranslationOutcome.Err -> lastFailure = o.failure
            }
        }
        return TranslationOutcome.Err(
            lastFailure ?: TranslationFailure.NoVendorForTarget(request.targetLanguage),
        )
    }

    private fun orderedVendors(preferredVendorName: String?): List<TranslationVendor> {
        val preferred = preferredVendorName?.trim()?.takeIf { it.isNotEmpty() } ?: return vendors
        val hit = vendors.firstOrNull { it.name == preferred } ?: return vendors
        return listOf(hit) + vendors.filter { it.name != preferred }
    }
}
