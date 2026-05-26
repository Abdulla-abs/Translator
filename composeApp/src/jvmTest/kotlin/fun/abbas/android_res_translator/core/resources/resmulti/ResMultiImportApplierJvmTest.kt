package `fun`.abbas.android_res_translator.core.resources.resmulti

import `fun`.abbas.android_res_translator.core.resources.export.StringsMatrix
import `fun`.abbas.android_res_translator.core.resources.export.StringsMatrixRow
import `fun`.abbas.android_res_translator.core.resources.xml.StringsXmlCodec
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

class ResMultiImportApplierJvmTest {
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
    fun applyDiffs_writesOnlyChangedKeysAndMarksDirty() {
        val project = createWorkspaceProject()
        val en = project.languages.first { it.langCode == "en" }
        val path =
            ResMultiProjectScanner.joinPath(
                ResMultiProjectFileStore.workspacePath(project.id),
                en.stringsRelativePath,
            )
        val before = File(path).readText()
        val imported =
            StringsMatrix(
                columnHeaders = listOf("key", "en"),
                rows =
                    listOf(
                        StringsMatrixRow("a", listOf("a", "changed")),
                        StringsMatrixRow("b", listOf("b", "keep")),
                    ),
            )
        val compare =
            ResMultiProjectImportService.buildCompareMatrix(project, imported).getOrThrow()
        val result = ResMultiImportApplier.applyDiffs(project.id, compare).getOrThrow()
        assertEquals(1, result.totalChangedCells)
        val after = File(path).readText()
        val reparsed = StringsXmlCodec.parse(after)
        assertEquals("changed", reparsed.strings["a"]?.value)
        assertEquals("keep", reparsed.strings["b"]?.value)
        assertTrue(before.contains("name=\"b\""))
        assertTrue(ResMultiProjectFileStore.readMeta(project.id)?.dirty == true)
    }

    private fun createWorkspaceProject(): ResMultiProject {
        val created = ResMultiProjectFileStore.createProject("ApplyTest")
        val workspace = File(ResMultiProjectFileStore.workspacePath(created.id))
        File(workspace, "values-en").mkdirs()
        File(workspace, "values-en/strings.xml").writeText(
            """<resources><string name="a">old</string><string name="b">keep</string></resources>""",
        )
        return created.copy(
            initState = ResMultiInitState.READY,
            languages = listOf(ResMultiLanguageEntry("values-en", "en", "values-en/strings.xml")),
        )
    }
}
