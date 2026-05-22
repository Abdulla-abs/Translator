package `fun`.abbas.android_res_translator.core.resources.planner

import `fun`.abbas.android_res_translator.core.resources.consumer.IncrementalSlotPolicy
import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.EntryStatus
import `fun`.abbas.android_res_translator.ui.screens.fileeditor.XmlEntryUi

enum class PlannedEntryAction {
    TRANSLATE,
    SKIP,
    SKIP_NOT_TRANSLATABLE,
}

data class PlannedStringEntry(
    val key: String,
    val sourceText: String,
    val targetText: String?,
    val action: PlannedEntryAction,
    val translatable: Boolean,
)

object IncrementalTranslationPlanner {
    fun plan(
        source: StringResourceFile,
        target: StringResourceFile,
        forceTranslation: Boolean = false,
        mustTranslateNames: Set<String> = emptySet(),
    ): List<PlannedStringEntry> =
        source.strings.map { (key, src) ->
            val tgt = target.strings[key]
            val skipNotTranslatable = !forceTranslation && !src.translatable
            val action =
                when {
                    skipNotTranslatable -> PlannedEntryAction.SKIP_NOT_TRANSLATABLE
                    key in mustTranslateNames -> PlannedEntryAction.TRANSLATE
                    IncrementalSlotPolicy.needsTranslateString(tgt) -> PlannedEntryAction.TRANSLATE
                    else -> PlannedEntryAction.SKIP
                }
            PlannedStringEntry(
                key = key,
                sourceText = src.value,
                targetText = tgt?.value?.takeIf { it.isNotBlank() },
                action = action,
                translatable = src.translatable,
            )
        }

    fun planFullReplace(source: StringResourceFile, forceTranslation: Boolean = false): List<PlannedStringEntry> =
        source.strings.map { (key, src) ->
            val skipNotTranslatable = !forceTranslation && !src.translatable
            PlannedStringEntry(
                key = key,
                sourceText = src.value,
                targetText = null,
                action =
                    if (skipNotTranslatable) {
                        PlannedEntryAction.SKIP_NOT_TRANSLATABLE
                    } else {
                        PlannedEntryAction.TRANSLATE
                    },
                translatable = src.translatable,
            )
        }
}

fun List<PlannedStringEntry>.toXmlEntryUiList(): List<XmlEntryUi> =
    map { planned ->
        when (planned.action) {
            PlannedEntryAction.TRANSLATE ->
                XmlEntryUi(
                    key = planned.key,
                    sourceText = planned.sourceText,
                    targetText = null,
                    status = EntryStatus.Pending,
                    translatable = planned.translatable,
                )
            PlannedEntryAction.SKIP ->
                XmlEntryUi(
                    key = planned.key,
                    sourceText = planned.sourceText,
                    targetText = planned.targetText,
                    status = EntryStatus.Skipped,
                    translatable = planned.translatable,
                )
            PlannedEntryAction.SKIP_NOT_TRANSLATABLE ->
                XmlEntryUi(
                    key = planned.key,
                    sourceText = planned.sourceText,
                    targetText = planned.sourceText,
                    status = EntryStatus.Skipped,
                    translatable = false,
                )
        }
    }
