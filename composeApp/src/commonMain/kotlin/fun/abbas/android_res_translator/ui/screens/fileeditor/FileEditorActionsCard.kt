package `fun`.abbas.android_res_translator.ui.screens.fileeditor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `fun`.abbas.android_res_translator.ui.theme.AppCardShape
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppOutlineStrokeWidth
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.dp
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.file_editor_export_xml
import androidrestranslator.composeapp.generated.resources.file_editor_pause_translation
import androidrestranslator.composeapp.generated.resources.file_editor_resume_translation
import androidrestranslator.composeapp.generated.resources.file_editor_retranslate_confirm
import androidrestranslator.composeapp.generated.resources.file_editor_start_translation
import org.jetbrains.compose.resources.stringResource

@Composable
fun FileEditorActionsCard(
    state: FileEditorState,
    translationEnabled: Boolean = true,
    exportEnabled: Boolean = true,
    onTranslationAction: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppCardShape,
        color = colors.surfaceContainerHigh,
        border = BorderStroke(AppOutlineStrokeWidth.dp, colors.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            val pauseLabel =
                when {
                    state.isPaused -> stringResource(Res.string.file_editor_resume_translation)
                    state.isRunning -> stringResource(Res.string.file_editor_pause_translation)
                    state.isExportReady -> stringResource(Res.string.file_editor_retranslate_confirm)
                    else -> stringResource(Res.string.file_editor_start_translation)
                }
            val pauseIcon =
                when {
                    state.isPaused || !state.isRunning -> Icons.Default.PlayCircle
                    else -> Icons.Default.PauseCircle
                }
            Button(
                onClick = onTranslationAction,
                enabled = translationEnabled,
                modifier = Modifier.fillMaxWidth(),
                shape = AppControlShape,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (state.isPaused) {
                                colors.tertiaryContainer
                            } else {
                                colors.primaryContainer
                            },
                        contentColor =
                            if (state.isPaused) {
                                colors.onTertiaryContainer
                            } else {
                                colors.onPrimaryContainer
                            },
                    ),
            ) {
                Icon(pauseIcon, contentDescription = null)
                Text(pauseLabel, modifier = Modifier.padding(start = AppSpacing.sm))
            }
            if (state.isExportReady) {
                Button(
                    onClick = onExportClick,
                    enabled = exportEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppControlShape,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = colors.secondaryContainer,
                            contentColor = colors.onSecondaryContainer,
                        ),
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Text(
                        stringResource(Res.string.file_editor_export_xml, state.targetLang.uppercase()),
                        modifier = Modifier.padding(start = AppSpacing.sm),
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onExportClick,
                    enabled = exportEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppControlShape,
                    border = BorderStroke(1.dp, colors.outlineVariant),
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Text(
                        stringResource(Res.string.file_editor_export_xml, state.targetLang.uppercase()),
                        modifier = Modifier.padding(start = AppSpacing.sm),
                    )
                }
            }
        }
    }
}
