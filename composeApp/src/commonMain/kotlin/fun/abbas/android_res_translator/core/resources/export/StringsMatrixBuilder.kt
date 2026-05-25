package `fun`.abbas.android_res_translator.core.resources.export

import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.EntryStatus
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.XmlEntryUi

/**
 * 将单文件编辑器状态展平为 Excel 矩阵（key + 源语言 + 目标语言）。
 * `string` 行来自 UI 条目；`string-array` 按 legacy `name + index` 展平，目标列优先取目标 baseline。
 */
object StringsMatrixBuilder {
    fun buildForFileEditor(
        sourceFile: StringResourceFile,
        targetBaseline: StringResourceFile?,
        sourceLang: String,
        targetLang: String,
        entries: List<XmlEntryUi>,
    ): StringsMatrix {
        val entryByKey = entries.associateBy { it.key }
        val sourceLangLabel = sourceLang.trim().ifEmpty { "source" }
        val targetLangLabel = targetLang.trim().ifEmpty { "target" }
        val headers = listOf("key", sourceLangLabel, targetLangLabel)

        val stringRows =
            entries
                .sortedBy { it.key }
                .map { entry ->
                    StringsMatrixRow(
                        key = entry.key,
                        valuesByColumn =
                            listOf(
                                entry.key,
                                entry.sourceText,
                                resolveTargetText(entry),
                            ),
                    )
                }

        val arrayRows = mutableListOf<StringsMatrixRow>()
        for ((_, arr) in sourceFile.stringArrays.entries.sortedBy { it.key }) {
            if (!arr.translatable) continue
            val targetArr = targetBaseline?.stringArrays?.get(arr.name)
            arr.items.forEachIndexed { index, sourceItem ->
                val flatKey = "${arr.name}$index"
                if (flatKey in entryByKey) return@forEachIndexed
                val targetItem = targetArr?.items?.getOrNull(index).orEmpty()
                arrayRows +=
                    StringsMatrixRow(
                        key = flatKey,
                        valuesByColumn =
                            listOf(
                                flatKey,
                                sourceItem,
                                targetItem.ifBlank { sourceItem },
                            ),
                    )
            }
        }

        return StringsMatrix(
            columnHeaders = headers,
            rows = stringRows + arrayRows.sortedBy { it.key },
        )
    }

    private fun resolveTargetText(entry: XmlEntryUi): String =
        when {
            !entry.translatable -> entry.sourceText
            entry.status is EntryStatus.Completed && entry.targetText?.isNotBlank() == true ->
                entry.targetText.orEmpty()
            entry.status is EntryStatus.Skipped && entry.targetText?.isNotBlank() == true ->
                entry.targetText.orEmpty()
            else -> entry.sourceText
        }
}
