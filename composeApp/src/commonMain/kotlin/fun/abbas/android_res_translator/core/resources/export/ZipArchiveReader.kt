package `fun`.abbas.android_res_translator.core.resources.export

internal expect fun readZipArchive(bytes: ByteArray): Map<String, ByteArray>
