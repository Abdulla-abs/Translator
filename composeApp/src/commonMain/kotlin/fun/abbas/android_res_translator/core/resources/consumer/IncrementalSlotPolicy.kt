package `fun`.abbas.android_res_translator.core.resources.consumer

import `fun`.abbas.android_res_translator.core.resources.model.StringArrayEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringEntry

/**
 * 增量槽位判定：与 [FilledTranslationConsumer]、[IncrementalTranslationPlanner] 共用。
 * - string：目标缺 key 或 value 为空 → 需翻译
 * - string-array：目标缺整段，或任一 item 缺失/为空 → 需处理该 array
 */
object IncrementalSlotPolicy {
    fun needsTranslateString(target: StringEntry?): Boolean =
        target == null || target.value.isBlank()

    fun needsTranslateStringArray(
        source: StringArrayEntry,
        target: StringArrayEntry?,
    ): Boolean =
        target == null ||
            source.items.indices.any { i ->
                needsTranslateStringArrayItem(target.items.getOrNull(i))
            }

    fun needsTranslateStringArrayItem(targetItem: String?): Boolean =
        targetItem == null || targetItem.isBlank()
}
