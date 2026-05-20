package `fun`.abbas.android_res_translator.util

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun currentEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()
