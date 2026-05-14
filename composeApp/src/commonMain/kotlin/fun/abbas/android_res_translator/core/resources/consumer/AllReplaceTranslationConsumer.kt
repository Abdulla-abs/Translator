package `fun`.abbas.android_res_translator.core.resources.consumer

import `fun`.abbas.android_res_translator.core.resources.model.StringArrayEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringEntry

/**
 * 全量翻译：与旧工程 `AllReplaceTranslationConsumer` 一致——对存在的 `string` / `string-array` 槽位一律按源文重译并覆盖。
 */
class AllReplaceTranslationConsumer(
    forceTranslation: Boolean = false,
) : AbsTranslationConsumer(forceTranslation) {

    override fun shouldTranslateString(
        key: String,
        source: StringEntry,
        target: StringEntry?,
    ): Boolean = true

    override fun shouldTranslateStringArray(
        source: StringArrayEntry,
        target: StringArrayEntry?,
    ): Boolean = true

    override fun shouldTranslateStringArrayItem(
        sourceItem: String,
        targetItem: String?,
    ): Boolean = true
}
