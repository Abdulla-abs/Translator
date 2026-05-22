package `fun`.abbas.android_res_translator.ui.screens.fileeditor

import `fun`.abbas.android_res_translator.core.ports.FakeSecretsProvider
import `fun`.abbas.android_res_translator.core.resources.planner.TranslationWorkflowMode
import `fun`.abbas.android_res_translator.ui.TranslationServices
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileEditorControllerTest {
    private val sourceXml =
        """
        <?xml version="1.0" encoding="utf-8"?>
        <resources>
            <string name="app_name">Hello</string>
            <string name="greeting">Hi</string>
        </resources>
        """.trimIndent()

    @Test
    fun load_parsesStringEntries() = runTest {
        val controller =
            FileEditorController(
                services = TranslationServices(FakeSecretsProvider(emptyMap())),
                scope = this,
                fileName = "strings.xml",
                filePath = "/res/",
                sourceLang = "en",
                targetLang = "zh",
                sourceXml = sourceXml,
            )
        controller.load(sourceXml)
        assertTrue(controller.state.value.entries.any { it.key == "app_name" && it.sourceText == "Hello" })
    }

    @Test
    fun fullMode_withoutTarget_canTranslateFromSource() = runTest {
        val controller =
            FileEditorController(
                services = TranslationServices(FakeSecretsProvider(emptyMap())),
                scope = this,
                fileName = "strings.xml",
                filePath = "/res/",
                sourceLang = "en",
                targetLang = "zh",
                sourceXml = sourceXml,
                workflowMode = TranslationWorkflowMode.FULL,
                targetBaselineXml = null,
            )
        assertTrue(controller.state.value.entries.isNotEmpty())
        assertTrue(controller.canStartTranslation())
        assertFalse(controller.canExport())
    }

    @Test
    fun incremental_marksSkipped() = runTest {
        val targetXml =
            """
            <resources>
                <string name="app_name">已有</string>
            </resources>
            """.trimIndent()
        val controller =
            FileEditorController(
                services = TranslationServices(FakeSecretsProvider(emptyMap())),
                scope = this,
                fileName = "strings.xml",
                filePath = "/res/",
                sourceLang = "en",
                targetLang = "zh",
                sourceXml = sourceXml,
                workflowMode = TranslationWorkflowMode.INCREMENTAL,
                targetBaselineXml = targetXml,
            )
        assertEquals(1, controller.state.value.skippedCount)
        assertEquals(1, controller.state.value.pendingCount)
        assertTrue(controller.canStartTranslation())
    }

    @Test
    fun exportXml_usesTranslatedTarget() = runTest {
        val controller =
            FileEditorController(
                services = TranslationServices(FakeSecretsProvider(emptyMap())),
                scope = this,
                fileName = "strings.xml",
                filePath = "/res/",
                sourceLang = "en",
                targetLang = "zh",
                sourceXml =
                    """
                    <resources>
                        <string name="app_name">Hello</string>
                    </resources>
                    """.trimIndent(),
            )
        controller.load(
            """
            <resources>
                <string name="app_name">Hello</string>
            </resources>
            """.trimIndent(),
        )
        val exported = controller.exportXml()
        assertTrue(exported.contains("app_name"))
    }

    @Test
    fun restoreSession_keepsCompletedEntriesWithoutAutoStart() = runTest {
        val controller =
            FileEditorController(
                services = TranslationServices(FakeSecretsProvider(emptyMap())),
                scope = this,
                fileName = "strings.xml",
                filePath = "/res/",
                sourceLang = "en",
                targetLang = "zh",
                sourceXml =
                    """
                    <resources>
                        <string name="a">One</string>
                        <string name="b">Two</string>
                    </resources>
                    """.trimIndent(),
                initialSession =
                    FileEditorSessionSnapshot(
                        entries =
                            listOf(
                                XmlEntryUi("a", "One", "一", EntryStatus.Completed),
                                XmlEntryUi("b", "Two", null, EntryStatus.Pending),
                            ),
                    ),
            )
        assertEquals(1, controller.state.value.completedCount)
        assertEquals(false, controller.state.value.isRunning)
    }
}
