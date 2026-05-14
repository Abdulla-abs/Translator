package `fun`.abbas.android_res_translator.core.translation.vendors

import org.kotlincrypto.hash.sha2.SHA256

/**
 * 腾讯云 API 3.0 **TC3-HMAC-SHA256** 签名（[签名方法 v3](https://www.tencentcloud.com/document/product/213/33224)）。
 */
internal object TencentTc3Signer {

    private const val ALGORITHM = "TC3-HMAC-SHA256"
    private const val TERMINATOR = "tc3_request"

    /**
     * @param signedHeaders 参与签名的头：键、值均会按文档转为小写并 trim；键需已小写（如 `content-type`、`host`）。
     */
    fun buildAuthorization(
        secretId: String,
        secretKey: String,
        timestampSeconds: Long,
        service: String,
        requestPayload: ByteArray,
        signedHeaders: List<Pair<String, String>>,
    ): String {
        val timestamp = timestampSeconds.toString()
        val date = utcYmdDashFromUnixSeconds(timestampSeconds)
        val payloadHash = sha256HexLower(requestPayload)
        val sorted = signedHeaders.map { (k, v) -> k.lowercase().trim() to v.lowercase().trim() }.sortedBy { it.first }
        val canonicalHeaders =
            buildString {
                for ((k, v) in sorted) {
                    append(k)
                    append(':')
                    append(v)
                    append('\n')
                }
            }
        val signedHeaderNames = sorted.joinToString(";") { it.first }
        val canonicalRequest =
            listOf(
                "POST",
                "/",
                "",
                canonicalHeaders,
                signedHeaderNames,
                payloadHash,
            ).joinToString("\n")
        val hashedCanonical = sha256HexLower(canonicalRequest.encodeToByteArray())
        val credentialScope = "$date/$service/$TERMINATOR"
        val stringToSign =
            listOf(
                ALGORITHM,
                timestamp,
                credentialScope,
                hashedCanonical,
            ).joinToString("\n")
        val secretDate = hmacSha256(("TC3$secretKey").encodeToByteArray(), date.encodeToByteArray())
        val secretService = hmacSha256(secretDate, service.encodeToByteArray())
        val secretSigning = hmacSha256(secretService, TERMINATOR.encodeToByteArray())
        val signature = hmacSha256(secretSigning, stringToSign.encodeToByteArray()).toHexLower()
        return "$ALGORITHM Credential=$secretId/$credentialScope, SignedHeaders=$signedHeaderNames, Signature=$signature"
    }

    private fun utcYmdDashFromUnixSeconds(seconds: Long): String {
        require(seconds >= 0L) { "timestampSeconds must be non-negative" }
        val dayNumber = seconds / 86400L
        val (y, m, d) = utcYmdFromDaysSince1970(dayNumber)
        fun p2(n: Int) = n.toString().padStart(2, '0')
        return "$y-${p2(m)}-${p2(d)}"
    }

    private fun utcYmdFromDaysSince1970(dayNumber: Long): Triple<Int, Int, Int> {
        var d = dayNumber
        var y = 1970
        while (d >= daysInYear(y)) {
            d -= daysInYear(y)
            y++
        }
        var m = 1
        while (d >= daysInMonth(y, m)) {
            d -= daysInMonth(y, m)
            m++
        }
        return Triple(y, m, d.toInt() + 1)
    }

    private fun isLeapYear(y: Int): Boolean = y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)

    private fun daysInYear(y: Int): Long = if (isLeapYear(y)) 366L else 365L

    private fun daysInMonth(y: Int, m: Int): Long =
        when (m) {
            2 -> if (isLeapYear(y)) 29L else 28L
            4, 6, 9, 11 -> 30L
            else -> 31L
        }

    private fun hmacSha256(
        key: ByteArray,
        message: ByteArray,
    ): ByteArray {
        val blockSize = 64
        var keyBytes = key
        if (keyBytes.size > blockSize) {
            keyBytes = sha256Digest(keyBytes)
        }
        if (keyBytes.size < blockSize) {
            keyBytes = keyBytes + ByteArray(blockSize - keyBytes.size)
        }
        val ipad = ByteArray(blockSize) { i -> (keyBytes[i].toInt() xor 0x36).toByte() }
        val opad = ByteArray(blockSize) { i -> (keyBytes[i].toInt() xor 0x5c).toByte() }
        return sha256Digest(opad + sha256Digest(ipad + message))
    }

    private fun sha256Digest(data: ByteArray): ByteArray {
        val d = SHA256()
        d.update(data)
        return d.digest()
    }

    private fun sha256HexLower(data: ByteArray): String = sha256Digest(data).toHexLower()

    private fun ByteArray.toHexLower(): String = buildString(size * 2) {
        for (b in this@toHexLower) {
            val v = b.toInt() and 0xFF
            if (v < 16) append('0')
            append(v.toString(16))
        }
    }
}
