package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.AndroidSettingsContext
import java.io.File

actual fun appTranslationProjectsRoot(): String =
    File(AndroidSettingsContext.require().filesDir, "translation-projects").absolutePath

actual fun readTextFile(path: String): String = File(path).readText(Charsets.UTF_8)

actual fun writeTextFileAtomic(path: String, content: String) {
    val target = File(path)
    target.parentFile?.mkdirs()
    val tmp = File("$path.tmp")
    tmp.writeText(content, Charsets.UTF_8)
    if (target.exists()) target.delete()
    tmp.renameTo(target)
}

actual fun ensureDirectory(path: String) {
    File(path).mkdirs()
}

actual fun fileExists(path: String): Boolean = File(path).exists()

actual fun deletePathRecursively(path: String): Boolean {
    val root = File(path)
    if (!root.exists()) return true
    return root.walkBottomUp().fold(true) { ok, file -> file.delete() && ok }
}
