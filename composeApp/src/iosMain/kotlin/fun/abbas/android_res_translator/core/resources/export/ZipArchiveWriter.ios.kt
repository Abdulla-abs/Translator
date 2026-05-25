package `fun`.abbas.android_res_translator.core.resources.export

/** iOS 暂用纯 Kotlin STORED ZIP；桌面/Android 使用 java.util.zip。 */
internal actual fun writeZipArchive(files: Map<String, ByteArray>): ByteArray = StoredZipWriter.write(files)
