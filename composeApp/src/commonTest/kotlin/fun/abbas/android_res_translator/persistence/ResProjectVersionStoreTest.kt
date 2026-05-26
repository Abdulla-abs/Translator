package `fun`.abbas.android_res_translator.persistence

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResProjectVersionStoreTest {
    @Test
    fun allocateVersionId_incrementsSequence() {
        val existing =
            listOf(
                ResMultiVersionIndexEntry("v0001-init", "init", 0L),
                ResMultiVersionIndexEntry("v0002-alpha", "alpha", 1L),
            )
        assertEquals("v0003-beta", ResProjectVersionStore.allocateVersionId(existing, "beta"))
    }

    @Test
    fun sanitizeVersionSlug_replacesUnsafeCharacters() {
        assertEquals("my-version", ResProjectVersionStore.sanitizeVersionSlug("my/version"))
    }

    @Test
    fun requiresCascadeDeleteCountdown_onlyForNonLatest() {
        val index =
            ResMultiVersionIndex(
                versions =
                    listOf(
                        ResMultiVersionIndexEntry("v0001-init", "init", 0),
                        ResMultiVersionIndexEntry("v0002-a", "a", 1),
                        ResMultiVersionIndexEntry("v0003-b", "b", 2),
                    ),
            )
        assertTrue(ResProjectVersionStore.requiresCascadeDeleteCountdown(index, "v0002-a"))
        assertFalse(ResProjectVersionStore.requiresCascadeDeleteCountdown(index, "v0003-b"))
    }

    @Test
    fun deleteVersionAndSuccessors_keepsPrefixBeforeTarget() {
        val versions =
            listOf(
                ResMultiVersionIndexEntry("v0001-init", "init", 0),
                ResMultiVersionIndexEntry("v0002-a", "a", 1),
                ResMultiVersionIndexEntry("v0003-b", "b", 2),
            )
        val position = versions.indexOfFirst { it.id == "v0002-a" }
        val remaining = versions.take(position)
        assertEquals(1, remaining.size)
        assertEquals("v0001-init", remaining.last().id)
    }
}
