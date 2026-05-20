package `fun`.abbas.android_res_translator.ui.screens.fileeditor

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import `fun`.abbas.android_res_translator.ui.navigation.AppBackHandler
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.XmlFileAccess
import `fun`.abbas.android_res_translator.ui.components.AppGlassCard
import `fun`.abbas.android_res_translator.ui.screens.main.LanguagePickerDialog
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import `fun`.abbas.android_res_translator.ui.i18n.AppLocale
import `fun`.abbas.android_res_translator.ui.translation.ActiveTranslationEngine
import `fun`.abbas.android_res_translator.ui.translation.LanguagePickerCatalog
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_cancel
import androidrestranslator.composeapp.generated.resources.file_editor_export_cancelled
import androidrestranslator.composeapp.generated.resources.file_editor_export_confirm_continue
import androidrestranslator.composeapp.generated.resources.file_editor_export_confirm_message
import androidrestranslator.composeapp.generated.resources.file_editor_export_confirm_title
import androidrestranslator.composeapp.generated.resources.file_editor_export_not_ready
import androidrestranslator.composeapp.generated.resources.file_editor_exported
import androidrestranslator.composeapp.generated.resources.file_editor_filter_placeholder
import androidrestranslator.composeapp.generated.resources.file_editor_header_langs
import androidrestranslator.composeapp.generated.resources.file_editor_no_matching_entries
import androidrestranslator.composeapp.generated.resources.file_editor_xml_entries
import androidrestranslator.composeapp.generated.resources.file_editor_retranslate_confirm
import androidrestranslator.composeapp.generated.resources.file_editor_retranslate_message
import androidrestranslator.composeapp.generated.resources.file_editor_retranslate_title
import androidrestranslator.composeapp.generated.resources.language_picker_source_title
import androidrestranslator.composeapp.generated.resources.language_picker_target_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun FileEditorScreen(
    controller: FileEditorController,
    selectedEngine: ActiveTranslationEngine?,
    xmlFileAccess: XmlFileAccess,
    uiLocale: AppLocale,
    onBack: () -> Unit,
    onEditorStateChange: ((FileEditorState) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val state by controller.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    var editingLang by remember { mutableStateOf<LangEdit?>(null) }
    var showRetranslateConfirm by remember { mutableStateOf(false) }
    var showExportWithErrorsConfirm by remember { mutableStateOf(false) }

    val onTranslationAction: () -> Unit = {
        if (state.isExportReady && !state.isRunning && !state.isPaused) {
            showRetranslateConfirm = true
        } else {
            controller.onTranslationButtonClick()
        }
    }

    AppBackHandler(onBack = onBack)

    LaunchedEffect(selectedEngine) {
        controller.setPreferredVendorName(selectedEngine?.vendorName)
    }
    LaunchedEffect(uiLocale) {
        controller.uiLocale = uiLocale
    }

    val exportedMessage = stringResource(Res.string.file_editor_exported)
    val exportCancelledMessage = stringResource(Res.string.file_editor_export_cancelled)
    val exportNotReadyMessage = stringResource(Res.string.file_editor_export_not_ready)

    fun performExport() {
        val xml = controller.exportXml()
        xmlFileAccess.launchSaveXml(xml, state.fileName) { ok ->
            controller.setExportMessage(if (ok) exportedMessage else exportCancelledMessage)
        }
    }

    fun onExportClick() {
        when {
            state.errorCount > 0 -> showExportWithErrorsConfirm = true
            state.isExportReady -> performExport()
            else ->
                scope.launch {
                    snackbarHostState.showSnackbar(exportNotReadyMessage)
                }
        }
    }

    DisposableEffect(controller) {
        onDispose {
            // 离开页面不取消翻译；仅同步最新状态供首页卡片展示。
            onEditorStateChange?.invoke(controller.state.value)
        }
    }

    LaunchedEffect(
        state.entries,
        state.completedCount,
        state.isRunning,
        state.isPaused,
        state.keyFilter,
        state.errorCount,
        state.sourceLang,
        state.targetLang,
    ) {
        onEditorStateChange?.invoke(state)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    },
        ) {
            Column(Modifier.fillMaxSize()) {
                FileEditorHeader(
                    fileName = state.fileName,
                    filePath = state.filePath,
                    sourceLang = state.sourceLang,
                    targetLang = state.targetLang,
                    onBack = onBack,
                )
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(AppSpacing.gutter),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                ) {
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        if (maxWidth >= 700.dp) {
                            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                                FileEditorProgressCard(
                                    state = state,
                                    onSwapLanguages = {
                                        controller.setTranslationLanguages(state.targetLang, state.sourceLang)
                                    },
                                    onEditSourceLang = { editingLang = LangEdit.Source },
                                    onEditTargetLang = { editingLang = LangEdit.Target },
                                    modifier = Modifier.weight(2f),
                                )
                                FileEditorActionsCard(
                                    state = state,
                                    onTranslationAction = onTranslationAction,
                                    onExportClick = { onExportClick() },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                                FileEditorProgressCard(
                                    state = state,
                                    onSwapLanguages = {
                                        controller.setTranslationLanguages(state.targetLang, state.sourceLang)
                                    },
                                    onEditSourceLang = { editingLang = LangEdit.Source },
                                    onEditTargetLang = { editingLang = LangEdit.Target },
                                )
                                FileEditorActionsCard(
                                    state = state,
                                    onTranslationAction = onTranslationAction,
                                    onExportClick = { onExportClick() },
                                )
                            }
                        }
                    }
                    state.exportMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                    }
                    XmlEntriesSection(
                        state = state,
                        onKeyFilterChange = controller::setKeyFilter,
                        onRetry = controller::retryEntry,
                    )
                    Spacer(Modifier.height(72.dp))
                }
            }
        }
    }

    editingLang?.let { which ->
        val isSource = which == LangEdit.Source
        val options =
            remember(selectedEngine, state.sourceLang, state.targetLang, which) {
                when {
                    selectedEngine == null -> emptyList()
                    isSource -> LanguagePickerCatalog.sourceOptions(selectedEngine, state.targetLang)
                    else -> LanguagePickerCatalog.targetOptions(selectedEngine, state.sourceLang)
                }
            }
        val selectedCode = if (isSource) state.sourceLang else state.targetLang
        LanguagePickerDialog(
            title =
                stringResource(
                    if (isSource) Res.string.language_picker_source_title else Res.string.language_picker_target_title,
                ),
            engine = selectedEngine,
            options = options,
            selectedCode = selectedCode,
            onDismiss = { editingLang = null },
            onSelect = { code ->
                if (which == LangEdit.Source) {
                    controller.setTranslationLanguages(
                        sourceLang = code,
                        targetLang = state.targetLang,
                    )
                } else {
                    controller.setTranslationLanguages(
                        sourceLang = state.sourceLang,
                        targetLang = code,
                    )
                }
            },
        )
    }

    if (showExportWithErrorsConfirm) {
        AlertDialog(
            onDismissRequest = { showExportWithErrorsConfirm = false },
            title = { Text(stringResource(Res.string.file_editor_export_confirm_title)) },
            text = {
                Text(
                    stringResource(Res.string.file_editor_export_confirm_message, state.errorCount),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportWithErrorsConfirm = false
                        performExport()
                    },
                ) {
                    Text(stringResource(Res.string.file_editor_export_confirm_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportWithErrorsConfirm = false }) {
                    Text(stringResource(Res.string.common_cancel))
                }
            },
        )
    }

    if (showRetranslateConfirm) {
        AlertDialog(
            onDismissRequest = { showRetranslateConfirm = false },
            title = { Text(stringResource(Res.string.file_editor_retranslate_title)) },
            text = {
                Text(
                    stringResource(Res.string.file_editor_retranslate_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRetranslateConfirm = false
                        controller.retranslateAllFromScratch()
                    },
                ) {
                    Text(stringResource(Res.string.file_editor_retranslate_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRetranslateConfirm = false }) {
                    Text(stringResource(Res.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun FileEditorHeader(
    fileName: String,
    filePath: String,
    sourceLang: String,
    targetLang: String,
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.background, tonalElevation = 0.dp) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = colors.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.file_editor_header_langs, fileName, sourceLang, targetLang),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.primary,
                )
                Text(filePath, style = AppCodeSmallTextStyle, color = colors.onSurfaceVariant)
            }
        }
        HorizontalDivider(color = colors.outlineVariant)
    }
}

@Composable
private fun XmlEntriesSection(
    state: FileEditorState,
    onKeyFilterChange: (String) -> Unit,
    onRetry: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var isSearchFocused by remember { mutableStateOf(false) }
    val searchExpanded = isSearchFocused

    AppGlassCard {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.sm),
        ) {
            val collapsedWidth = minOf(220.dp, maxWidth)
            val searchWidth by animateDpAsState(
                targetValue = if (searchExpanded) maxWidth else collapsedWidth,
                animationSpec = tween(durationMillis = 280),
                label = "xmlEntriesSearchWidth",
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!searchExpanded) {
                    Text(
                        stringResource(Res.string.file_editor_xml_entries),
                        style = AppLabelCapsTextStyle,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f).padding(end = AppSpacing.sm),
                    )
                }
                OutlinedTextField(
                    value = state.keyFilter,
                    onValueChange = onKeyFilterChange,
                    placeholder = { Text(stringResource(Res.string.file_editor_filter_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = AppControlShape,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.secondary,
                            unfocusedBorderColor = colors.outlineVariant,
                            cursorColor = colors.secondary,
                        ),
                    modifier =
                        Modifier
                            .width(searchWidth)
                            .onFocusChanged { isSearchFocused = it.isFocused },
                )
            }
        }
        HorizontalDivider(color = colors.outlineVariant)
        if (state.filteredEntries.isEmpty()) {
            Text(
                stringResource(Res.string.file_editor_no_matching_entries),
                modifier = Modifier.padding(vertical = AppSpacing.lg),
                color = colors.onSurfaceVariant,
            )
        } else {
            state.filteredEntries.forEachIndexed { index, entry ->
                XmlEntryRow(
                    entry = entry,
                    sourceLang = state.sourceLang,
                    targetLang = state.targetLang,
                    onRetry = { onRetry(entry.key) },
                )
                if (index < state.filteredEntries.lastIndex) {
                    HorizontalDivider(color = colors.outlineVariant)
                }
            }
        }
    }
}

private enum class LangEdit {
    Source,
    Target,
}
