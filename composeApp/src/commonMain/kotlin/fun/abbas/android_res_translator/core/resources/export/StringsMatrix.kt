package `fun`.abbas.android_res_translator.core.resources.export

/** 导出/比对用的二维表：首列为 key，其余列为各语言文案。 */
data class StringsMatrix(
    val columnHeaders: List<String>,
    val rows: List<StringsMatrixRow>,
)

data class StringsMatrixRow(
    val key: String,
    val valuesByColumn: List<String>,
)
