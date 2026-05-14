package `fun`.abbas.android_res_translator.core.ports

class FakeSecretsProvider(
    private val map: Map<String, String>,
) : SecretsProvider {
    override fun get(key: String): String? = map[key]
}
