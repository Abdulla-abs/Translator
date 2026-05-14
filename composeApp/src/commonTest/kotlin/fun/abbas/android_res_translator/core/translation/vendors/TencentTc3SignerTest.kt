package `fun`.abbas.android_res_translator.core.translation.vendors

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * TC3 向量：与官方文档 CVM 示例相同的 payload / 时间戳，[HashedCanonicalRequest](https://www.tencentcloud.com/document/product/213/33224) 为 `28158430…`；
 * 完整 `Authorization` 使用固定测试密钥，与 Python `hashlib`/`hmac` 独立计算结果一致。
 */
class TencentTc3SignerTest {

    @Test
    fun cvmDocVector_authorizationMatchesGolden() {
        val payload =
            """{"Limit": 1, "Filters": [{"Values": ["unnamed"], "Name": "instance-name"}]}"""
                .encodeToByteArray()
        val auth =
            TencentTc3Signer.buildAuthorization(
                secretId = "AKIDunittest",
                secretKey = "TestSecretKeyForUnitTest123456",
                timestampSeconds = 1551113065L,
                service = "cvm",
                requestPayload = payload,
                signedHeaders =
                    listOf(
                        "content-type" to "application/json; charset=utf-8",
                        "host" to "cvm.tencentcloudapi.com",
                    ),
            )
        assertEquals(
            "TC3-HMAC-SHA256 Credential=AKIDunittest/2019-02-25/cvm/tc3_request, " +
                "SignedHeaders=content-type;host, " +
                "Signature=9d2908f030589957e67a90707292ff12137316ac5d450ada675cb0a27b756b26",
            auth,
        )
    }

    @Test
    fun authorizationHasExpectedPrefixAndCredentialScope() {
        val auth =
            TencentTc3Signer.buildAuthorization(
                secretId = "AKIDx",
                secretKey = "key",
                timestampSeconds = 1551113065L,
                service = "tmt",
                requestPayload = "{}".encodeToByteArray(),
                signedHeaders =
                    listOf(
                        "content-type" to "application/json; charset=utf-8",
                        "host" to "tmt.tencentcloudapi.com",
                    ),
            )
        assertTrue(auth.startsWith("TC3-HMAC-SHA256 Credential="))
        assertTrue(auth.contains("/2019-02-25/tmt/tc3_request"))
        assertTrue(auth.contains("SignedHeaders=content-type;host"))
        assertTrue(auth.contains("Signature="))
    }
}
