package `fun`.abbas.android_res_translator.ui.screens.main

internal fun formatLanguageLabel(code: String): String {
    val trimmed = code.trim()
    if (trimmed.isBlank()) return "(EN)英语"
    val name =
        LanguageDisplayNames.chineseName(trimmed)
            ?: trimmed.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val suffix = trimmed.uppercase().take(8)
    return "($suffix)$name"
}
