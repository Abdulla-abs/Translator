package `fun`.abbas.android_res_translator.core

/** 墙钟毫秒（用于签名 `curtime` 等）；各平台 `actual` 实现。 */
internal expect fun currentEpochMillis(): Long
