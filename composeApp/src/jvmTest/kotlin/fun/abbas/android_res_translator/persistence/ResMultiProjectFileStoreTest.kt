package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.ui.screens.resmulti.ResMultiInitState
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResMultiProjectFileStoreTest {
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
    fun initializeFromSource_clonesAndScansJvmDirectory() {
        val source = File(tempRoot, "sample-res").apply { mkdirs() }
        File(source, "values").mkdirs()
        File(source, "values/strings.xml").writeText(
            """
            <resources><string name="k">v</string></resources>
            """.trimIndent(),
        )
        File(source, "values-en").mkdirs()
        File(source, "values-en/strings.xml").writeText(
            """
            <resources><string name="k">v en</string></resources>
            """.trimIndent(),
        )

        val project = ResMultiProjectFileStore.createProject("MyRes")
        val ready = ResMultiProjectFileStore.initializeFromSource(project.id, source.absolutePath)

        assertEquals(ResMultiInitState.READY, ready.initState)
        assertEquals(2, ready.languages.size)
        assertTrue(fileExists(ResMultiProjectFileStore.workspacePath(project.id) + "/values/strings.xml"))
        assertTrue(fileExists(ResMultiProjectFileStore.versionDirectory(project.id, "v0001-init") + "/values/strings.xml"))
        val versionIndex = ResProjectVersionStore.readVersionIndex(project.id)
        assertEquals("v0001-init", versionIndex.headId)
    }
}
