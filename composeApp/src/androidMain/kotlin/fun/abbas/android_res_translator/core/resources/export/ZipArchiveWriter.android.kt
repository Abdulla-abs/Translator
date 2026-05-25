package `fun`.abbas.android_res_translator.core.resources.export

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal actual fun writeZipArchive(files: Map<String, ByteArray>): ByteArray {
    val output = ByteArrayOutputStream()
    ZipOutputStream(output).use { zip ->
        for ((path, data) in files.entries.sortedBy { it.key }) {
            zip.putNextEntry(ZipEntry(path))
            zip.write(data)
            zip.closeEntry()
        }
    }
    return output.toByteArray()
}
