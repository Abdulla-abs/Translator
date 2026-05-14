package `fun`.abbas.android_res_translator.core.resources.consumer

import `fun`.abbas.android_res_translator.core.resources.model.StringArrayEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringEntry
import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import `fun`.abbas.android_res_translator.core.translation.TranslationOutcome
import `fun`.abbas.android_res_translator.core.translation.TranslationSuccess
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FilledTranslationConsumerTest {

    private val port =
        TranslationSegmentPort { text, _, _ ->
            TranslationOutcome.Ok(
                TranslationSuccess(
                    translatedText = "[$text]",
                    resolvedSourceLanguage = "en",
                    resolvedTargetLanguage = "zh",
                ),
            )
        }

    @Test
    fun filled_addsMissingString() = runTest {
        val source =
            StringResourceFile(
                strings = mapOf("k" to StringEntry("k", "Hello", translatable = true)),
            )
        val target = StringResourceFile()
        val out = FilledTranslationConsumer().accept(source, "en", target, "zh", port)
        assertEquals("[Hello]", out.entries["k"])
    }

    @Test
    fun filled_skipsExistingString() = runTest {
        val source =
            StringResourceFile(
                strings = mapOf("k" to StringEntry("k", "Hello", translatable = true)),
            )
        val target =
            StringResourceFile(
                strings = mapOf("k" to StringEntry("k", "Old", translatable = true)),
            )
        val out = FilledTranslationConsumer().accept(source, "en", target, "zh", port)
        assertEquals("Old", out.entries["k"])
    }

    @Test
    fun filled_mustTranslateNames_overwritesExistingString() = runTest {
        val source =
            StringResourceFile(
                strings = mapOf("k" to StringEntry("k", "Hello", translatable = true)),
            )
        val target =
            StringResourceFile(
                strings = mapOf("k" to StringEntry("k", "Old", translatable = true)),
            )
        val out =
            FilledTranslationConsumer(mustTranslateNames = setOf("k"))
                .accept(source, "en", target, "zh", port)
        assertEquals("[Hello]", out.entries["k"])
    }

    @Test
    fun filled_skipsStringArrayWhenTargetHasArray() = runTest {
        val source =
            StringResourceFile(
                stringArrays =
                    mapOf(
                        "tabs" to StringArrayEntry("tabs", listOf("A", "B")),
                    ),
            )
        val target =
            StringResourceFile(
                stringArrays =
                    mapOf(
                        "tabs" to StringArrayEntry("tabs", listOf("keep")),
                    ),
            )
        val out = FilledTranslationConsumer().accept(source, "en", target, "zh", port)
        assertEquals(listOf("keep"), out.stringArrays["tabs"]?.items)
    }

    @Test
    fun filled_createsMissingStringArray() = runTest {
        val source =
            StringResourceFile(
                stringArrays =
                    mapOf(
                        "tabs" to StringArrayEntry("tabs", listOf("A", "B")),
                    ),
            )
        val target = StringResourceFile()
        val out = FilledTranslationConsumer().accept(source, "en", target, "zh", port)
        assertEquals(listOf("[A]", "[B]"), out.stringArrays["tabs"]?.items)
    }

    @Test
    fun allReplace_overwritesStringAndRebuildsArray() = runTest {
        val source =
            StringResourceFile(
                strings = mapOf("k" to StringEntry("k", "Hi", translatable = true)),
                stringArrays =
                    mapOf(
                        "tabs" to StringArrayEntry("tabs", listOf("a", "b")),
                    ),
            )
        val target =
            StringResourceFile(
                strings = mapOf("k" to StringEntry("k", "Old", translatable = true)),
                stringArrays =
                    mapOf(
                        "tabs" to StringArrayEntry("tabs", listOf("x", "y", "z")),
                    ),
            )
        val out = AllReplaceTranslationConsumer().accept(source, "en", target, "zh", port)
        assertEquals("[Hi]", out.entries["k"])
        assertEquals(listOf("[a]", "[b]"), out.stringArrays["tabs"]?.items)
    }

    @Test
    fun skipsTranslatableFalseUnlessForce() = runTest {
        val source =
            StringResourceFile(
                strings =
                    mapOf(
                        "x" to StringEntry("x", "No", translatable = false),
                    ),
            )
        val target = StringResourceFile()
        val out = FilledTranslationConsumer().accept(source, "en", target, "zh", port)
        assertNull(out.strings["x"])
        val forced =
            FilledTranslationConsumer(forceTranslation = true)
                .accept(source, "en", target, "zh", port)
        assertEquals("[No]", forced.entries["x"])
    }
}
