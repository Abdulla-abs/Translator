package `fun`.abbas.android_res_translator.core.ports

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FakeSecretsProviderTest {
    @Test
    fun returnsConfiguredValue() {
        val s = FakeSecretsProvider(mapOf("baidu.appId" to "id123"))
        assertEquals("id123", s["baidu.appId"])
    }

    @Test
    fun returnsNullForMissingKey() {
        val s = FakeSecretsProvider(emptyMap())
        assertNull(s["missing"])
    }
}
