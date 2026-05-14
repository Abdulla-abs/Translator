package `fun`.abbas.android_res_translator.core.resources.usecase

import `fun`.abbas.android_res_translator.core.ports.FileTreePort
import `fun`.abbas.android_res_translator.core.resources.consumer.TranslationConsumer
import `fun`.abbas.android_res_translator.core.resources.consumer.TranslationSegmentPort
import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import `fun`.abbas.android_res_translator.core.resources.xml.StringsXmlCodec

/**
 * 遍历 `res` 根下 `values` / `values-*` 目录，按 [ValuesFolderCountryTransformer] 归类后，
 * 以源语言目录为基准对其余语言目录的 `strings.xml` 执行 [TranslationConsumer] 并 suspend 写回（对照旧工程 `translationEntireValueFolder`；写盘可等待）。
 */
class TranslateValuesFolderUseCase(
    private val files: FileTreePort,
    private val consumer: TranslationConsumer,
) {
    suspend fun invoke(
        resRootPath: String,
        transformer: ValuesFolderCountryTransformer,
        port: TranslationSegmentPort,
        stringsFileName: String = "strings.xml",
    ) {
        val root = resRootPath.normalizePath()
        val children = files.listChildren(root)
        val valueDirs =
            children.filter { it.isDirectory && isValuesResourceFolderName(it.name) }
        val countryToDirPath =
            valueDirs.associate { transformer.folderNameToCountry(it.name) to it.path.normalizePath() }

        val sourcePath =
            countryToDirPath[transformer.sourceCountry]
                ?: error("source language folder not found for country=${transformer.sourceCountry}")

        val sourceXml = files.readUtf8(joinPath(sourcePath, stringsFileName))
        val sourceModel = StringsXmlCodec.parse(sourceXml)

        for ((country, dirPath) in countryToDirPath) {
            if (country == transformer.sourceCountry) continue
            val targetFilePath = joinPath(dirPath, stringsFileName)
            val targetXml =
                try {
                    files.readUtf8(targetFilePath)
                } catch (_: Exception) {
                    ""
                }
            val targetModel =
                if (targetXml.isBlank()) {
                    StringResourceFile()
                } else {
                    StringsXmlCodec.parse(targetXml)
                }
            val merged = consumer.accept(sourceModel, transformer.sourceCountry, targetModel, country, port)
            files.writeUtf8(targetFilePath, StringsXmlCodec.serialize(merged))
        }
    }

    private companion object {
        fun String.normalizePath(): String = trimEnd('/').replace('\\', '/')

        fun joinPath(
            dir: String,
            file: String,
        ): String {
            val d = dir.normalizePath()
            val f = file.trimStart('/').replace('\\', '/')
            return "$d/$f"
        }
    }
}
