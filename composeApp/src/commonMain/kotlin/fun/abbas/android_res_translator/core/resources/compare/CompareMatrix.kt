package `fun`.abbas.android_res_translator.core.resources.compare

import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile

data class CompareMatrix(
    val leftColumnLabel: String,
    val rightColumnLabel: String,
    val rows: List<CompareMatrixRow>,
)

data class CompareMatrixRow(
    val key: String,
    val kind: FlatRowKind,
    val leftValue: String,
    val rightValue: String,
    val hasDifference: Boolean,
)

object CompareMatrixBuilder {
    fun build(
        leftColumnLabel: String,
        leftFile: StringResourceFile,
        rightColumnLabel: String,
        rightFile: StringResourceFile,
    ): CompareMatrix {
        val leftFlat = ResourceFlattener.flatten(leftFile)
        val rightFlat = ResourceFlattener.flatten(rightFile)
        val allKeys = (leftFlat.keys + rightFlat.keys).toSortedSet()
        val rows =
            allKeys.map { key ->
                val left = leftFlat[key]?.value.orEmpty()
                val right = rightFlat[key]?.value.orEmpty()
                val kind =
                    leftFlat[key]?.kind
                        ?: rightFlat[key]?.kind
                        ?: FlatRowKind.STRING
                CompareMatrixRow(
                    key = key,
                    kind = kind,
                    leftValue = left,
                    rightValue = right,
                    hasDifference = normalizeForCompare(left) != normalizeForCompare(right),
                )
            }
        return CompareMatrix(
            leftColumnLabel = leftColumnLabel.trim().ifEmpty { "A" },
            rightColumnLabel = rightColumnLabel.trim().ifEmpty { "B" },
            rows = rows,
        )
    }

    internal fun normalizeForCompare(text: String): String =
        text.trim().replace(Regex("""\s+"""), " ")
}
