package `fun`.abbas.android_res_translator.ui.screens.main

import `fun`.abbas.android_res_translator.core.resources.xml.StringsXmlCodec

fun countTranslatableKeys(xml: String): Int =
    runCatching {
        val file = StringsXmlCodec.parse(xml)
        file.strings.size + file.stringArrays.values.sumOf { it.items.size }
    }.getOrDefault(0)
