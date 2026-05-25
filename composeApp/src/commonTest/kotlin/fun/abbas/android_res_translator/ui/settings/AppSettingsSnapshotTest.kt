package `fun`.abbas.android_res_translator.ui.settings

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppSettingsSnapshotTest {
    @Test
    fun quickTranslateAuto_defaultsToTrue() {
        assertTrue(AppSettingsSnapshot().quickTranslateAuto)
    }

    @Test
    fun quickTranslateAuto_fromFlatMap_parsesExplicitValues() {
        assertTrue(
            AppSettingsSnapshot.fromFlatMap(
                mapOf(AppSettingsSnapshot.KEY_QUICK_TRANSLATE_AUTO to "true"),
            ).quickTranslateAuto,
        )
        assertFalse(
            AppSettingsSnapshot.fromFlatMap(
                mapOf(AppSettingsSnapshot.KEY_QUICK_TRANSLATE_AUTO to "false"),
            ).quickTranslateAuto,
        )
        assertTrue(AppSettingsSnapshot.fromFlatMap(emptyMap()).quickTranslateAuto)
    }

    @Test
    fun quickTranslateAuto_roundTripsInPersistenceMap() {
        val map = AppSettingsSnapshot(quickTranslateAuto = false).toPersistenceMap()
        assertFalse(
            AppSettingsSnapshot.fromFlatMap(map).quickTranslateAuto,
        )
    }
}
