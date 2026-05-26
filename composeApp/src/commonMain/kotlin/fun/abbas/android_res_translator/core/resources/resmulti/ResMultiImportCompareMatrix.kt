package `fun`.abbas.android_res_translator.core.resources.resmulti

import `fun`.abbas.android_res_translator.core.resources.compare.CompareMatrixBuilder
import `fun`.abbas.android_res_translator.core.resources.compare.FlatRowKind
import `fun`.abbas.android_res_translator.core.resources.export.StringsMatrix
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiLanguageEntry

data class ResMultiImportColumn(
    val headerLabel: String,
    val language: ResMultiLanguageEntry,
)

data class ResMultiImportCompareCell(
    val importValue: String,
    val workspaceValue: String,
    val hasDifference: Boolean,
)

data class ResMultiImportCompareRow(
    val key: String,
    val kind: FlatRowKind,
    val cells: List<ResMultiImportCompareCell>,
    val hasDifference: Boolean,
)

data class ResMultiImportCompareMatrix(
    val columns: List<ResMultiImportColumn>,
    val skippedHeaders: List<String>,
    val rows: List<ResMultiImportCompareRow>,
) {
    val diffCellCount: Int
        get() = rows.sumOf { row -> row.cells.count { it.hasDifference } }
}

object ResMultiImportCompareBuilder {
    fun build(
        imported: StringsMatrix,
        workspaceFlats: List<ResMultiLanguageFlat>,
        languages: List<ResMultiLanguageEntry>,
    ): ResMultiImportCompareMatrix {
        val keyHeader = imported.columnHeaders.firstOrNull()?.trim().orEmpty()
        require(keyHeader.equals("key", ignoreCase = true)) {
            "First column header must be \"key\", got \"$keyHeader\""
        }
        val workspaceByLanguage = workspaceFlats.associate { it.entry to it }
        val importByLang = linkedMapOf<ResMultiLanguageEntry, Map<String, String>>()
        val skipped = mutableListOf<String>()
        imported.columnHeaders.drop(1).forEachIndexed { index, header ->
            val lang = matchLanguageHeader(header, languages)
            if (lang == null) {
                skipped += header
                return@forEachIndexed
            }
            val colIndex = index + 1
            val values = linkedMapOf<String, String>()
            for (row in imported.rows) {
                values[row.key] = row.valuesByColumn.getOrNull(colIndex).orEmpty()
            }
            importByLang[lang] = values
        }
        val columns =
            importByLang.keys.map { lang ->
                ResMultiImportColumn(
                    headerLabel = ResMultiStringsMatrixBuilder.columnLabel(lang, languages),
                    language = lang,
                )
            }
        val allKeys =
            (importByLang.values.flatMap { it.keys } + workspaceFlats.flatMap { it.valuesByKey.keys })
                .toSortedSet()
        val rows =
            allKeys.map { key ->
                val cells =
                    columns.map { column ->
                        val importValue = importByLang[column.language]?.get(key).orEmpty()
                        val workspaceValue =
                            workspaceByLanguage[column.language]?.valuesByKey?.get(key).orEmpty()
                        val hasDifference =
                            CompareMatrixBuilder.normalizeForCompare(importValue) !=
                                CompareMatrixBuilder.normalizeForCompare(workspaceValue)
                        ResMultiImportCompareCell(
                            importValue = importValue,
                            workspaceValue = workspaceValue,
                            hasDifference = hasDifference,
                        )
                    }
                ResMultiImportCompareRow(
                    key = key,
                    kind = resolveKind(key, columns, workspaceByLanguage),
                    cells = cells,
                    hasDifference = cells.any { it.hasDifference },
                )
            }
        return ResMultiImportCompareMatrix(
            columns = columns,
            skippedHeaders = skipped,
            rows = rows,
        )
    }

    fun matchLanguageHeader(
        header: String,
        languages: List<ResMultiLanguageEntry>,
    ): ResMultiLanguageEntry? {
        val trimmed = header.trim()
        if (trimmed.isEmpty()) return null
        languages.find { it.langCode.equals(trimmed, ignoreCase = true) }?.let { return it }
        val parenStart = trimmed.indexOf('(')
        if (parenStart > 0 && trimmed.endsWith(')')) {
            val langCode = trimmed.substring(0, parenStart).trim()
            val folderName = trimmed.substring(parenStart + 1, trimmed.length - 1).trim()
            return languages.find { it.langCode == langCode && it.folderName == folderName }
        }
        return languages.find { it.folderName.equals(trimmed, ignoreCase = true) }
    }

    private fun resolveKind(
        key: String,
        columns: List<ResMultiImportColumn>,
        workspaceByLanguage: Map<ResMultiLanguageEntry, ResMultiLanguageFlat>,
    ): FlatRowKind {
        for (column in columns) {
            workspaceByLanguage[column.language]?.kindsByKey?.get(key)?.let { return it }
        }
        if (key.contains('#')) return FlatRowKind.PLURAL_ITEM
        if (key.any { it.isDigit() }) return FlatRowKind.STRING_ARRAY_ITEM
        return FlatRowKind.STRING
    }
}
