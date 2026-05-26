package `fun`.abbas.android_res_translator.core.resources.export

/**
 * 最小 OOXML 读取器：解析本应用 [MinimalXlsxEncoder] 产出的单表 xlsx（sharedStrings + sheet1）。
 */
object MinimalXlsxDecoder {
    fun decode(bytes: ByteArray): StringsMatrix {
        val entries = readZipArchive(bytes)
        val sharedXml =
            entries["xl/sharedStrings.xml"]?.decodeToString()
                ?: error("Missing xl/sharedStrings.xml")
        val sheetXml =
            entries["xl/worksheets/sheet1.xml"]?.decodeToString()
                ?: error("Missing xl/worksheets/sheet1.xml")
        val sharedStrings = parseSharedStrings(sharedXml)
        val table = parseSheet(sheetXml, sharedStrings)
        require(table.isNotEmpty()) { "Spreadsheet is empty" }
        val headers = table.first()
        val rows =
            table.drop(1).mapNotNull { cells ->
                val key = cells.firstOrNull()?.trim().orEmpty()
                if (key.isEmpty()) return@mapNotNull null
                StringsMatrixRow(
                    key = key,
                    valuesByColumn = listOf(key) + cells.drop(1),
                )
            }
        return StringsMatrix(columnHeaders = headers, rows = rows)
    }

    internal fun parseSharedStrings(xml: String): List<String> {
        val strings = mutableListOf<String>()
        val siRegex = Regex("""<si\b[^>]*>(.*?)</si>""", RegexOption.DOT_MATCHES_ALL)
        for (match in siRegex.findAll(xml)) {
            val body = match.groupValues[1]
            val text =
                Regex("""<t[^>]*>(.*?)</t>""", RegexOption.DOT_MATCHES_ALL)
                    .find(body)
                    ?.groupValues
                    ?.get(1)
                    ?.let(::unescapeXml)
                    .orEmpty()
            strings += text
        }
        return strings
    }

    internal fun parseSheet(
        xml: String,
        sharedStrings: List<String>,
    ): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val rowRegex = Regex("""<row\b[^>]*>(.*?)</row>""", RegexOption.DOT_MATCHES_ALL)
        for (rowMatch in rowRegex.findAll(xml)) {
            val rowBody = rowMatch.groupValues[1]
            val cells = mutableMapOf<Int, String>()
            val cellRegex = Regex("""<c\b([^>]*)>(.*?)</c>""", RegexOption.DOT_MATCHES_ALL)
            for (cellMatch in cellRegex.findAll(rowBody)) {
                val attrs = cellMatch.groupValues[1]
                val inner = cellMatch.groupValues[2]
                val ref = ATTR_REF.find(attrs)?.groupValues?.get(1) ?: continue
                val col = columnIndexFromRef(ref)
                val value =
                    when {
                        attrs.contains("""t="s"""") || attrs.contains("""t='s'""") -> {
                            val idx =
                                Regex("""<v>(\d+)</v>""")
                                    .find(inner)
                                    ?.groupValues
                                    ?.get(1)
                                    ?.toIntOrNull()
                                    ?: 0
                            sharedStrings.getOrElse(idx) { "" }
                        }
                        else -> {
                            Regex("""<v>(.*?)</v>""", RegexOption.DOT_MATCHES_ALL)
                                .find(inner)
                                ?.groupValues
                                ?.get(1)
                                .orEmpty()
                        }
                    }
                cells[col] = value
            }
            if (cells.isEmpty()) continue
            val maxCol = cells.keys.max()
            val rowValues = (0..maxCol).map { col -> cells[col].orEmpty() }
            rows += rowValues
        }
        return rows
    }

    private val ATTR_REF = Regex("""\br="([^"]+)"""")

    internal fun columnIndexFromRef(ref: String): Int {
        val letters = ref.takeWhile { it.isLetter() }
        var col = 0
        for (ch in letters.uppercase()) {
            col = col * 26 + (ch.code - 'A'.code + 1)
        }
        return col - 1
    }

    private fun unescapeXml(text: String): String =
        text
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
            .replace("&#10;", "\n")
}

object StringsMatrixImporter {
    fun decodeXlsx(bytes: ByteArray): StringsMatrix = MinimalXlsxDecoder.decode(bytes)
}
