package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.ui.screens.fileeditor.EntryStatus
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranslationProjectFileStoreTest {
    private lateinit var tempRoot: String

    @BeforeTest
    fun setUp() {
        tempRoot = createTempDirectory().toFile().absolutePath
        translationProjectsRootOverride = tempRoot
    }

    @AfterTest
    fun tearDown() {
        translationProjectsRootOverride = null
    }

    @Test
    fun createProject_writesSourceAndResultTemplate() {
        val project =
            TranslationProjectFileStore.createProjectFromUpload(
                sourceXml = SAMPLE_XML,
                displayName = "strings.xml",
                sourceLang = "en",
                targetLang = "zh",
            )
        assertTrue(fileExists(project.sourcePath))
        assertTrue(fileExists(project.resultPath))
        val session = TranslationProjectFileStore.loadSessionSnapshot(project)
        assertEquals(1, session?.entries?.count { it.status is EntryStatus.Pending })
    }

    @Test
    fun buildEntries_restoresCompletedFromResultFile() {
        val project =
            TranslationProjectFileStore.createProjectFromUpload(
                sourceXml = SAMPLE_XML,
                displayName = "strings.xml",
                sourceLang = "en",
                targetLang = "zh",
            )
        val translated =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="app_name">你好</string>
            </resources>
            """.trimIndent()
        writeTextFileAtomic(project.resultPath, translated)
        val entries =
            TranslationProjectFileStore.buildEntriesFromXml(
                readTextFile(project.sourcePath),
                translated,
            )
        val appName = entries.single { it.key == "app_name" }
        assertEquals(EntryStatus.Completed, appName.status)
        assertEquals("你好", appName.targetText)
    }

    companion object {
        private val SAMPLE_XML =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="app_name">Hello</string>
            </resources>
            """.trimIndent()
    }
}
