package `fun`.abbas.android_res_translator.core.resources.consumer

import `fun`.abbas.android_res_translator.core.resources.model.StringArrayEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import `fun`.abbas.android_res_translator.core.translation.TranslationOutcome

/**
 * 与旧工程 `AbsTranslationConsumer` 等价的合并流程（DOM 改为 [StringResourceFile]）。
 *
 * `string-array`：当需要处理时，按源 `item` 数量重建列表（与旧版「先删再建」语义一致）。
 * 旧 Java 在「目标 item 已存在」分支曾 `setText(getText())` 未写入译文；此处在子类要求翻译该槽位时写入新译文。
 */
abstract class AbsTranslationConsumer(
    protected val forceTranslation: Boolean = false,
) : TranslationConsumer {

    protected open fun needSkipString(source: StringEntry): Boolean =
        !forceTranslation && !source.translatable

    protected open fun needSkipArray(source: StringArrayEntry): Boolean =
        !forceTranslation && !source.translatable

    protected abstract fun shouldTranslateString(
        key: String,
        source: StringEntry,
        target: StringEntry?,
    ): Boolean

    protected abstract fun shouldTranslateStringArray(
        source: StringArrayEntry,
        target: StringArrayEntry?,
    ): Boolean

    protected abstract fun shouldTranslateStringArrayItem(
        sourceItem: String,
        targetItem: String?,
    ): Boolean

    override suspend fun accept(
        source: StringResourceFile,
        sourceLang: String,
        target: StringResourceFile,
        targetLang: String,
        port: TranslationSegmentPort,
    ): StringResourceFile {
        val outStrings = target.strings.toMutableMap()
        val outArrays = target.stringArrays.toMutableMap()

        for ((key, src) in source.strings) {
            if (needSkipString(src)) continue
            val tgt = outStrings[key]
            if (!shouldTranslateString(key, src, tgt)) continue
            val translated =
                translateOrKeep(
                    text = src.value,
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                    port = port,
                    fallback = tgt?.value ?: src.value,
                )
            outStrings[key] =
                StringEntry(
                    name = key,
                    value = translated,
                    translatable = tgt?.translatable ?: src.translatable,
                )
        }

        for ((key, srcArr) in source.stringArrays) {
            if (needSkipArray(srcArr)) continue
            val tgtArr = outArrays[key]
            if (!shouldTranslateStringArray(srcArr, tgtArr)) continue

            val tgtItems = tgtArr?.items.orEmpty()
            val newItems =
                List(srcArr.items.size) { i ->
                    val sItem = srcArr.items[i]
                    val tItem = tgtItems.getOrNull(i)
                    if (!shouldTranslateStringArrayItem(sItem, tItem)) {
                        tItem ?: ""
                    } else {
                        translateOrKeep(
                            text = sItem,
                            sourceLang = sourceLang,
                            targetLang = targetLang,
                            port = port,
                            fallback = tItem ?: sItem,
                        )
                    }
                }
            outArrays[key] =
                StringArrayEntry(
                    name = key,
                    items = newItems,
                    translatable = tgtArr?.translatable ?: srcArr.translatable,
                )
        }

        return StringResourceFile(strings = outStrings, stringArrays = outArrays)
    }

    private suspend fun translateOrKeep(
        text: String,
        sourceLang: String,
        targetLang: String,
        port: TranslationSegmentPort,
        fallback: String,
    ): String =
        when (val o = port.translateSegment(text, sourceLang, targetLang)) {
            is TranslationOutcome.Ok -> {
                val t = o.value.translatedText
                if (t.isEmpty()) fallback else t
            }
            else -> fallback
        }
}
