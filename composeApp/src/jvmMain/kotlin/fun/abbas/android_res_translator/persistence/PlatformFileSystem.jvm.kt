package `fun`.abbas.android_res_translator.persistence

import java.io.File

/** 单元测试可注入临时目录。 */
internal var translationProjectsRootOverride: String? = null

actual fun appTranslationProjectsRoot(): String =
    translationProjectsRootOverride
        ?: File(
            System.getProperty("user.home"),
            ".android_res_translator${File.separator}translation-projects",
        ).absolutePath

actual fun readTextFile(path: String): String = File(path).readText(Charsets.UTF_8)

actual fun writeTextFileAtomic(path: String, content: String) {
    val target = File(path)
    target.parentFile?.mkdirs()
    val tmp = File("$path.tmp")
    tmp.writeText(content, Charsets.UTF_8)
    if (target.exists()) target.delete()
    check(tmp.renameTo(target)) { "无法写入 $path" }
}

actual fun ensureDirectory(path: String) {
    File(path).mkdirs()
}

actual fun fileExists(path: String): Boolean = File(path).exists()
