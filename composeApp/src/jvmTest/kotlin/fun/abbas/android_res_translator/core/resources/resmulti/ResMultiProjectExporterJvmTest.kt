package `fun`.abbas.android_res_translator.core.resources.resmulti

import `fun`.abbas.android_res_translator.persistence.ResMultiProjectFileStore
import `fun`.abbas.android_res_translator.persistence.resMultiProjectsRootOverride
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiInitState
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiLanguageEntry
import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiProject
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResMultiProjectExporterJvmTest {
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
    fun exportFull_writesValidXlsxZip() {
        val project = createProjectWithWorkspace()
        val export = ResMultiProjectExporter.exportFull(project).getOrThrow()
        assertTrue(export.suggestedFileName.endsWith("_all.xlsx"))
        assertEquals(0x50, export.bytes[0].toInt())
        assertEquals(0x4b, export.bytes[1].toInt())
    }

    @Test
    fun exportSingle_producesXlsxForSelectedLanguage() {
        val project = createProjectWithWorkspace()
        val lang = project.languages.first { it.langCode == "zh" }
        val export = ResMultiProjectExporter.exportSingle(project, lang).getOrThrow()
        assertTrue(export.suggestedFileName.contains("_zh.xlsx"))
        assertTrue(export.bytes.size > 100)
        assertEquals(0x50, export.bytes[0].toInt())
    }

    private fun createProjectWithWorkspace(): ResMultiProject {
        val created = ResMultiProjectFileStore.createProject("ExportTest")
        val workspace = File(ResMultiProjectFileStore.workspacePath(created.id))
        File(workspace, "values").mkdirs()
        File(workspace, "values/strings.xml").writeText(
            """<resources><string name="appName">My App</string></resources>""",
        )
        File(workspace, "values-zh").mkdirs()
        File(workspace, "values-zh/strings.xml").writeText(
            """<resources><string name="appName">我的程序</string></resources>""",
        )
        return created.copy(
            initState = ResMultiInitState.READY,
            languages =
                listOf(
                    ResMultiLanguageEntry("values", "en", "values/strings.xml"),
                    ResMultiLanguageEntry("values-zh", "zh", "values-zh/strings.xml"),
                ),
        )
    }

}
