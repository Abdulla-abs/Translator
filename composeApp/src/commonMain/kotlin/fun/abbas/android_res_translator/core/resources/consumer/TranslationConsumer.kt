package `fun`.abbas.android_res_translator.core.resources.consumer

import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import `fun`.abbas.android_res_translator.core.translation.TranslationOutcome

/** 可注入的「单段翻译」，便于测试替换为固定返回值（计划 Step 14.1）。 */
fun interface TranslationSegmentPort {
    suspend fun translateSegment(
        text: String,
        from: String,
        to: String,
    ): TranslationOutcome
}

/**
 * 在内存 [StringResourceFile] 上合并「源」与「目标」语言资源（对照旧工程 `TranslationConsumer`）。
 */
interface TranslationConsumer {
    suspend fun accept(
        source: StringResourceFile,
        sourceLang: String,
        target: StringResourceFile,
        targetLang: String,
        port: TranslationSegmentPort,
    ): StringResourceFile
}
