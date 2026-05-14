package `fun`.abbas.android_res_translator.core.resources.usecase

import `fun`.abbas.android_res_translator.core.resources.consumer.TranslationConsumer
import `fun`.abbas.android_res_translator.core.resources.consumer.TranslationSegmentPort
import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import `fun`.abbas.android_res_translator.core.resources.xml.StringsXmlCodec

/**
 * 单文件 `strings.xml`：解析 XML → [TranslationConsumer] 合并 → 再序列化（对照旧 `translationXXtoXX` 中内存合并部分）。
 */
class TranslateStringsXmlUseCase(
    private val consumer: TranslationConsumer,
) {
    suspend fun invoke(
        sourceXml: String,
        sourceLang: String,
        targetXml: String,
        targetLang: String,
        port: TranslationSegmentPort,
    ): String {
        val source = StringsXmlCodec.parse(sourceXml)
        val target =
            if (targetXml.isBlank()) {
                StringResourceFile()
            } else {
                StringsXmlCodec.parse(targetXml)
            }
        val merged = consumer.accept(source, sourceLang, target, targetLang, port)
        return StringsXmlCodec.serialize(merged)
    }
}
