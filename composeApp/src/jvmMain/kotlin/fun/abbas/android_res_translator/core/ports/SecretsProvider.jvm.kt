package `fun`.abbas.android_res_translator.core.ports

private object EmptySecretsProvider : SecretsProvider {
    override fun get(key: String): String? = null
}

actual fun defaultSecretsProvider(): SecretsProvider = EmptySecretsProvider
