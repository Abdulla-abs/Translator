package `fun`.abbas.android_res_translator.core.ports

fun interface SecretsProvider {
    operator fun get(key: String): String?
}

/** 运行期密钥来源；各平台 `actual` 可接系统安全存储或开发用占位。 */
expect fun defaultSecretsProvider(): SecretsProvider
