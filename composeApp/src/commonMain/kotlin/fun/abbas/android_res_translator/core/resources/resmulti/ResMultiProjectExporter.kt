package `fun`.abbas.android_res_translator.core.resources.resmulti

import `fun`.abbas.android_res_translator.core.resources.export.StringsMatrixExporter
import `fun`.abbas.android_res_translator.persistence.ResMultiProjectFileStore
import `fun`.abbas.android_res_translator.persistence.fileExists
import `fun`.abbas.android_res_translator.persistence.readTextFile
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiLanguageEntry
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiProject

data class ResMultiXlsxExport(
    val bytes: ByteArray,
    val suggestedFileName: String,
)

object ResMultiProjectExporter {
    fun exportFull(project: ResMultiProject): Result<ResMultiXlsxExport> {
        val flats = loadLanguageFlats(project).getOrElse { return Result.failure(it) }
        val matrix =
            ResMultiStringsMatrixBuilder.buildFull(flats).getOrElse { return Result.failure(it) }
        val fileName = "${sanitizeFileName(project.displayName)}_all.xlsx"
        return Result.success(
            ResMultiXlsxExport(
                bytes = StringsMatrixExporter.encodeXlsx(matrix),
                suggestedFileName = fileName,
            ),
        )
    }

    fun exportSingle(
        project: ResMultiProject,
        language: ResMultiLanguageEntry,
    ): Result<ResMultiXlsxExport> {
        val flat =
            loadLanguageFlat(project.id, language).getOrElse { return Result.failure(it) }
        val matrix = ResMultiStringsMatrixBuilder.buildSingle(flat)
        val lang = language.langCode.ifBlank { "lang" }
        val fileName = "${sanitizeFileName(project.displayName)}_$lang.xlsx"
        return Result.success(
            ResMultiXlsxExport(
                bytes = StringsMatrixExporter.encodeXlsx(matrix),
                suggestedFileName = fileName,
            ),
        )
    }

    private fun loadLanguageFlats(project: ResMultiProject): Result<List<ResMultiLanguageFlat>> {
        if (project.languages.isEmpty()) {
            return Result.failure(
                IllegalStateException(ResMultiMatrixBuildError.NoLanguages().message),
            )
        }
        val flats = mutableListOf<ResMultiLanguageFlat>()
        for (lang in project.languages) {
            val flat = loadLanguageFlat(project.id, lang).getOrElse { return Result.failure(it) }
            flats += flat
        }
        return Result.success(flats)
    }

    private fun loadLanguageFlat(
        projectId: String,
        language: ResMultiLanguageEntry,
    ): Result<ResMultiLanguageFlat> {
        val path = stringsPath(projectId, language)
        if (!fileExists(path)) {
            return Result.failure(
                IllegalStateException(
                    "strings.xml not found: ${language.stringsRelativePath}",
                ),
            )
        }
        val xml = readTextFile(path)
        return ResMultiStringsMatrixBuilder.parseAndFlatten(language, xml)
    }

    private fun stringsPath(
        projectId: String,
        language: ResMultiLanguageEntry,
    ): String =
        ResMultiProjectScanner.joinPath(
            ResMultiProjectFileStore.workspacePath(projectId),
            language.stringsRelativePath,
        )

    private fun sanitizeFileName(name: String): String {
        val trimmed = name.trim().ifBlank { "res-multi" }
        return trimmed.replace(Regex("""[\\/:*?"<>|]"""), "_")
    }
}
