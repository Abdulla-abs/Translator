package `fun`.abbas.android_res_translator.persistence

import `fun`.abbas.android_res_translator.ui.screens.fileeditor.EntryStatus
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.FileEditorState
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.XmlEntryUi
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EditorSessionPersistenceTest {
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
    fun writeSession_persistsForceTranslationPerProject() {
        val project =
            TranslationProjectFileStore.createProjectFromUpload(
                sourceXml = SAMPLE_XML,
                displayName = "strings.xml",
                sourceLang = "en",
                targetLang = "zh",
            )
        TranslationProjectFileStore.writeSessionFromEditorState(
            project.id,
            FileEditorState(
                entries =
                    listOf(
                        XmlEntryUi("app_name", "Hello", null, EntryStatus.Pending),
                    ),
                forceTranslation = true,
            ),
        )
        val loaded = TranslationProjectFileStore.loadSessionSnapshot(project)
        assertTrue(loaded?.forceTranslation == true)

        TranslationProjectFileStore.writeSessionFromEditorState(
            project.id,
            FileEditorState(
                entries =
                    listOf(
                        XmlEntryUi("app_name", "Hello", null, EntryStatus.Pending),
                    ),
                forceTranslation = false,
            ),
        )
        val reloaded = TranslationProjectFileStore.loadSessionSnapshot(project)
        assertFalse(reloaded?.forceTranslation == true)
    }

    @Test
    fun writeSession_persistsSourceAndTargetLangPerProject() {
        val project =
            TranslationProjectFileStore.createProjectFromUpload(
                sourceXml = SAMPLE_XML,
                displayName = "strings.xml",
                sourceLang = "en",
                targetLang = "zh",
            )
        TranslationProjectFileStore.writeSessionFromEditorState(
            project.id,
            FileEditorState(
                entries =
                    listOf(
                        XmlEntryUi("app_name", "Hello", null, EntryStatus.Pending),
                    ),
                sourceLang = "ja",
                targetLang = "ko",
            ),
        )
        val loaded = TranslationProjectFileStore.loadSessionSnapshot(project)
        kotlin.test.assertEquals("ja", loaded?.sourceLang)
        kotlin.test.assertEquals("ko", loaded?.targetLang)
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
