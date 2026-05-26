package `fun`.abbas.android_res_translator.persistence

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResProjectVersionStoreJvmTest {
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
    fun push_restore_roundTrip() {
        val source = setupProjectWithWorkspace("v1")
        val project = ResMultiProjectFileStore.createProject("VersionRt")
        ResMultiProjectFileStore.initializeFromSource(project.id, source.absolutePath)

        File(ResMultiProjectFileStore.workspacePath(project.id), "values/strings.xml").writeText(
            """<resources><string name="k">changed</string></resources>""",
        )
        markDirtyForTest(project.id)

        val pushed = ResProjectVersionStore.pushVersion(project.id, "save1").getOrThrow()
        assertEquals(2, pushed.versions.size)
        assertFalse(ResProjectVersionStore.isDirty(project.id))

        File(ResMultiProjectFileStore.workspacePath(project.id), "values/strings.xml").writeText(
            """<resources><string name="k">newer</string></resources>""",
        )

        ResProjectVersionStore.restoreToVersion(project.id, "v0001-init").getOrThrow()
        val xml = File(ResMultiProjectFileStore.workspacePath(project.id), "values/strings.xml").readText()
        assertTrue(xml.contains(">v1<"))
        assertFalse(xml.contains(">newer<"))
        assertFalse(ResProjectVersionStore.isDirty(project.id))
        assertEquals(
            "v0001-init",
            ResProjectVersionStore.readVersionIndex(project.id).headId,
        )
    }

    @Test
    fun restoreToVersion_updatesHeadWhenNewerSnapshotsExist() {
        val source = setupProjectWithWorkspace("a")
        val project = ResMultiProjectFileStore.createProject("HeadAfterRestore")
        ResMultiProjectFileStore.initializeFromSource(project.id, source.absolutePath)
        markDirtyForTest(project.id)
        ResProjectVersionStore.pushVersion(project.id, "b").getOrThrow()
        markDirtyForTest(project.id)
        val latest = ResProjectVersionStore.pushVersion(project.id, "c").getOrThrow()
        assertEquals("v0003-c", latest.headId)

        ResProjectVersionStore.restoreToVersion(project.id, "v0002-b").getOrThrow()
        assertEquals("v0002-b", ResProjectVersionStore.readVersionIndex(project.id).headId)
        assertEquals(3, ResProjectVersionStore.readVersionIndex(project.id).versions.size)
    }

    @Test
    fun deleteVersionAndSuccessors_removesDirectoriesOnDisk() {
        val source = setupProjectWithWorkspace("base")
        val project = ResMultiProjectFileStore.createProject("Cascade")
        ResMultiProjectFileStore.initializeFromSource(project.id, source.absolutePath)

        ResProjectVersionStore.pushVersion(project.id, "v2").getOrThrow()
        ResProjectVersionStore.pushVersion(project.id, "v3").getOrThrow()

        val updated = ResProjectVersionStore.deleteVersionAndSuccessors(project.id, "v0002-v2").getOrThrow()
        assertEquals(1, updated.versions.size)
        assertEquals("v0001-init", updated.versions.single().id)
        assertFalse(
            fileExists(ResMultiProjectFileStore.versionDirectory(project.id, "v0002-v2")),
        )
        assertFalse(
            fileExists(ResMultiProjectFileStore.versionDirectory(project.id, "v0003-v3")),
        )
    }

    private fun setupProjectWithWorkspace(value: String): File {
        val source = File(tempRoot, "res-${value}").apply { mkdirs() }
        File(source, "values").mkdirs()
        File(source, "values/strings.xml").writeText(
            """<resources><string name="k">$value</string></resources>""",
        )
        return source
    }
}

private fun markDirtyForTest(projectId: String) {
    val meta = ResMultiProjectFileStore.readMeta(projectId) ?: return
    ResMultiProjectFileStore.writeMeta(meta.copy(dirty = true))
    ResProjectVersionStore.writeVersionIndex(
        projectId,
        ResProjectVersionStore.readVersionIndex(projectId).copy(dirty = true),
    )
}
