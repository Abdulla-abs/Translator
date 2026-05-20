package `fun`.abbas.android_res_translator.ui.screens.main

import `fun`.abbas.android_res_translator.core.resources.xml.StringsXmlCodec
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun countTranslatableKeys(xml: String): Int =
    runCatching {
        val file = StringsXmlCodec.parse(xml)
        file.strings.size + file.stringArrays.values.sumOf { it.items.size }
    }.getOrDefault(0)

@OptIn(ExperimentalTime::class)
fun recentProjectFromXml(
    xml: String,
    displayName: String,
): RecentXmlProject {
    val total = countTranslatableKeys(xml).coerceAtLeast(1)
    return RecentXmlProject(
        id = displayName + "_" + Random.nextInt(),
        displayName = displayName,
        modifiedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
        progressPercent = 0f,
        translatedKeys = 0,
        totalKeys = total,
        isComplete = false,
        sourceXml = xml,
    )
}
