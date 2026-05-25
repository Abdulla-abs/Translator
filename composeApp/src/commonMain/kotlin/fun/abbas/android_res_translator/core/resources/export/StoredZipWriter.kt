package `fun`.abbas.android_res_translator.core.resources.export

/** 无压缩（STORED）ZIP，供 OOXML (.xlsx) 打包，全 KMP 平台可用。 */
internal object StoredZipWriter {
    fun write(files: Map<String, ByteArray>): ByteArray {
        val localParts = mutableListOf<ByteArray>()
        val centralParts = mutableListOf<ByteArray>()
        var offset = 0
        for ((path, data) in files.entries.sortedBy { it.key }) {
            val local = localFileHeader(path, data)
            val central = centralDirectoryHeader(path, data, offset)
            localParts += local
            centralParts += central
            offset += local.size
        }
        val centralDir = centralParts.fold(ByteArray(0)) { acc, part -> acc + part }
        val end =
            endOfCentralDirectory(
                entryCount = files.size,
                centralDirSize = centralDir.size,
                centralDirOffset = offset,
            )
        return localParts.fold(ByteArray(0)) { acc, part -> acc + part } + centralDir + end
    }

    private fun localFileHeader(path: String, data: ByteArray): ByteArray {
        val nameBytes = path.encodeToByteArray()
        val crc = crc32(data)
        val buf =
            ByteArray(30 + nameBytes.size + data.size)
        writeU32(buf, 0, 0x04034b50) // local file header signature PK\x03\x04
        writeU16(buf, 4, 0x0014) // version needed to extract
        writeU16(buf, 6, 0) // general purpose bit flag
        writeU16(buf, 8, 0) // compression method: stored
        writeU16(buf, 10, 0) // last mod file time
        writeU16(buf, 12, 0) // last mod file date
        writeU32(buf, 14, crc and 0xFFFFFFFF.toInt())
        writeU32(buf, 18, data.size)
        writeU32(buf, 22, data.size)
        writeU16(buf, 26, nameBytes.size)
        writeU16(buf, 28, 0) // extra field length
        nameBytes.copyInto(buf, 30) // file name starts at offset 30
        data.copyInto(buf, 30 + nameBytes.size)
        return buf
    }

    private fun centralDirectoryHeader(
        path: String,
        data: ByteArray,
        offset: Int,
    ): ByteArray {
        val nameBytes = path.encodeToByteArray()
        val crc = crc32(data)
        val buf = ByteArray(46 + nameBytes.size)
        writeU32(buf, 0, 0x02014b50)
        writeU16(buf, 4, 0x0314) // version made by
        writeU16(buf, 6, 0x0014) // version needed to extract
        writeU16(buf, 8, 0) // general purpose bit flag
        writeU16(buf, 10, 0) // compression method: stored
        writeU16(buf, 12, 0) // last mod file time
        writeU16(buf, 14, 0) // last mod file date
        writeU32(buf, 16, crc and 0xFFFFFFFF.toInt())
        writeU32(buf, 20, data.size)
        writeU32(buf, 24, data.size) // compressed size (stored)
        writeU16(buf, 28, nameBytes.size)
        writeU16(buf, 30, 0)
        writeU16(buf, 32, 0)
        writeU16(buf, 34, 0)
        writeU16(buf, 36, 0)
        writeU16(buf, 38, 0)
        writeU32(buf, 40, 0)
        writeU32(buf, 44, offset)
        nameBytes.copyInto(buf, 46)
        return buf
    }

    private fun endOfCentralDirectory(
        entryCount: Int,
        centralDirSize: Int,
        centralDirOffset: Int,
    ): ByteArray {
        val buf = ByteArray(22)
        writeU32(buf, 0, 0x06054b50)
        writeU16(buf, 4, 0)
        writeU16(buf, 6, 0)
        writeU16(buf, 8, entryCount)
        writeU16(buf, 10, entryCount)
        writeU32(buf, 12, centralDirSize)
        writeU32(buf, 16, centralDirOffset)
        writeU16(buf, 20, 0)
        return buf
    }

    private fun writeU16(buf: ByteArray, offset: Int, value: Int) {
        buf[offset] = (value and 0xFF).toByte()
        buf[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }

    private fun writeU32(buf: ByteArray, offset: Int, value: Int) {
        buf[offset] = (value and 0xFF).toByte()
        buf[offset + 1] = ((value shr 8) and 0xFF).toByte()
        buf[offset + 2] = ((value shr 16) and 0xFF).toByte()
        buf[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun crc32(data: ByteArray): Int {
        var crc = 0xFFFFFFFF.toInt()
        for (b in data) {
            crc = crc xor (b.toInt() and 0xFF)
            repeat(8) {
                crc =
                    if (crc and 1 != 0) {
                        0xEDB88320.toInt() xor (crc ushr 1)
                    } else {
                        crc ushr 1
                    }
            }
        }
        return crc.inv()
    }
}

private operator fun ByteArray.plus(other: ByteArray): ByteArray {
    val out = ByteArray(size + other.size)
    copyInto(out)
    other.copyInto(out, size)
    return out
}
