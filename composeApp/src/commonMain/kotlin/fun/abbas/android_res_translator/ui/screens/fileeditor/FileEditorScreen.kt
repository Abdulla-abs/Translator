package `fun`.abbas.android_res_translator.ui.screens.fileeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.TranslationServices
import `fun`.abbas.android_res_translator.ui.XmlFileAccess
import `fun`.abbas.android_res_translator.ui.components.AppGlassCard
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import kotlinx.coroutines.launch

@Composable
fun FileEditorScreen(
    fileName: String,
    filePath: String,
    sourceXml: String,
    sourceLang: String,
    targetLang: String,
    services: TranslationServices,
    xmlFileAccess: XmlFileAccess,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val controller =
        remember(fileName, sourceXml) {
            FileEditorController(
                services = services,
                scope = scope,
                fileName = fileName,
                filePath = filePath,
                sourceLang = sourceLang,
                targetLang = targetLang,
                sourceXml = sourceXml,
            )
        }
    val state by controller.state.collectAsState()

    DisposableEffect(controller) {
        onDispose { controller.dispose() }
    }

    LaunchedEffect(controller, state.totalCount) {
        if (state.totalCount > 0 && !state.isRunning && state.completedCount == 0) {
            controller.startTranslationIfIdle()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
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
                        FileEditorProgressCard(state, Modifier.weight(2f))
                        FileEditorActionsCard(
                            state = state,
                            onTranslationAction = controller::onTranslationButtonClick,
                            onExport = {
                                val xml = controller.exportXml()
                                xmlFileAccess.launchSaveXml(xml, state.fileName) { ok ->
                                    controller.setExportMessage(if (ok) "已导出" else "导出取消")
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                        FileEditorProgressCard(state)
                        FileEditorActionsCard(
                            state = state,
                            onTranslationAction = controller::onTranslationButtonClick,
                            onExport = {
                                val xml = controller.exportXml()
                                xmlFileAccess.launchSaveXml(xml, state.fileName) { ok ->
                                    controller.setExportMessage(if (ok) "已导出" else "导出取消")
                                }
                            },
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
                    "$fileName ($sourceLang -> $targetLang)",
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
    AppGlassCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("XML ENTRIES", style = AppLabelCapsTextStyle, color = colors.onSurfaceVariant)
            OutlinedTextField(
                value = state.keyFilter,
                onValueChange = onKeyFilterChange,
                placeholder = { Text("Filter keys...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.widthIn(max = 220.dp),
            )
        }
        HorizontalDivider(color = colors.outlineVariant)
        if (state.filteredEntries.isEmpty()) {
            Text(
                "无匹配条目",
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
