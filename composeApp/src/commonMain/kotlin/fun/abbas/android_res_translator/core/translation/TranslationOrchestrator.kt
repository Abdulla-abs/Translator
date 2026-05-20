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
        val ordered = orderedVendors(preferredVendorName)
        TranslationDebugLog.log(
            "Orchestrator",
            "translate from=${request.sourceLanguage} to=${request.targetLanguage} " +
                "preferred=${preferredVendorName ?: "(none)"} chain=${ordered.map { it.name }}",
        )
        val preferredKey = preferredVendorName?.trim()?.takeIf { it.isNotEmpty() }
        var lastFailure: TranslationFailure? = null
        for (v in ordered) {
            val supports = v.supportsLanguagePair(request.sourceLanguage, request.targetLanguage)
            if (!supports) {
                TranslationDebugLog.log(
                    "Orchestrator",
                    "skip ${v.name}: supportsLanguagePair=false for ${request.sourceLanguage}->${request.targetLanguage}",
                )
                continue
            }
            TranslationDebugLog.log("Orchestrator", "try ${v.name} ...")
            when (val o = v.translate(request)) {
                is TranslationOutcome.Ok -> {
                    TranslationDebugLog.log("Orchestrator", "ok ${v.name}")
                    return o
                }
                is TranslationOutcome.Err -> {
                    lastFailure = o.failure
                    TranslationDebugLog.log(
                        "Orchestrator",
                        "err ${v.name}: ${o.failure}",
                    )
                    if (preferredKey != null && v.name == preferredKey) {
                        TranslationDebugLog.log(
                            "Orchestrator",
                            "stop: preferred vendor $preferredKey failed, not trying fallbacks",
                        )
                        return TranslationOutcome.Err(o.failure)
                    }
                }
            }
        }
        TranslationDebugLog.log(
            "Orchestrator",
            "all vendors failed; lastFailure=$lastFailure",
        )
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
