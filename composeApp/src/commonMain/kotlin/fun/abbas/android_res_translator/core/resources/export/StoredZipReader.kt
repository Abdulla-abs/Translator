package `fun`.abbas.android_res_translator.core.resources.export

/** 读取无压缩（STORED）ZIP，与 [StoredZipWriter] 输出格式兼容。 */
internal object StoredZipReader {
    fun read(zipBytes: ByteArray): Map<String, ByteArray> {
        val eocdOffset = findEndOfCentralDirectory(zipBytes) ?: error("Invalid ZIP: missing end of central directory")
        val entryCount = readU16(zipBytes, eocdOffset + 10)
        val centralDirSize = readU32(zipBytes, eocdOffset + 12)
        val centralDirOffset = readU32(zipBytes, eocdOffset + 16)
        require(entryCount > 0) { "ZIP has no entries" }
        val result = linkedMapOf<String, ByteArray>()
        var pos = centralDirOffset
        val centralEnd = centralDirOffset + centralDirSize
        repeat(entryCount) {
            require(pos + 46 <= centralEnd) { "Truncated central directory" }
            val signature = readU32(zipBytes, pos)
            require(signature == 0x02014b50) { "Invalid central directory header at $pos" }
            val compression = readU16(zipBytes, pos + 10)
            require(compression == 0) { "Only STORED entries are supported" }
            val compressedSize = readU32(zipBytes, pos + 20)
            val nameLen = readU16(zipBytes, pos + 28)
            val extraLen = readU16(zipBytes, pos + 30)
            val commentLen = readU16(zipBytes, pos + 32)
            val localHeaderOffset = readU32(zipBytes, pos + 42)
            val nameStart = pos + 46
            val name = zipBytes.decodeToString(nameStart, nameStart + nameLen)
            pos = nameStart + nameLen + extraLen + commentLen
            val data = readLocalFileData(zipBytes, localHeaderOffset, compressedSize)
            result[name] = data
        }
        return result
    }

    private fun readLocalFileData(
        zipBytes: ByteArray,
        offset: Int,
        expectedSize: Int,
    ): ByteArray {
        require(offset + 30 <= zipBytes.size) { "Truncated local header" }
        val signature = readU32(zipBytes, offset)
        require(signature == 0x04034b50) { "Invalid local file header at $offset" }
        val compression = readU16(zipBytes, offset + 8)
        require(compression == 0) { "Only STORED entries are supported" }
        val compressedSize = readU32(zipBytes, offset + 18)
        require(compressedSize == expectedSize) { "Local/central size mismatch" }
        val nameLen = readU16(zipBytes, offset + 26)
        val extraLen = readU16(zipBytes, offset + 28)
        val dataStart = offset + 30 + nameLen + extraLen
        val dataEnd = dataStart + compressedSize
        require(dataEnd <= zipBytes.size) { "Truncated file data" }
        return zipBytes.copyOfRange(dataStart, dataEnd)
    }

    private fun findEndOfCentralDirectory(bytes: ByteArray): Int? {
        if (bytes.size < 22) return null
        val minStart = (bytes.size - 22 - 0xFFFF).coerceAtLeast(0)
        for (i in bytes.size - 22 downTo minStart) {
            if (readU32(bytes, i) != 0x06054b50) continue
            val commentLen = readU16(bytes, i + 20)
            if (i + 22 + commentLen != bytes.size) continue
            val centralDirSize = readU32(bytes, i + 12)
            val centralDirOffset = readU32(bytes, i + 16)
            if (centralDirOffset < 0 || centralDirSize < 0) continue
            if (centralDirOffset + centralDirSize != i) continue
            if (centralDirOffset + 4 > bytes.size) continue
            if (readU32(bytes, centralDirOffset) != 0x02014b50) continue
            val entryCount = readU16(bytes, i + 10)
            if (entryCount <= 0) continue
            return i
        }
        return null
    }

    private fun readU16(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)

    private fun readU32(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 3].toInt() and 0xFF) shl 24)
}
