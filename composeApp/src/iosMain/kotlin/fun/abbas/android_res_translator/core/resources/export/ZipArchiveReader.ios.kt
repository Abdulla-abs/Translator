package `fun`.abbas.android_res_translator.core.resources.export

internal actual fun readZipArchive(bytes: ByteArray): Map<String, ByteArray> = StoredZipReader.read(bytes)
