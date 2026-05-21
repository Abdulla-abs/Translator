package `fun`.abbas.android_res_translator.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.core.translation.TranslationOutcome
import `fun`.abbas.android_res_translator.ui.TranslationServices
import `fun`.abbas.android_res_translator.ui.components.AppGlassCard
import `fun`.abbas.android_res_translator.ui.components.AppLanguageChip
import `fun`.abbas.android_res_translator.ui.components.AppThinProgress
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsRepository
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import `fun`.abbas.android_res_translator.ui.theme.appCodeTextStyle
import `fun`.abbas.android_res_translator.ui.toUserMessage
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.language_picker_source_title
import androidrestranslator.composeapp.generated.resources.language_picker_target_title
import androidrestranslator.composeapp.generated.resources.quick_translate_button
import androidrestranslator.composeapp.generated.resources.quick_translate_copy
import androidrestranslator.composeapp.generated.resources.quick_translate_copy_content_description
import androidrestranslator.composeapp.generated.resources.quick_translate_copied_snackbar
import androidrestranslator.composeapp.generated.resources.quick_translate_placeholder
import androidrestranslator.composeapp.generated.resources.quick_translate_title
import `fun`.abbas.android_res_translator.ui.platform.rememberCopyToClipboardHandler
import org.jetbrains.compose.resources.stringResource
import `fun`.abbas.android_res_translator.ui.translation.ActiveTranslationEngine
import `fun`.abbas.android_res_translator.ui.translation.LanguagePickerCatalog
import `fun`.abbas.android_res_translator.ui.translation.TranslationEngineCatalog
import kotlinx.coroutines.launch

