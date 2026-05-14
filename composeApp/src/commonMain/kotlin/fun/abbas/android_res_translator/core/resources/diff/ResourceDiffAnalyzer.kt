package `fun`.abbas.android_res_translator.core.resources.diff

import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile

/**
 * 多语言 `strings.xml` 内存模型按 key 对齐的差异结果（spec G3）。
 */
sealed class DiffRow {
    abstract val key: String

    abstract val targetCountry: String

    data class MissingInTarget(
        override val key: String,
        override val targetCountry: String,
        val isStringArray: Boolean = false,
    ) : DiffRow()

    data class ExtraInTarget(
        override val key: String,
        override val targetCountry: String,
        val isStringArray: Boolean = false,
    ) : DiffRow()

    data class ValueMismatch(
        override val key: String,
        override val targetCountry: String,
        val baselineText: String,
        val targetText: String,
        val isStringArray: Boolean = false,
    ) : DiffRow()
}

object ResourceDiffAnalyzer {

    fun analyze(
        baseline: StringResourceFile,
        targets: Map<String, StringResourceFile>,
    ): Map<String, List<DiffRow>> = targets.mapValues { (country, file) -> diffOne(baseline, country, file) }

    fun diffOne(
        baseline: StringResourceFile,
        targetCountry: String,
        target: StringResourceFile,
    ): List<DiffRow> {
        val rows = mutableListOf<DiffRow>()
        for ((k, be) in baseline.strings) {
            val te = target.strings[k]
            if (te == null) {
                rows.add(DiffRow.MissingInTarget(k, targetCountry, isStringArray = false))
            } else if (be.value != te.value) {
                rows.add(
                    DiffRow.ValueMismatch(
                        key = k,
                        targetCountry = targetCountry,
                        baselineText = be.value,
                        targetText = te.value,
                        isStringArray = false,
                    ),
                )
            }
        }
        for (k in target.strings.keys - baseline.strings.keys) {
            rows.add(DiffRow.ExtraInTarget(k, targetCountry, isStringArray = false))
        }

        for ((k, ba) in baseline.stringArrays) {
            val ta = target.stringArrays[k]
            if (ta == null) {
                rows.add(DiffRow.MissingInTarget(k, targetCountry, isStringArray = true))
            } else if (ba.items != ta.items) {
                rows.add(
                    DiffRow.ValueMismatch(
                        key = k,
                        targetCountry = targetCountry,
                        baselineText = encodeItems(ba.items),
                        targetText = encodeItems(ta.items),
                        isStringArray = true,
                    ),
                )
            }
        }
        for (k in target.stringArrays.keys - baseline.stringArrays.keys) {
            rows.add(DiffRow.ExtraInTarget(k, targetCountry, isStringArray = true))
        }
        return rows
    }

    private fun encodeItems(items: List<String>): String = items.joinToString("\u241e") // RECORD SEPARATOR
}
