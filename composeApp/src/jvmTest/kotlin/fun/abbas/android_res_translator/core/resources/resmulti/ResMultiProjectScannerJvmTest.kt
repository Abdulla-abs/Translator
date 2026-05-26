package `fun`.abbas.android_res_translator.core.resources.resmulti

import `fun`.abbas.android_res_translator.persistence.resMultiProjectsRootOverride
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResMultiProjectScannerJvmTest {
    private lateinit var tempRoot: File

    @BeforeTest
    fun setUp() {
        tempRoot = createTempDirectory().toFile()
        resMultiProjectsRootOverride = tempRoot.absolutePath
    }

    @AfterTest
    fun tearDown() {
        resMultiProjectsRootOverride = null
        tempRoot.deleteRecursively()
    }

    @Test
    fun scanWorkspace_findsValuesFoldersWithStringsXml() {
        val workspace = File(tempRoot, "workspace").apply { mkdirs() }
        File(workspace, "values").mkdirs()
        File(workspace, "values/strings.xml").writeText(
            """
            <resources>
                <string name="app_name">Demo</string>
            </resources>
            """.trimIndent(),
        )
        File(workspace, "values-zh").mkdirs()
        File(workspace, "values-zh/strings.xml").writeText(
            """
            <resources>
                <string name="app_name">演示</string>
            </resources>
            """.trimIndent(),
        )
        File(workspace, "values-fr").mkdirs()
        // no strings.xml — should warn

        val result = ResMultiProjectScanner.scanWorkspace(workspace.absolutePath)
        assertEquals(2, result.languages.size)
        assertEquals(setOf("en", "zh"), result.languages.map { it.langCode }.toSet())
        assertTrue(result.warnings.any { it.contains("values-fr") })
    }
}
