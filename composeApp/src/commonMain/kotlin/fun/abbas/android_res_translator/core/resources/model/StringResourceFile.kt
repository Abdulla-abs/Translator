package `fun`.abbas.android_res_translator.core.resources.model

/** 单条 `<string name="…">…</string>`；[translatable] 为 `false` 表示资源上声明了 `translatable="false"`，与旧版跳过逻辑一致。 */
data class StringEntry(
    val name: String,
    val value: String,
    val translatable: Boolean = true,
)

/** `<string-array name="…">` 下若干 `<item>`。 */
data class StringArrayEntry(
    val name: String,
    val items: List<String>,
    val translatable: Boolean = true,
)

/**
 * `values` 目录下 `strings.xml` 的内存模型（阶段一子集：`string` + `string-array`）。
 * 其他标签解析时整段跳过；序列化时不会输出。
 */
data class StringResourceFile(
    val strings: Map<String, StringEntry> = emptyMap(),
    val stringArrays: Map<String, StringArrayEntry> = emptyMap(),
    val plurals: Map<String, PluralEntry> = emptyMap(),
) {
    /** 计划文档中的 `entries["app_name"]` 语义：`<string>` 的 name → 文本。 */
    val entries: Map<String, String>
        get() = strings.mapValues { it.value.value }
}