@Composable
fun QuickTranslateSection(
    settings: AppSettingsRepository,
    services: TranslationServices,
    defaultFrom: String,
    defaultTo: String,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val snap by settings.snapshot.collectAsState()
    val selectedEngine = remember(snap) { LanguagePickerCatalog.resolveSelectedEngine(snap) }
    val engineOptions = remember(snap) { TranslationEngineCatalog.engineOptions(snap) }
    var from by remember(defaultFrom) { mutableStateOf(defaultFrom) }
    var to by remember(defaultTo) { mutableStateOf(defaultTo) }
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var editingLang by remember { mutableStateOf<LangEdit?>(null) }
    var showEnginePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val copyToClipboard = rememberCopyToClipboardHandler()
    val copiedSnackbarMessage = stringResource(Res.string.quick_translate_copied_snackbar)

    fun preferredVendorName(): String? = selectedEngine?.vendorName

    fun copyResultWithSnackbar(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        copyToClipboard(trimmed)
        scope.launch {
            snackbarHostState.showSnackbar(copiedSnackbarMessage)
        }
    }

    fun performTranslate() {
        error = null
        loading = true
        scope.launch {
            try {
                when (
                    val o =
                        services.translatePlainText(
                            input.trim(),
                            from.trim(),
                            to.trim(),
                            preferredVendorName(),
                        )
                ) {
                    is TranslationOutcome.Ok -> {
                        output = o.value.translatedText
                        copyResultWithSnackbar(o.value.translatedText)
                    }
                    is TranslationOutcome.Err -> {
                        error = o.failure.toUserMessage(snap.uiLocale)
                        output = ""
                    }
                }
            } finally {
                loading = false
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(22.dp))
            Text(
                stringResource(Res.string.quick_translate_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = AppSpacing.sm),
            )
        }

        Box(Modifier.fillMaxWidth()) {
            AppGlassCard {
            Box(Modifier.fillMaxWidth()) {
                Box(
                    modifier =
                        Modifier
                            .size(128.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 32.dp, y = (-40).dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                )

                Column(Modifier.fillMaxWidth().padding(top = AppSpacing.xs)) {
                    QuickTranslateEngineRow(
                        selectedEngine = selectedEngine,
                        onEditEngine = { showEnginePicker = true },
                    )
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                    if (maxWidth >= 600.dp) {
                        Row(
                            modifier = Modifier.padding(top = AppSpacing.md),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                        ) {
                            QuickTranslateSourceColumn(
                                from = from,
                                input = input,
                                onEditLang = { editingLang = LangEdit.Source },
                                onInputChange = { input = it },
                                modifier = Modifier.weight(1f),
                            )
                            QuickTranslateTargetColumn(
                                to = to,
                                output = output,
                                loading = loading,
                                input = input,
                                onEditLang = { editingLang = LangEdit.Target },
                                onTranslate = ::performTranslate,
                                onCopy = { copyResultWithSnackbar(output) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(top = AppSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                        ) {
                            QuickTranslateSourceColumn(
                                from = from,
                                input = input,
                                onEditLang = { editingLang = LangEdit.Source },
                                onInputChange = { input = it },
                            )
                            QuickTranslateTargetColumn(
                                to = to,
                                output = output,
                                loading = loading,
                                input = input,
                                onEditLang = { editingLang = LangEdit.Target },
                                onTranslate = ::performTranslate,
                                onCopy = { copyResultWithSnackbar(output) },
                            )
                        }
                    }
                    }
                }
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
            SnackbarHost(
                hostState = snackbarHostState,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(AppSpacing.md),
            )
        }
    }

    if (showEnginePicker) {
        EnginePickerDialog(
            options = engineOptions,
            selected = selectedEngine,
            onDismiss = { showEnginePicker = false },
            onSelect = { engine ->
                scope.launch {
                    settings.replaceAll(
                        settings.snapshot.value.copy(preferredTranslationEngine = engine),
                    )
                }
            },
        )
    }

    editingLang?.let { which ->
        val isSource = which == LangEdit.Source
        val options =
            remember(selectedEngine, from, to, which) {
                when {
                    selectedEngine == null -> emptyList()
                    isSource -> LanguagePickerCatalog.sourceOptions(selectedEngine, to)
                    else -> LanguagePickerCatalog.targetOptions(selectedEngine, from)
                }
            }
        val engineTitle = selectedEngine?.displayName ?: "未配置"
        LanguagePickerDialog(
            title =
                stringResource(
                    if (isSource) Res.string.language_picker_source_title else Res.string.language_picker_target_title,
                ),
            engine = selectedEngine,
            options = options,
            selectedCode = if (isSource) from else to,
            onDismiss = { editingLang = null },
            onSelect = { code ->
                when (which) {
                    LangEdit.Source -> {
                        from = code
                        to = LanguagePickerCatalog.adjustTargetWhenSourceEqualsTarget(code, to)
                    }
                    LangEdit.Target -> to = code
                }
            },
        )
    }
}

@Composable
private fun QuickTranslateLangHeader(
    label: String,
    langCode: String,
    onEditLang: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = AppLabelCapsTextStyle, color = MaterialTheme.colorScheme.outline)
        AppLanguageChip(label = formatLanguageLabel(langCode), onClick = onEditLang)
    }
}

@Composable
private fun QuickTranslateSourceColumn(
    from: String,
    input: String,
    onEditLang: () -> Unit,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        QuickTranslateLangHeader(label = "SOURCE", langCode = from.ifBlank { "en" }, onEditLang = onEditLang)
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            placeholder = { Text(stringResource(Res.string.quick_translate_placeholder), style = appCodeTextStyle()) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
            minLines = 6,
            shape = RoundedCornerShape(12.dp),
            textStyle = appCodeTextStyle(),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceContainerLowest,
                    unfocusedContainerColor = colors.surfaceContainerLowest,
                    focusedBorderColor = colors.primaryContainer,
                    unfocusedBorderColor = colors.outlineVariant.copy(alpha = 0.5f),
                    cursorColor = colors.primary,
                ),
        )
    }
}

@Composable
private fun QuickTranslateTargetColumn(
    to: String,
    output: String,
    loading: Boolean,
    input: String,
    onEditLang: () -> Unit,
    onTranslate: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val canCopy = output.isNotBlank() && !loading
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        QuickTranslateLangHeader(label = "TARGET", langCode = to.ifBlank { "zh" }, onEditLang = onEditLang)
        SelectionContainer {
            OutlinedTextField(
                value = output,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        "Translation result will appear here...",
                        style = appCodeTextStyle(),
                        color = colors.onSurfaceVariant,
                    )
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                minLines = 6,
                shape = RoundedCornerShape(12.dp),
                textStyle = appCodeTextStyle(),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceContainerLow,
                        unfocusedContainerColor = colors.surfaceContainerLow,
                        disabledContainerColor = colors.surfaceContainerLow,
                        focusedBorderColor = colors.outlineVariant.copy(alpha = 0.35f),
                        unfocusedBorderColor = colors.outlineVariant.copy(alpha = 0.35f),
                        disabledBorderColor = colors.outlineVariant.copy(alpha = 0.35f),
                        cursorColor = colors.primary,
                        disabledTextColor = colors.onSurface,
                    ),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
        ) {
            OutlinedButton(
                onClick = onCopy,
                enabled = canCopy,
                shape = RoundedCornerShape(999.dp),
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = stringResource(Res.string.quick_translate_copy_content_description),
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    stringResource(Res.string.quick_translate_copy),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = AppSpacing.xs),
                )
            }
            Button(
                onClick = onTranslate,
                enabled = !loading && input.isNotBlank(),
                shape = RoundedCornerShape(999.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = colors.primaryContainer,
                        contentColor = colors.onPrimaryContainer,
                    ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(Res.string.quick_translate_button), fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = AppSpacing.xs))
            }
        }
    }
}

private enum class LangEdit { Source, Target }
