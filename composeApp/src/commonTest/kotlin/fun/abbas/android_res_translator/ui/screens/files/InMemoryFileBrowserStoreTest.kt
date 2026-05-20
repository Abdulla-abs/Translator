package `fun`.abbas.android_res_translator.ui.screens.files

import `fun`.abbas.android_res_translator.core.ports.InMemoryFileTree
import `fun`.abbas.android_res_translator.ui.screens.main.InMemoryRecentXmlProjectRepository
import `fun`.abbas.android_res_translator.ui.screens.main.RecentXmlProject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FileBrowserStoreTest {

    private fun makeFileTree() = InMemoryFileTree(
        initialDirs = listOf(
            "src",
            "src/commonMain",
            "src/commonMain/resources",
            "src/commonMain/resources/layout",
        ),
        initialFiles = mapOf(
            "src/commonMain/resources/strings_main.xml" to "<resources><string name=\"app_name\">KMP Translator</string></resources>",
            "src/commonMain/resources/errors_en.xml" to "<resources><string name=\"error_network\">Network error</string></resources>",
            "src/commonMain/resources/auth_screens.xml" to "<resources><string name=\"login_title\">Sign in</string></resources>",
        )
    )

    private fun TestScope.makeStore(recent: InMemoryRecentXmlProjectRepository = InMemoryRecentXmlProjectRepository()): FileBrowserStore =
        FileBrowserStore(makeFileTree(), recent, this)

    private fun TestScope.navigateToResources(store: FileBrowserStore) {
        store.enterFolder(FileBrowserItem.Folder("src", "src"))
        advanceUntilIdle()
        store.enterFolder(FileBrowserItem.Folder("commonMain", "src/commonMain"))
        advanceUntilIdle()
        store.enterFolder(FileBrowserItem.Folder("resources", "src/commonMain/resources"))
        advanceUntilIdle()
    }

    @Test
    fun navigateHome_resetsToRoot() = runTest(UnconfinedTestDispatcher()) {
        val store = makeStore()
        advanceUntilIdle()
        store.enterFolder(FileBrowserItem.Folder("src", "src"))
        advanceUntilIdle()
        store.navigateHome()
        advanceUntilIdle()
        assertEquals(emptyList(), store.state.value.pathSegments)
        assertTrue(store.state.value.items.any { it is FileBrowserItem.Folder && it.name == "src" })
    }

    @Test
    fun enterFolder_reachesResources() = runTest(UnconfinedTestDispatcher()) {
        val store = makeStore()
        advanceUntilIdle()
        navigateToResources(store)
        assertEquals(listOf("src", "commonMain", "resources"), store.state.value.pathSegments)
        assertTrue(store.state.value.items.any { it is FileBrowserItem.XmlFile && it.name == "strings_main.xml" })
    }

    @Test
    fun searchQuery_filtersByName() = runTest(UnconfinedTestDispatcher()) {
        val store = makeStore()
        advanceUntilIdle()
        navigateToResources(store)
        store.setSearchQuery("strings_main")
        advanceUntilIdle()
        val names = store.state.value.items.map {
            when (it) {
                is FileBrowserItem.Folder -> it.name
                is FileBrowserItem.XmlFile -> it.name
            }
        }
        assertEquals(listOf("strings_main.xml"), names)
    }

    @Test
    fun recentProject_appearsInResources() = runTest(UnconfinedTestDispatcher()) {
        val recent = InMemoryRecentXmlProjectRepository()
        val xml = "<resources><string name=\"a\">A</string></resources>"
        recent.addOrUpdateWithSource(
            RecentXmlProject(
                id = "uploaded1",
                displayName = "custom.xml",
                modifiedAtEpochMs = 0L,
                progressPercent = 0f,
                translatedKeys = 0,
                totalKeys = 3,
                isComplete = false,
            ),
            sourceXml = xml,
        )
        val store = makeStore(recent)
        advanceUntilIdle()
        navigateToResources(store)
        assertTrue(store.state.value.items.any { it is FileBrowserItem.XmlFile && it.name == "custom.xml" })
        val content = store.loadXmlContent("uploaded1")
        assertEquals(xml, content)
    }
}
