package `fun`.abbas.android_res_translator.core.resources.compare

import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile

enum class FlatRowKind {
    STRING,
    STRING_ARRAY_ITEM,
    PLURAL_ITEM,
}

data class FlatResourceRow(
    val key: String,
    val value: String,
    val kind: FlatRowKind,
)

/**
 * 将 [StringResourceFile] 展平为比对行。
 * - string → `name`
 * - string-array → `name0`, `name1`, …
 * - plurals → `name#one`, `name#other`, …
 */
object ResourceFlattener {
    fun flatten(file: StringResourceFile): Map<String, FlatResourceRow> {
        val rows = linkedMapOf<String, FlatResourceRow>()
        for ((_, entry) in file.strings.entries.sortedBy { it.key }) {
            rows[entry.name] =
                FlatResourceRow(
                    key = entry.name,
                    value = entry.value,
                    kind = FlatRowKind.STRING,
                )
        }
        for ((_, arr) in file.stringArrays.entries.sortedBy { it.key }) {
            arr.items.forEachIndexed { index, item ->
                val flatKey = "${arr.name}$index"
                rows[flatKey] =
                    FlatResourceRow(
                        key = flatKey,
                        value = item,
                        kind = FlatRowKind.STRING_ARRAY_ITEM,
                    )
            }
        }
        for ((_, plural) in file.plurals.entries.sortedBy { it.key }) {
            for ((quantity, value) in plural.items.entries.sortedBy { it.key }) {
                val flatKey = "${plural.name}#$quantity"
                rows[flatKey] =
                    FlatResourceRow(
                        key = flatKey,
                        value = value,
                        kind = FlatRowKind.PLURAL_ITEM,
                    )
            }
        }
        return rows
    }
}
