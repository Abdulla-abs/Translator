package `fun`.abbas.android_res_translator.core.resources.resmulti

import `fun`.abbas.android_res_translator.core.resources.usecase.DefaultValuesFolderCountryTransformer
import `fun`.abbas.android_res_translator.core.resources.usecase.ValuesFolderCountryTransformer
import `fun`.abbas.android_res_translator.core.resources.usecase.isValuesResourceFolderName
import `fun`.abbas.android_res_translator.persistence.fileExists
import `fun`.abbas.android_res_translator.persistence.isDirectoryPath
import `fun`.abbas.android_res_translator.persistence.listFileNamesInDirectory
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiLanguageEntry

data class ResMultiScanResult(
    val languages: List<ResMultiLanguageEntry>,
    val warnings: List<String>,
)

object ResMultiProjectScanner {
    private const val STRINGS_XML = "strings.xml"

    fun scanWorkspace(
        workspaceRoot: String,
        transformer: ValuesFolderCountryTransformer = DefaultValuesFolderCountryTransformer(),
    ): ResMultiScanResult {
        if (!isDirectoryPath(workspaceRoot)) {
            return ResMultiScanResult(emptyList(), listOf("Workspace is not a directory: $workspaceRoot"))
        }
        val warnings = mutableListOf<String>()
        val languages = mutableListOf<ResMultiLanguageEntry>()
        for (folderName in listFileNamesInDirectory(workspaceRoot).sorted()) {
            if (!isValuesResourceFolderName(folderName)) continue
            val folderPath = joinPath(workspaceRoot, folderName)
            if (!isDirectoryPath(folderPath)) continue
            val stringsPath = joinPath(folderPath, STRINGS_XML)
            if (!fileExists(stringsPath)) {
                warnings += "Skipped $folderName: missing strings.xml"
                continue
            }
            val langCode = transformer.folderNameToCountry(folderName)
            languages +=
                ResMultiLanguageEntry(
                    folderName = folderName,
                    langCode = langCode,
                    stringsRelativePath = "$folderName/$STRINGS_XML",
                )
        }
        if (languages.isEmpty()) {
            warnings += "No values folders with strings.xml were found under workspace"
        }
        return ResMultiScanResult(languages = languages, warnings = warnings)
    }

    internal fun joinPath(base: String, child: String): String =
        if (base.endsWith('/')) base + child else "$base/$child"
}
