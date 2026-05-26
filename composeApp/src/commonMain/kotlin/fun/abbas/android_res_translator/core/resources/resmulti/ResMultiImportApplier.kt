package `fun`.abbas.android_res_translator.core.resources.resmulti

import `fun`.abbas.android_res_translator.core.resources.compare.FlatRowKind
import `fun`.abbas.android_res_translator.core.resources.xml.StringsXmlCodec
import `fun`.abbas.android_res_translator.persistence.ResMultiProjectFileStore
import `fun`.abbas.android_res_translator.persistence.ResProjectVersionStore
import `fun`.abbas.android_res_translator.persistence.fileExists
import `fun`.abbas.android_res_translator.persistence.readTextFile
import `fun`.abbas.android_res_translator.persistence.writeTextFileAtomic
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiLanguageEntry
import `fun`.abbas.android_res_translator.util.currentEpochMillis

data class ResMultiImportApplyResult(
    val changedKeysByLanguage: Map<ResMultiLanguageEntry, Set<String>>,
    val totalChangedCells: Int,
)

object ResMultiImportApplier {
    fun applyDiffs(
        projectId: String,
        matrix: ResMultiImportCompareMatrix,
    ): Result<ResMultiImportApplyResult> {
        if (matrix.diffCellCount == 0) {
            return Result.success(ResMultiImportApplyResult(emptyMap(), 0))
        }
        val changedByLang = linkedMapOf<ResMultiLanguageEntry, MutableSet<String>>()
        var total = 0
        for ((columnIndex, column) in matrix.columns.withIndex()) {
            val path = stringsPath(projectId, column.language)
            if (!fileExists(path)) {
                return Result.failure(IllegalStateException("Missing ${column.language.stringsRelativePath}"))
            }
            val xml = readTextFile(path)
            var file = StringsXmlCodec.parse(xml)
            val changes = linkedMapOf<String, Pair<FlatRowKind, String>>()
            for (row in matrix.rows) {
                val cell = row.cells.getOrNull(columnIndex) ?: continue
                if (!cell.hasDifference) continue
                changes[row.key] = row.kind to cell.importValue
            }
            if (changes.isEmpty()) continue
            for ((key, kindValue) in changes) {
                val (kind, value) = kindValue
                file = FlatResourceWriter.setValue(file, key, kind, value)
            }
            writeTextFileAtomic(path, StringsXmlCodec.serialize(file))
            changedByLang[column.language] = changes.keys.toMutableSet()
            total += changes.size
        }
        markProjectDirty(projectId)
        return Result.success(
            ResMultiImportApplyResult(
                changedKeysByLanguage = changedByLang.mapValues { it.value.toSet() },
                totalChangedCells = total,
            ),
        )
    }

    fun markProjectDirty(projectId: String) {
        val meta =
            ResMultiProjectFileStore.readMeta(projectId)
                ?: return
        val now = currentEpochMillis()
        ResMultiProjectFileStore.writeMeta(
            meta.copy(
                dirty = true,
                modifiedAtEpochMs = now,
            ),
        )
        val versionIndex = ResProjectVersionStore.readVersionIndex(projectId)
        if (versionIndex.versions.isNotEmpty()) {
            ResProjectVersionStore.writeVersionIndex(
                projectId,
                versionIndex.copy(dirty = true),
            )
        }
    }

    private fun stringsPath(
        projectId: String,
        language: ResMultiLanguageEntry,
    ): String =
        ResMultiProjectScanner.joinPath(
            ResMultiProjectFileStore.workspacePath(projectId),
            language.stringsRelativePath,
        )
}
