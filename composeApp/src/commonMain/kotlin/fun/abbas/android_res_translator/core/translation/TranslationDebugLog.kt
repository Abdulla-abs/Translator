package `fun`.abbas.android_res_translator.core.translation

/**
 * 翻译链路诊断日志。JVM 桌面端 `:composeApp:run` 时输出到 stdout，便于在 IDEA Run 窗口查看。
 */
object TranslationDebugLog {
    /** 设为 false 可关闭日志（默认开启便于排查）。 */
    var enabled: Boolean = true

    fun log(tag: String, message: String) {
        if (!enabled) return
        println("[AndroidResTranslator][$tag] $message")
    }
}
