package `fun`.abbas.android_res_translator.core.resources.consumer

import `fun`.abbas.android_res_translator.core.resources.model.StringArrayEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringEntry

/**
 * 增量翻译：目标缺 key / 空 value / 空 array item 时翻译；非空保留。
 * [mustTranslateNames] 可强制重译指定 string。
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
    ): Boolean = key in mustTranslateNames || IncrementalSlotPolicy.needsTranslateString(target)

    override fun shouldTranslateStringArray(
        source: StringArrayEntry,
        target: StringArrayEntry?,
    ): Boolean = IncrementalSlotPolicy.needsTranslateStringArray(source, target)

    override fun shouldTranslateStringArrayItem(
        sourceItem: String,
        targetItem: String?,
    ): Boolean = IncrementalSlotPolicy.needsTranslateStringArrayItem(targetItem)
}
