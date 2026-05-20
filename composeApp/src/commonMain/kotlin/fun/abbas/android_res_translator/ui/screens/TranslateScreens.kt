package `fun`.abbas.android_res_translator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import `fun`.abbas.android_res_translator.core.resources.consumer.AllReplaceTranslationConsumer
import `fun`.abbas.android_res_translator.core.resources.consumer.FilledTranslationConsumer
import `fun`.abbas.android_res_translator.core.resources.usecase.TranslateStringsXmlUseCase
import `fun`.abbas.android_res_translator.ui.TranslationServices
import `fun`.abbas.android_res_translator.ui.XmlFileAccess
import `fun`.abbas.android_res_translator.ui.components.AppOutlinedField
import `fun`.abbas.android_res_translator.ui.components.AppPrimaryButton
import `fun`.abbas.android_res_translator.ui.components.AppSectionCard
import `fun`.abbas.android_res_translator.ui.components.AppThinProgress
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsSnapshot
import `fun`.abbas.android_res_translator.ui.settings.ConsumerMode
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import kotlinx.coroutines.launch
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_back
import androidrestranslator.composeapp.generated.resources.translate_file_detail
import androidrestranslator.composeapp.generated.resources.translate_result_preview
import androidrestranslator.composeapp.generated.resources.translate_source_lang
import androidrestranslator.composeapp.generated.resources.translate_source_xml_label
import androidrestranslator.composeapp.generated.resources.translate_target_lang
import androidrestranslator.composeapp.generated.resources.translate_target_xml_optional
import androidrestranslator.composeapp.generated.resources.translate_read_source_xml
import androidrestranslator.composeapp.generated.resources.translate_save_as_file
import androidrestranslator.composeapp.generated.resources.translate_to_merged_xml
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TranslateFileTab(
    services: TranslationServices,
    snapshot: AppSettingsSnapshot,
    xmlFileAccess: XmlFileAccess,
    modifier: Modifier = Modifier,
    initialSourceXml: String? = null,
    onBack: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    var sourceXml by remember(initialSourceXml) { mutableStateOf(initialSourceXml.orEmpty()) }
    var targetXml by remember { mutableStateOf("") }
    var from by remember { mutableStateOf(snapshot.defaultSourceLang) }
    var to by remember { mutableStateOf(snapshot.defaultTargetLang) }
    var output by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val consumer =
        remember(snapshot.consumerMode, snapshot.forceTranslation) {
            when (snapshot.consumerMode) {
                ConsumerMode.FILLED -> FilledTranslationConsumer(forceTranslation = snapshot.forceTranslation)
                ConsumerMode.ALL_REPLACE -> AllReplaceTranslationConsumer(forceTranslation = snapshot.forceTranslation)
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.gutter),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        onBack?.let { back ->
            Row {
                IconButton(onClick = back) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                }
                Text(stringResource(Res.string.translate_file_detail), style = MaterialTheme.typography.titleMedium)
            }
        }
        AppSectionCard {
            AppOutlinedField(from, { from = it }, stringResource(Res.string.translate_source_lang))
            Spacer(Modifier.height(AppSpacing.sm))
            AppOutlinedField(to, { to = it }, stringResource(Res.string.translate_target_lang))
            Spacer(Modifier.height(AppSpacing.md))
            AppPrimaryButton(
                onClick = { xmlFileAccess.launchPickXml { r: Result<String> -> r.onSuccess { sourceXml = it } } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.translate_read_source_xml))
            }
            Spacer(Modifier.height(AppSpacing.sm))
            AppOutlinedField(
                value = sourceXml,
                onValueChange = { sourceXml = it },
                label = stringResource(Res.string.translate_source_xml_label),
                minLines = 4,
                singleLine = false,
                useMonospace = true,
            )
            Spacer(Modifier.height(AppSpacing.sm))
            AppOutlinedField(
                value = targetXml,
                onValueChange = { targetXml = it },
                label = stringResource(Res.string.translate_target_xml_optional),
                minLines = 2,
                singleLine = false,
                useMonospace = true,
            )
            Spacer(Modifier.height(AppSpacing.md))
            AppPrimaryButton(
                onClick = {
                    error = null
                    loading = true
                    output = ""
                    scope.launch {
                        try {
                            val uc = TranslateStringsXmlUseCase(consumer)
                            val port = services.segmentPort()
                            output =
                                uc.invoke(
                                    sourceXml = sourceXml,
                                    sourceLang = from.trim(),
                                    targetXml = targetXml,
                                    targetLang = to.trim(),
                                    port = port,
                                )
                        } catch (e: Exception) {
                            error = e.message ?: e.toString()
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading && sourceXml.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.translate_to_merged_xml))
            }
            if (loading) {
                Spacer(Modifier.height(AppSpacing.sm))
                AppThinProgress(progress = null, inProgress = true)
            }
            error?.let {
                Spacer(Modifier.height(AppSpacing.sm))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }
        AppSectionCard {
            AppOutlinedField(
                value = output,
                onValueChange = {},
                label = stringResource(Res.string.translate_result_preview),
                minLines = 4,
                singleLine = false,
                readOnly = true,
                useMonospace = true,
            )
            if (output.isNotBlank() && !loading) {
                Spacer(Modifier.height(AppSpacing.sm))
                AppThinProgress(progress = 1f, inProgress = false)
            }
            Spacer(Modifier.height(AppSpacing.md))
            AppPrimaryButton(
                onClick = {
                    if (output.isNotBlank()) {
                        xmlFileAccess.launchSaveXml(output, "strings.xml") { }
                    }
                },
                enabled = output.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.translate_save_as_file))
            }
        }
    }
}
