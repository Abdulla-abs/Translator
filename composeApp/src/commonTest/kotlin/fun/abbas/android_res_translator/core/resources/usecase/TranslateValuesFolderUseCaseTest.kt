package `fun`.abbas.android_res_translator.core.resources.usecase

import `fun`.abbas.android_res_translator.core.ports.InMemoryFileTree
import `fun`.abbas.android_res_translator.core.resources.consumer.FilledTranslationConsumer
import `fun`.abbas.android_res_translator.core.resources.consumer.TranslationSegmentPort
import `fun`.abbas.android_res_translator.core.resources.xml.StringsXmlCodec
import `fun`.abbas.android_res_translator.core.translation.TranslationOutcome
import `fun`.abbas.android_res_translator.core.translation.TranslationSuccess
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TranslateValuesFolderUseCaseTest {

    @Test
    fun invokesPerNonSourceValuesFolder() = runTest {
        val sourceXml =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="greet">Hello</string>
            </resources>
            """.trimIndent()
        val zhXml =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources/>
            """.trimIndent()
        val tree =
            InMemoryFileTree(
                initialDirs = listOf("res/values", "res/values-zh"),
                initialFiles =
                    mapOf(
                        "res/values/strings.xml" to sourceXml,
                        "res/values-zh/strings.xml" to zhXml,
                    ),
            )
        val port =
            TranslationSegmentPort { text, _, _ ->
                TranslationOutcome.Ok(
                    TranslationSuccess(
                        translatedText = "[$text]",
                        resolvedSourceLanguage = "en",
                        resolvedTargetLanguage = "zh",
                    ),
                )
            }
        val uc = TranslateValuesFolderUseCase(tree, FilledTranslationConsumer())
        uc.invoke("res", DefaultValuesFolderCountryTransformer(), port)
        val out = tree.readUtf8("res/values-zh/strings.xml")
        val model = StringsXmlCodec.parse(out)
        assertEquals("[Hello]", model.entries["greet"])
    }
}
