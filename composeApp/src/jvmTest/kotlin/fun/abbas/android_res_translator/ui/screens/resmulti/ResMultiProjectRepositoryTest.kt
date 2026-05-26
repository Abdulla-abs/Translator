package `fun`.abbas.android_res_translator.ui.screens.resmulti

import `fun`.abbas.android_res_translator.persistence.ResMultiProjectFileStore
import `fun`.abbas.android_res_translator.persistence.resMultiProjectsRootOverride
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class ResMultiProjectRepositoryTest {
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
    fun reloadFromDisk_restoresProjectMeta() =
        runTest {
            val source = File(tempRoot, "sample-res").apply { mkdirs() }
            File(source, "values").mkdirs()
            File(source, "values/strings.xml").writeText(
                """<resources><string name="k">v</string></resources>""",
            )
            val repo = PersistentResMultiProjectRepository(scope = this)
            val created = repo.createProject("PersistTest")
            repo.initializeFromSource(created.id, source.absolutePath)

            val reloaded = PersistentResMultiProjectRepository(scope = this)
            reloaded.reloadFromDisk()
            val fromDisk = reloaded.readProject(created.id)

            assertEquals(ResMultiInitState.READY, fromDisk?.initState)
            assertEquals(1, fromDisk?.languages?.size)
            assertEquals("en", fromDisk?.languages?.first()?.langCode)
            assertEquals(
                created.id,
                ResMultiProjectFileStore.readMeta(created.id)?.id,
            )
        }
}
