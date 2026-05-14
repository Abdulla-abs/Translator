package `fun`.abbas.android_res_translator.core.resources.usecase

import `fun`.abbas.android_res_translator.core.resources.consumer.AllReplaceTranslationConsumer
import `fun`.abbas.android_res_translator.core.resources.consumer.FilledTranslationConsumer
import `fun`.abbas.android_res_translator.core.resources.consumer.TranslationSegmentPort
import `fun`.abbas.android_res_translator.core.translation.TranslationOutcome
import `fun`.abbas.android_res_translator.core.translation.TranslationSuccess
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class TranslateStringsXmlUseCaseTest {

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

    private val sourceXml =
        """
        <?xml version="1.0" encoding="utf-8"?>
        <resources>
            <string name="a">Hi</string>
        </resources>
        """.trimIndent()

    @Test
    fun filled_mergesIntoEmptyTarget() = runTest {
        val uc = TranslateStringsXmlUseCase(FilledTranslationConsumer())
        val out = uc.invoke(sourceXml, "en", "", "zh", port)
        assertTrue(out.contains("""<string name="a">[Hi]</string>"""))
    }

    @Test
    fun allReplace_overwritesExistingTarget() = runTest {
        val targetXml =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="a">Old</string>
            </resources>
            """.trimIndent()
        val uc = TranslateStringsXmlUseCase(AllReplaceTranslationConsumer())
        val out = uc.invoke(sourceXml, "en", targetXml, "zh", port)
        assertTrue(out.contains("""<string name="a">[Hi]</string>"""))
    }
}
