package `fun`.abbas.android_res_translator.ui.screens.main

import kotlin.test.Test
import kotlin.test.assertEquals

class RecentXmlProjectRepositoryTest {
    @Test
    fun addOrUpdate_keepsMaxTen() {
        val repo = InMemoryRecentXmlProjectRepository(maxItems = 10)
        repeat(12) { i ->
            repo.addOrUpdate(sampleProject("id$i", "file$i.xml"))
        }
        assertEquals(10, repo.projects.value.size)
        assertEquals("id11", repo.projects.value.first().id)
    }

    @Test
    fun addOrUpdate_updatesExistingById() {
        val repo = InMemoryRecentXmlProjectRepository()
        repo.addOrUpdate(sampleProject("a", "first.xml", progress = 0.2f))
        repo.addOrUpdate(sampleProject("a", "first.xml", progress = 0.8f))
        assertEquals(1, repo.projects.value.size)
        assertEquals(0.8f, repo.projects.value.single().progressPercent)
    }

    private fun sampleProject(
        id: String,
        name: String,
        progress: Float = 0f,
    ) = RecentXmlProject(
        id = id,
        displayName = name,
        modifiedAtEpochMs = 0L,
        progressPercent = progress,
        translatedKeys = (progress * 10).toInt(),
        totalKeys = 10,
        isComplete = progress >= 1f,
    )
}
