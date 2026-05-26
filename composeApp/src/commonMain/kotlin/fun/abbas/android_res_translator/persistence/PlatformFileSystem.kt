package `fun`.abbas.android_res_translator.persistence

/** 应用内翻译项目根目录（各平台应用私有数据目录下）。 */
expect fun appTranslationProjectsRoot(): String

/** 应用内 XML 比对项目根目录。 */
expect fun appCompareProjectsRoot(): String

/** 应用内多文件 res 项目根目录。 */
expect fun appResMultiProjectsRoot(): String

expect fun readTextFile(path: String): String

/** 先写临时文件再替换，降低崩溃时损坏风险。 */
expect fun writeTextFileAtomic(path: String, content: String)

expect fun ensureDirectory(path: String)

expect fun fileExists(path: String): Boolean

/** 递归删除文件或目录；路径不存在时视为成功。 */
expect fun deletePathRecursively(path: String): Boolean

/** 列出目录下直接子项名称（不含路径）。目录不存在时返回空列表。 */
expect fun listFileNamesInDirectory(directoryPath: String): List<String>

/** 路径是否为目录。 */
expect fun isDirectoryPath(path: String): Boolean

/**
 * 将 [sourcePath] 目录递归复制到 [destinationPath]（本地文件路径）。
 * JVM：源为文件系统路径；Android：源可为 `content://` 树 URI 或文件路径。
 */
expect fun copyDirectoryRecursively(sourcePath: String, destinationPath: String): Boolean
