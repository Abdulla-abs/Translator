package `fun`.abbas.android_res_translator.core.resources.resmulti

import `fun`.abbas.android_res_translator.core.resources.export.StringsMatrix
import `fun`.abbas.android_res_translator.core.resources.export.StringsMatrixImporter
import `fun`.abbas.android_res_translator.persistence.ResMultiProjectFileStore
import `fun`.abbas.android_res_translator.persistence.fileExists
import `fun`.abbas.android_res_translator.persistence.readTextFile
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiProject

object ResMultiProjectImportService {
    fun decodeSpreadsheet(bytes: ByteArray): Result<StringsMatrix> =
        runCatching { StringsMatrixImporter.decodeXlsx(bytes) }

    fun loadWorkspaceFlats(project: ResMultiProject): Result<List<ResMultiLanguageFlat>> {
        if (project.languages.isEmpty()) {
            return Result.failure(IllegalStateException("No languages in project"))
        }
        val flats = mutableListOf<ResMultiLanguageFlat>()
        for (lang in project.languages) {
            val path =
                ResMultiProjectScanner.joinPath(
                    ResMultiProjectFileStore.workspacePath(project.id),
                    lang.stringsRelativePath,
                )
            if (!fileExists(path)) {
                return Result.failure(IllegalStateException("Missing ${lang.stringsRelativePath}"))
            }
            val flat =
                ResMultiStringsMatrixBuilder
                    .parseAndFlatten(lang, readTextFile(path))
                    .getOrElse { return Result.failure(it) }
            flats += flat
        }
        return Result.success(flats)
    }

    fun buildCompareMatrix(
        project: ResMultiProject,
        imported: StringsMatrix,
    ): Result<ResMultiImportCompareMatrix> {
        val workspaceFlats = loadWorkspaceFlats(project).getOrElse { return Result.failure(it) }
        if (imported.columnHeaders.isEmpty()) {
            return Result.failure(IllegalStateException("Imported spreadsheet has no headers"))
        }
        return Result.success(
            ResMultiImportCompareBuilder.build(
                imported = imported,
                workspaceFlats = workspaceFlats,
                languages = project.languages,
            ),
        )
    }
}
