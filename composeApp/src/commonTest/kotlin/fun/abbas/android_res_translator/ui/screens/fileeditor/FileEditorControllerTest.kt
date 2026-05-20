package `fun`.abbas.android_res_translator.ui.screens.fileeditor

import `fun`.abbas.android_res_translator.ui.TranslationServices
import `fun`.abbas.android_res_translator.core.ports.FakeSecretsProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileEditorControllerTest {
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
        sourceXml =
          """
          <?xml version="1.0" encoding="utf-8"?>
          <resources>
              <string name="app_name">Hello</string>
          </resources>
          """.trimIndent(),
      )
    assertTrue(controller.state.value.entries.any { it.key == "app_name" && it.sourceText == "Hello" })
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
