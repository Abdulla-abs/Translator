package `fun`.abbas.android_res_translator.core.resources.model

/** `<plurals name="…">` 下按 [quantity] 分组的文案。 */
data class PluralEntry(
    val name: String,
    val items: Map<String, String>,
    val translatable: Boolean = true,
)
