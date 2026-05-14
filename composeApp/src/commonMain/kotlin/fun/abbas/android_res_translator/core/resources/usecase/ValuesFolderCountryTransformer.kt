package `fun`.abbas.android_res_translator.core.resources.usecase

/**
 * 将 `res` 下子目录名（如 `values`、`values-en`、`values-zh-rCN`）映射为语言/地区代码，
 * 语义与旧工程 `AbsStringFolderCountryTransformer` 一致：`split("-", limit = 2)` 有第二段则取第二段，否则返回 `sourceCountry`。
 */
interface ValuesFolderCountryTransformer {
    fun folderNameToCountry(folderName: String): String

    val sourceCountry: String
}

/** 默认源语言 `en`，与旧工程 `DefaultCountryTransformer` 一致。 */
class DefaultValuesFolderCountryTransformer(
    override val sourceCountry: String = "en",
) : ValuesFolderCountryTransformer {
    override fun folderNameToCountry(folderName: String): String {
        val parts = folderName.split("-", limit = 2)
        return if (parts.size > 1) parts[1] else sourceCountry
    }
}

/** 仅识别 Android 常规 `values` / `values-xx` 目录，避免把 `drawable-hdpi` 等误当语言目录。 */
fun isValuesResourceFolderName(folderName: String): Boolean =
    folderName == "values" || folderName.startsWith("values-")
