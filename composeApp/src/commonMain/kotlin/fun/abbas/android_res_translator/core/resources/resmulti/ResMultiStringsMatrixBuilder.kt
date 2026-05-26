package `fun`.abbas.android_res_translator.core.resources.resmulti

import `fun`.abbas.android_res_translator.core.resources.compare.FlatRowKind
import `fun`.abbas.android_res_translator.core.resources.compare.ResourceFlattener
import `fun`.abbas.android_res_translator.core.resources.export.StringsMatrix
import `fun`.abbas.android_res_translator.core.resources.export.StringsMatrixRow
import `fun`.abbas.android_res_translator.core.resources.xml.StringsXmlCodec
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiLanguageEntry

/** 某一语言目录展平后的 key → value。 */
data class ResMultiLanguageFlat(
    val entry: ResMultiLanguageEntry,
    val valuesByKey: Map<String, String>,
    val kindsByKey: Map<String, FlatRowKind> = emptyMap(),
)

sealed class ResMultiMatrixBuildError {
    abstract val message: String

    data class NoLanguages(
        override val message: String = "No language folders to export",
    ) : ResMultiMatrixBuildError()

    data class ParseFailed(
        val langCode: String,
        val stringsPath: String,
        val cause: String,
    ) : ResMultiMatrixBuildError() {
        override val message: String =
            "Failed to parse $stringsPath ($langCode): $cause"
    }
}

/**
 * 将多文件项目 workspace 内各 `strings.xml` 合并为导出矩阵（含 string / array / plurals 展平行）。
 */
object ResMultiStringsMatrixBuilder {
    fun parseAndFlatten(
        entry: ResMultiLanguageEntry,
        xml: String,
    ): Result<ResMultiLanguageFlat> =
        try {
            val file = StringsXmlCodec.parse(xml)
            val flattened = ResourceFlattener.flatten(file)
            Result.success(
                ResMultiLanguageFlat(
                    entry = entry,
                    valuesByKey = flattened.mapValues { (_, row) -> row.value },
                    kindsByKey = flattened.mapValues { (_, row) -> row.kind },
                ),
            )
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(
                    ResMultiMatrixBuildError.ParseFailed(
                        langCode = entry.langCode,
                        stringsPath = entry.stringsRelativePath,
                        cause = e.message ?: e.toString(),
                    ).message,
                    e,
                ),
            )
        }

    fun buildFull(languages: List<ResMultiLanguageFlat>): Result<StringsMatrix> {
        if (languages.isEmpty()) {
            return Result.failure(IllegalStateException(ResMultiMatrixBuildError.NoLanguages().message))
        }
        val headers = listOf("key") + languages.map { columnLabel(it.entry, languages.map { l -> l.entry }) }
        val allKeys =
            languages
                .flatMap { it.valuesByKey.keys }
                .toSortedSet()
        val rows =
            allKeys.map { key ->
                StringsMatrixRow(
                    key = key,
                    valuesByColumn =
                        listOf(key) +
                            languages.map { lang ->
                                lang.valuesByKey[key].orEmpty()
                            },
                )
            }
        return Result.success(StringsMatrix(columnHeaders = headers, rows = rows))
    }

    fun buildSingle(language: ResMultiLanguageFlat): StringsMatrix {
        val label = language.entry.langCode
        val headers = listOf("key", label)
        val rows =
            language.valuesByKey.keys.sorted().map { key ->
                val value = language.valuesByKey[key].orEmpty()
                StringsMatrixRow(
                    key = key,
                    valuesByColumn = listOf(key, value),
                )
            }
        return StringsMatrix(columnHeaders = headers, rows = rows)
    }

    internal fun columnLabel(
        entry: ResMultiLanguageEntry,
        allEntries: List<ResMultiLanguageEntry>,
    ): String {
        val duplicateLang =
            allEntries.count { it.langCode == entry.langCode } > 1
        return if (duplicateLang) "${entry.langCode}(${entry.folderName})" else entry.langCode
    }

}
