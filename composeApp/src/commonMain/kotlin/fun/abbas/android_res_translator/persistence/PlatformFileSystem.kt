package `fun`.abbas.android_res_translator.persistence

/** 应用内翻译项目根目录（各平台应用私有数据目录下）。 */
expect fun appTranslationProjectsRoot(): String

expect fun readTextFile(path: String): String

/** 先写临时文件再替换，降低崩溃时损坏风险。 */
expect fun writeTextFileAtomic(path: String, content: String)

expect fun ensureDirectory(path: String)

expect fun fileExists(path: String): Boolean

/** 递归删除文件或目录；路径不存在时视为成功。 */
expect fun deletePathRecursively(path: String): Boolean
