package `fun`.abbas.android_res_translator.core.resources.consumer

import `fun`.abbas.android_res_translator.core.resources.model.StringArrayEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringEntry

/**
 * 增量翻译：与旧工程 `FilledTranslationConsumer` 一致——仅在目标缺少对应 `string` / 整段 `string-array`，
 * 或 `name` 属于 [mustTranslateNames]（强制重译该 string）时翻译。
 */
class FilledTranslationConsumer(
    private val mustTranslateNames: Set<String> = emptySet(),
    forceTranslation: Boolean = false,
) : AbsTranslationConsumer(forceTranslation) {

    constructor(mustTranslateNames: List<String>, forceTranslation: Boolean = false) :
        this(mustTranslateNames.toSet(), forceTranslation)

    override fun shouldTranslateString(
        key: String,
        source: StringEntry,
        target: StringEntry?,
    ): Boolean = target == null || key in mustTranslateNames

    override fun shouldTranslateStringArray(
        source: StringArrayEntry,
        target: StringArrayEntry?,
    ): Boolean = target == null

    override fun shouldTranslateStringArrayItem(
        sourceItem: String,
        targetItem: String?,
    ): Boolean = targetItem == null
}
