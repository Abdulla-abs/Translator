package `fun`.abbas.android_res_translator.core.resources.export

/**
 * 最小 OOXML 写入器：单工作表 + sharedStrings，无外部依赖。
 */
object MinimalXlsxEncoder {
    fun encode(
        matrix: StringsMatrix,
        sheetName: String = "strings",
    ): ByteArray {
        val table = buildTable(matrix)
        val shared = buildSharedStrings(table)
        val sheet = buildSheetXml(table, shared.indexByText)
        val files =
            mapOf(
                "[Content_Types].xml" to contentTypesXml(),
                "_rels/.rels" to rootRelsXml(),
                "xl/_rels/workbook.xml.rels" to workbookRelsXml(),
                "xl/workbook.xml" to workbookXml(sheetName),
                "xl/styles.xml" to stylesXml(),
                "xl/sharedStrings.xml" to shared.xml,
                "xl/worksheets/sheet1.xml" to sheet,
            )
        return writeZipArchive(files)
    }

    private data class Table(
        val rows: List<List<String>>,
    )

    private data class SharedStrings(
        val xml: ByteArray,
        val indexByText: Map<String, Int>,
    )

    private fun buildTable(matrix: StringsMatrix): Table {
        val headerRow = matrix.columnHeaders
        val dataRows = matrix.rows.map { row -> listOf(row.key) + row.valuesByColumn.drop(1) }
        return Table(rows = listOf(headerRow) + dataRows)
    }

    private fun buildSharedStrings(table: Table): SharedStrings {
        val texts = linkedSetOf<String>()
        var totalRefs = 0
        for (row in table.rows) {
            for (cell in row) {
                texts += cell
                totalRefs++
            }
        }
        val indexByText = texts.withIndex().associate { (i, t) -> t to i }
        val body =
            buildString {
                append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
                append("""<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" """)
                append("""xmlns:xml="http://www.w3.org/XML/1998/namespace" """)
                append("""count="$totalRefs" uniqueCount="${texts.size}">""")
                for (text in texts) {
                    append("<si><t")
                    if (text.startsWith(' ') || text.endsWith(' ')) {
                        append(""" xml:space="preserve"""")
                    }
                    append(">")
                    append(escapeXml(text))
                    append("</t></si>")
                }
                append("</sst>")
            }
        return SharedStrings(xml = body.encodeToByteArray(), indexByText = indexByText)
    }

    private fun buildSheetXml(
        table: Table,
        indexByText: Map<String, Int>,
    ): ByteArray {
        val body =
            buildString {
                append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
                append(
                    """<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" """,
                )
                append("""xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">""")
                val lastRow = table.rows.size
                val lastCol = table.rows.maxOfOrNull { it.size } ?: 1
                append("""<dimension ref="A1:${cellRef(lastCol - 1, lastRow)}"/>""")
                append("<sheetViews><sheetView workbookViewId=\"0\"/></sheetViews>")
                append("<sheetFormatPr defaultRowHeight=\"15\"/>")
                append("<sheetData>")
                table.rows.forEachIndexed { rowIndex, row ->
                    val r = rowIndex + 1
                    append("""<row r="$r">""")
                    row.forEachIndexed { colIndex, value ->
                        val ref = cellRef(colIndex, r)
                        val idx = indexByText[value] ?: 0
                        append("""<c r="$ref" t="s"><v>$idx</v></c>""")
                    }
                    append("</row>")
                }
                append("</sheetData></worksheet>")
            }
        return body.encodeToByteArray()
    }

    private fun cellRef(
        col: Int,
        row: Int,
    ): String = "${columnLetters(col)}$row"

    private fun columnLetters(col: Int): String {
        var n = col
        val chars = mutableListOf<Char>()
        do {
            chars.add(0, 'A' + (n % 26))
            n = n / 26 - 1
        } while (n >= 0)
        return chars.joinToString("")
    }

    private fun contentTypesXml(): ByteArray =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
          <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
          <Default Extension="xml" ContentType="application/xml"/>
          <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
          <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
          <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
          <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
        </Types>
        """.trimIndent().encodeToByteArray()

    private fun rootRelsXml(): ByteArray =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
        </Relationships>
        """.trimIndent().encodeToByteArray()

    private fun workbookRelsXml(): ByteArray =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
          <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
          <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
        </Relationships>
        """.trimIndent().encodeToByteArray()

    private fun workbookXml(sheetName: String): ByteArray {
        val safeName = escapeXml(sheetName)
        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
              xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
              <fileVersion appName="xl" lastEdited="7" lowestEdited="7" rupBuild="9303"/>
              <workbookPr defaultThemeVersion="166925"/>
              <sheets>
                <sheet name="$safeName" sheetId="1" r:id="rId1"/>
              </sheets>
            </workbook>
            """.trimIndent().encodeToByteArray()
    }

    private fun stylesXml(): ByteArray =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
          <fonts count="1"><font><sz val="11"/><color theme="1"/><name val="Calibri"/><family val="2"/></font></fonts>
          <fills count="2">
            <fill><patternFill patternType="none"/></fill>
            <fill><patternFill patternType="gray125"/></fill>
          </fills>
          <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
          <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
          <cellXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs>
          <cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>
        </styleSheet>
        """.trimIndent().encodeToByteArray()

    private fun escapeXml(text: String): String =
        buildString(text.length) {
            for (ch in text) {
                when (ch) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\n' -> append("&#10;")
                    '\r' -> Unit
                    else -> append(ch)
                }
            }
        }
}

object StringsMatrixExporter {
    fun encodeXlsx(matrix: StringsMatrix): ByteArray = MinimalXlsxEncoder.encode(matrix)
}
