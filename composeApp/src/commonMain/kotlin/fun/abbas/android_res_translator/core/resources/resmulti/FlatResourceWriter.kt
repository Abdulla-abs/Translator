package `fun`.abbas.android_res_translator.core.resources.resmulti

import `fun`.abbas.android_res_translator.core.resources.compare.FlatRowKind
import `fun`.abbas.android_res_translator.core.resources.model.PluralEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringArrayEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile

/** 按展平 key 写回 [StringResourceFile]（用于导入覆写）。 */
object FlatResourceWriter {
    fun setValue(
        file: StringResourceFile,
        key: String,
        kind: FlatRowKind,
        value: String,
    ): StringResourceFile =
        when (kind) {
            FlatRowKind.STRING -> setString(file, key, value)
            FlatRowKind.STRING_ARRAY_ITEM -> setArrayItem(file, key, value)
            FlatRowKind.PLURAL_ITEM -> setPluralItem(file, key, value)
        }

    private fun setString(
        file: StringResourceFile,
        key: String,
        value: String,
    ): StringResourceFile {
        val existing = file.strings[key]
        val entry = existing?.copy(value = value) ?: StringEntry(name = key, value = value)
        return file.copy(strings = file.strings + (key to entry))
    }

    private fun setArrayItem(
        file: StringResourceFile,
        key: String,
        value: String,
    ): StringResourceFile {
        val arrayName = resolveArrayName(key, file) ?: return file
        val index = key.removePrefix(arrayName).toIntOrNull() ?: return file
        val existing = file.stringArrays[arrayName] ?: return file
        val items = existing.items.toMutableList()
        while (items.size <= index) {
            items += ""
        }
        items[index] = value
        return file.copy(
            stringArrays = file.stringArrays + (arrayName to existing.copy(items = items)),
        )
    }

    private fun setPluralItem(
        file: StringResourceFile,
        key: String,
        value: String,
    ): StringResourceFile {
        val hash = key.indexOf('#')
        if (hash <= 0) return file
        val name = key.substring(0, hash)
        val quantity = key.substring(hash + 1)
        val existing = file.plurals[name] ?: return file
        val items = existing.items.toMutableMap()
        items[quantity] = value
        return file.copy(plurals = file.plurals + (name to existing.copy(items = items)))
    }

    internal fun resolveArrayName(
        key: String,
        file: StringResourceFile,
    ): String? =
        file.stringArrays.keys
            .filter { key.startsWith(it) && key.length > it.length }
            .filter { key.substring(it.length).all { ch -> ch.isDigit() } }
            .maxByOrNull { it.length }
}
