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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text("文件详情", style = MaterialTheme.typography.titleMedium)
            }
        }
        AppSectionCard {
            AppOutlinedField(from, { from = it }, "源语言")
            Spacer(Modifier.height(AppSpacing.sm))
            AppOutlinedField(to, { to = it }, "目标语言")
            Spacer(Modifier.height(AppSpacing.md))
            AppPrimaryButton(
                onClick = { xmlFileAccess.launchPickXml { r: Result<String> -> r.onSuccess { sourceXml = it } } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("从文件读取源 XML")
            }
            Spacer(Modifier.height(AppSpacing.sm))
            AppOutlinedField(
                value = sourceXml,
                onValueChange = { sourceXml = it },
                label = "源 strings.xml",
                minLines = 4,
                singleLine = false,
                useMonospace = true,
            )
            Spacer(Modifier.height(AppSpacing.sm))
            AppOutlinedField(
                value = targetXml,
                onValueChange = { targetXml = it },
                label = "已有目标 XML（可留空）",
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
                Text("翻译为合并后的 XML")
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
                label = "结果预览",
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
                Text("保存为文件…")
            }
        }
    }
}
