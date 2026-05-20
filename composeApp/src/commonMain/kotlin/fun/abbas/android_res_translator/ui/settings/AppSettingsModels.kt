package `fun`.abbas.android_res_translator.ui.settings

import `fun`.abbas.android_res_translator.ui.translation.ActiveTranslationEngine

/** 与 `StringsXmlCodec` 合并策略对应。 */
enum class ConsumerMode {
    FILLED,
    ALL_REPLACE,
}

/**
 * 应用内设置快照：厂商密钥（键名与 [config.properties.example] 一致）与翻译全局选项。
 */
data class AppSettingsSnapshot(
    val lingvanexToken: String = "",
    val tencentSecretId: String = "",
    val tencentSecretKey: String = "",
    val tencentRegion: String = "",
    val baiduAppId: String = "",
    val baiduSecretKey: String = "",
    val youdaoAppId: String = "",
    val youdaoSecretKey: String = "",
    val huoshanAccessKeyId: String = "",
    val huoshanSecretAccessKey: String = "",
    val defaultSourceLang: String = "en",
    val defaultTargetLang: String = "zh",
    val preferredTranslationEngine: ActiveTranslationEngine? = null,
    val appAppearance: AppAppearance = AppAppearance.Classic,
    val consumerMode: ConsumerMode = ConsumerMode.FILLED,
    val forceTranslation: Boolean = false,
) {
    /** 仅非空项，供 [SecretsProvider] 读取（空串表示未配置）。 */
    fun toSecretsMap(): Map<String, String> = buildMap {
        fun putIfNonBlank(key: String, value: String) {
            if (value.isNotBlank()) put(key, value.trim())
        }
        putIfNonBlank("lingvanex.token", lingvanexToken)
        putIfNonBlank("tencent.secretId", tencentSecretId)
        putIfNonBlank("tencent.secretKey", tencentSecretKey)
        putIfNonBlank("tencent.region", tencentRegion)
        putIfNonBlank("baidu.appId", baiduAppId)
        putIfNonBlank("baidu.secretKey", baiduSecretKey)
        putIfNonBlank("youdao.appId", youdaoAppId)
        putIfNonBlank("youdao.secretKey", youdaoSecretKey)
        putIfNonBlank("huoshan.accessKeyID", huoshanAccessKeyId)
        putIfNonBlank("huoshan.secretAccessKey", huoshanSecretAccessKey)
    }

    companion object {
        const val KEY_DEFAULT_SOURCE = "ui.defaultSourceLang"
        const val KEY_DEFAULT_TARGET = "ui.defaultTargetLang"
        const val KEY_PREFERRED_ENGINE = "ui.preferredTranslationEngine"
        const val KEY_APP_APPEARANCE = "ui.appAppearance"
        const val KEY_CONSUMER_MODE = "ui.consumerMode"
        const val KEY_FORCE_TRANSLATION = "ui.forceTranslation"

        fun fromFlatMap(map: Map<String, String>): AppSettingsSnapshot =
            AppSettingsSnapshot(
                lingvanexToken = map["lingvanex.token"].orEmpty(),
                tencentSecretId = map["tencent.secretId"].orEmpty(),
                tencentSecretKey = map["tencent.secretKey"].orEmpty(),
                tencentRegion = map["tencent.region"].orEmpty(),
                baiduAppId = map["baidu.appId"].orEmpty(),
                baiduSecretKey = map["baidu.secretKey"].orEmpty(),
                youdaoAppId = map["youdao.appId"].orEmpty(),
                youdaoSecretKey = map["youdao.secretKey"].orEmpty(),
                huoshanAccessKeyId = map["huoshan.accessKeyID"].orEmpty(),
                huoshanSecretAccessKey = map["huoshan.secretAccessKey"].orEmpty(),
                defaultSourceLang = map[KEY_DEFAULT_SOURCE]?.takeIf { it.isNotBlank() } ?: "en",
                defaultTargetLang = map[KEY_DEFAULT_TARGET]?.takeIf { it.isNotBlank() } ?: "zh",
                preferredTranslationEngine =
                    ActiveTranslationEngine.fromPersisted(map[KEY_PREFERRED_ENGINE]),
                appAppearance = AppAppearance.fromPersisted(map[KEY_APP_APPEARANCE]),
                consumerMode =
                    when (map[KEY_CONSUMER_MODE]?.uppercase()) {
                        "ALL_REPLACE" -> ConsumerMode.ALL_REPLACE
                        else -> ConsumerMode.FILLED
                    },
                forceTranslation = map[KEY_FORCE_TRANSLATION]?.equals("true", ignoreCase = true) == true,
            )
    }
}

fun AppSettingsSnapshot.toPersistenceMap(): Map<String, String> =
    buildMap {
        put("lingvanex.token", lingvanexToken)
        put("tencent.secretId", tencentSecretId)
        put("tencent.secretKey", tencentSecretKey)
        put("tencent.region", tencentRegion)
        put("baidu.appId", baiduAppId)
        put("baidu.secretKey", baiduSecretKey)
        put("youdao.appId", youdaoAppId)
        put("youdao.secretKey", youdaoSecretKey)
        put("huoshan.accessKeyID", huoshanAccessKeyId)
        put("huoshan.secretAccessKey", huoshanSecretAccessKey)
        put(AppSettingsSnapshot.KEY_DEFAULT_SOURCE, defaultSourceLang)
        put(AppSettingsSnapshot.KEY_DEFAULT_TARGET, defaultTargetLang)
        preferredTranslationEngine?.let { put(AppSettingsSnapshot.KEY_PREFERRED_ENGINE, it.name) }
        put(AppSettingsSnapshot.KEY_APP_APPEARANCE, appAppearance.name)
        put(AppSettingsSnapshot.KEY_CONSUMER_MODE, consumerMode.name)
        put(AppSettingsSnapshot.KEY_FORCE_TRANSLATION, forceTranslation.toString())
    }
