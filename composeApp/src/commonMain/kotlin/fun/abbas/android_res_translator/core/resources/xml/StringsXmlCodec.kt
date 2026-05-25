@file:Suppress("DEPRECATION")

package `fun`.abbas.android_res_translator.core.resources.xml

import `fun`.abbas.android_res_translator.core.resources.model.PluralEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringArrayEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlStreaming

/**
 * Android `strings.xml` 子集编解码（**仅** `string` / `string-array` / `item`），基于 [xmlutil](https://github.com/pdvrieze/xmlutil) 的 [XmlStreaming](https://pdvrieze.github.io/xmlutil/)。
 *
 * 解析前会去掉 XML 声明，避免部分 KMP 读取器对声明/命名空间组合报错；序列化时写回标准声明。
 */
object StringsXmlCodec {

    fun parse(xml: String): StringResourceFile {
        val body = stripXmlDeclaration(xml.trim())
        val reader = XmlStreaming.newReader(body)
        val strings = linkedMapOf<String, StringEntry>()
        val arrays = linkedMapOf<String, StringArrayEntry>()
        val plurals = linkedMapOf<String, PluralEntry>()
        if (!reader.hasNext()) error("empty xml")
        var ev = reader.next()
        if (ev == EventType.START_DOCUMENT) {
            check(reader.hasNext()) { "xml ended after document start" }
            ev = reader.next()
        }
        check(ev == EventType.START_ELEMENT && reader.localName == "resources") {
            "expected <resources> root, got $ev ${reader.localName}"
        }
        while (reader.hasNext()) {
            when (reader.next()) {
                EventType.START_ELEMENT -> {
                    when (reader.localName) {
                        "string" -> {
                            val name = reader.attrName() ?: error("string without name")
                            val tr = reader.readTranslatableFlag()
                            val value = reader.readTextUntilMatchingEnd()
                            strings[name] = StringEntry(name = name, value = value, translatable = tr)
                        }
                        "string-array" -> {
                            val name = reader.attrName() ?: error("string-array without name")
                            val tr = reader.readTranslatableFlag()
                            val items = reader.readStringArrayItems()
                            arrays[name] = StringArrayEntry(name = name, items = items, translatable = tr)
                        }
                        "plurals" -> {
                            val name = reader.attrName() ?: error("plurals without name")
                            val tr = reader.readTranslatableFlag()
                            val items = reader.readPluralItems()
                            plurals[name] = PluralEntry(name = name, items = items, translatable = tr)
                        }
                        else -> reader.skipSubtree()
                    }
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == "resources") break
                }
                else -> Unit
            }
        }
        return StringResourceFile(strings = strings, stringArrays = arrays, plurals = plurals)
    }

    fun serialize(file: StringResourceFile): String =
        buildString {
            appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
            appendLine("<resources>")
            for ((_, e) in file.strings.entries.sortedBy { it.key }) {
                append("    <string name=\"")
                append(escapeAttr(e.name))
                append("\"")
                if (!e.translatable) {
                    append(" translatable=\"false\"")
                }
                append(">")
                append(escapeText(e.value))
                appendLine("</string>")
            }
            for ((_, arr) in file.stringArrays.entries.sortedBy { it.key }) {
                append("    <string-array name=\"")
                append(escapeAttr(arr.name))
                append("\"")
                if (!arr.translatable) {
                    append(" translatable=\"false\"")
                }
                appendLine(">")
                for (item in arr.items) {
                    append("        <item>")
                    append(escapeText(item))
                    appendLine("</item>")
                }
                appendLine("    </string-array>")
            }
            for ((_, plural) in file.plurals.entries.sortedBy { it.key }) {
                append("    <plurals name=\"")
                append(escapeAttr(plural.name))
                append("\"")
                if (!plural.translatable) {
                    append(" translatable=\"false\"")
                }
                appendLine(">")
                for ((quantity, value) in plural.items.entries.sortedBy { it.key }) {
                    append("        <item quantity=\"")
                    append(escapeAttr(quantity))
                    append("\">")
                    append(escapeText(value))
                    appendLine("</item>")
                }
                appendLine("    </plurals>")
            }
            appendLine("</resources>")
        }

    private fun stripXmlDeclaration(s: String): String =
        s.replaceFirst(XML_DECL_REGEX, "").trimStart()

    private val XML_DECL_REGEX = Regex("""<\?xml[^?]*\?>""")

    private fun nl.adaptivity.xmlutil.XmlReader.readTranslatableFlag(): Boolean {
        val n = attributeCount
        for (i in 0 until n) {
            if (getAttributeLocalName(i) == "translatable") {
                return getAttributeValue(i) != "false"
            }
        }
        return true
    }

    private fun nl.adaptivity.xmlutil.XmlReader.attrName(): String? {
        val n = attributeCount
        for (i in 0 until n) {
            if (getAttributeLocalName(i) == "name") {
                return getAttributeValue(i)
            }
        }
        return null
    }

    private fun nl.adaptivity.xmlutil.XmlReader.readTextUntilMatchingEnd(): String {
        val sb = StringBuilder()
        var innerDepth = 0
        while (hasNext()) {
            when (val ev = next()) {
                EventType.TEXT,
                EventType.ENTITY_REF,
                EventType.CDSECT,
                -> sb.append(text)
                EventType.START_ELEMENT -> innerDepth++
                EventType.END_ELEMENT -> {
                    if (innerDepth == 0) return sb.toString()
                    innerDepth--
                }
                else -> Unit
            }
        }
        return sb.toString()
    }

    private fun nl.adaptivity.xmlutil.XmlReader.readStringArrayItems(): List<String> {
        val items = mutableListOf<String>()
        while (hasNext()) {
            when (val ev = next()) {
                EventType.START_ELEMENT -> {
                    when (localName) {
                        "item" -> items.add(readTextUntilMatchingEnd())
                        else -> skipSubtree()
                    }
                }
                EventType.END_ELEMENT -> {
                    if (localName == "string-array") return items
                }
                else -> Unit
            }
        }
        return items
    }

    private fun nl.adaptivity.xmlutil.XmlReader.readPluralItems(): Map<String, String> {
        val items = linkedMapOf<String, String>()
        while (hasNext()) {
            when (val ev = next()) {
                EventType.START_ELEMENT -> {
                    when (localName) {
                        "item" -> {
                            val quantity = attrQuantity() ?: "other"
                            items[quantity] = readTextUntilMatchingEnd()
                        }
                        else -> skipSubtree()
                    }
                }
                EventType.END_ELEMENT -> {
                    if (localName == "plurals") return items
                }
                else -> Unit
            }
        }
        return items
    }

    private fun nl.adaptivity.xmlutil.XmlReader.attrQuantity(): String? {
        val n = attributeCount
        for (i in 0 until n) {
            if (getAttributeLocalName(i) == "quantity") {
                return getAttributeValue(i)
            }
        }
        return null
    }

    private fun nl.adaptivity.xmlutil.XmlReader.skipSubtree() {
        var depth = 1
        while (depth > 0 && hasNext()) {
            when (next()) {
                EventType.START_ELEMENT -> depth++
                EventType.END_ELEMENT -> depth--
                else -> Unit
            }
        }
    }

    private fun escapeAttr(s: String): String =
        buildString(s.length + 4) {
            for (ch in s) {
                when (ch) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&apos;")
                    else -> append(ch)
                }
            }
        }

    private fun escapeText(s: String): String =
        buildString(s.length + 4) {
            for (ch in s) {
                when (ch) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    else -> append(ch)
                }
            }
        }
}
