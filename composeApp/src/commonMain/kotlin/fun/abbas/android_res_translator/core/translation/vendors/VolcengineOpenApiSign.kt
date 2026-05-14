package `fun`.abbas.android_res_translator.core.translation.vendors

import org.kotlincrypto.hash.sha2.SHA256

/**
 * 火山引擎 OpenAPI 签名（与 [volc-openapi-demos/signature/golang/sign.go](https://github.com/volcengine/volc-openapi-demos/blob/main/signature/golang/sign.go) 一致）。
 * 用于 `open.volcengineapi.com` 等 OpenAPI 网关。
 */
internal object VolcengineOpenApiSign {

    private const val ALGORITHM = "HMAC-SHA256"

    fun buildAuthorization(
        accessKeyId: String,
        secretAccessKey: String,
        region: String,
        service: String,
        method: String,
        host: String,
        canonicalUri: String,
        canonicalQueryString: String,
        body: ByteArray,
        contentType: String,
        xDate: String,
    ): String {
        val shortDate = xDate.take(8)
        val payloadHash = sha256HexLower(body)
        val signedHeaders = listOf("host", "x-date", "x-content-sha256", "content-type")
        val headerLines =
            listOf(
                "host:$host",
                "x-date:$xDate",
                "x-content-sha256:$payloadHash",
                "content-type:$contentType",
            ).joinToString("\n")
        val canonicalRequest =
            listOf(
                method.uppercase(),
                canonicalUri,
                canonicalQueryString,
                headerLines + "\n",
                signedHeaders.joinToString(";"),
                payloadHash,
            ).joinToString("\n")
        val hashedCanonical = sha256HexLower(canonicalRequest.encodeToByteArray())
        val credentialScope = "$shortDate/$region/$service/request"
        val stringToSign =
            listOf(
                ALGORITHM,
                xDate,
                credentialScope,
                hashedCanonical,
            ).joinToString("\n")
        val derivedSigningKey = deriveSigningKey(secretAccessKey, shortDate, region, service)
        val signature = hmacSha256(derivedSigningKey, stringToSign.encodeToByteArray()).toHexLower()
        return "$ALGORITHM Credential=$accessKeyId/$credentialScope, SignedHeaders=${signedHeaders.joinToString(";")}, Signature=$signature"
    }

    private fun deriveSigningKey(
        secretKey: String,
        shortDate: String,
        region: String,
        service: String,
    ): ByteArray {
        val kDate = hmacSha256(secretKey.encodeToByteArray(), shortDate.encodeToByteArray())
        val kRegion = hmacSha256(kDate, region.encodeToByteArray())
        val kService = hmacSha256(kRegion, service.encodeToByteArray())
        return hmacSha256(kService, "request".encodeToByteArray())
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

    /** 与请求头 `X-Content-Sha256` 一致（小写十六进制）。 */
    fun requestPayloadSha256Hex(body: ByteArray): String = sha256HexLower(body)

    private fun sha256HexLower(data: ByteArray): String = sha256Digest(data).toHexLower()

    private fun ByteArray.toHexLower(): String = buildString(size * 2) {
        for (b in this@toHexLower) {
            val v = b.toInt() and 0xFF
            if (v < 16) append('0')
            append(v.toString(16))
        }
    }
}
