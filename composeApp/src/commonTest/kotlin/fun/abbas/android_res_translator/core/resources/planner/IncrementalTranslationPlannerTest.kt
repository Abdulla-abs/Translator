package `fun`.abbas.android_res_translator.core.resources.planner

import `fun`.abbas.android_res_translator.core.resources.model.StringEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.EntryStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class IncrementalTranslationPlannerTest {
    @Test
    fun plan_countsTranslateAndSkip() {
        val source =
            StringResourceFile(
                strings =
                    mapOf(
                        "a" to StringEntry("a", "A", true),
                        "b" to StringEntry("b", "B", true),
                        "c" to StringEntry("c", "C", true),
                    ),
            )
        val target =
            StringResourceFile(
                strings =
                    mapOf(
                        "a" to StringEntry("a", "已有", true),
                        "b" to StringEntry("b", "", true),
                    ),
            )
        val planned = IncrementalTranslationPlanner.plan(source, target)
        assertEquals(1, planned.count { it.action == PlannedEntryAction.SKIP })
        assertEquals(2, planned.count { it.action == PlannedEntryAction.TRANSLATE })
    }

    @Test
    fun toXmlEntryUiList_marksSkipped() {
        val source = StringResourceFile(strings = mapOf("k" to StringEntry("k", "Hi", true)))
        val target = StringResourceFile(strings = mapOf("k" to StringEntry("k", "Old", true)))
        val ui = IncrementalTranslationPlanner.plan(source, target).toXmlEntryUiList()
        assertEquals(EntryStatus.Skipped, ui.single().status)
        assertEquals("Old", ui.single().targetText)
    }
}
