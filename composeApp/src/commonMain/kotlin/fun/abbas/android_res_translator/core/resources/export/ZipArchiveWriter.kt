package `fun`.abbas.android_res_translator.core.resources.export

/** 将多个文件条目写入 ZIP 归档（.xlsx 即 ZIP）。 */
internal expect fun writeZipArchive(files: Map<String, ByteArray>): ByteArray
