package `fun`.abbas.android_res_translator.core.resources.export

import java.util.zip.ZipInputStream

internal actual fun readZipArchive(bytes: ByteArray): Map<String, ByteArray> {
    val result = linkedMapOf<String, ByteArray>()
    ZipInputStream(bytes.inputStream()).use { zip ->
        var entry = zip.nextEntry
        while (entry != null) {
            result[entry.name] = zip.readBytes()
            entry = zip.nextEntry
        }
    }
    return result
}
