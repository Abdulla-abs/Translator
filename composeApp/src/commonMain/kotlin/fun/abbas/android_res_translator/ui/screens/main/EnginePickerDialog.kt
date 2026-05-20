package `fun`.abbas.android_res_translator.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.translation.ActiveTranslationEngine
import `fun`.abbas.android_res_translator.ui.translation.TranslationEngineOption
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_close
import androidrestranslator.composeapp.generated.resources.engine_picker_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun EnginePickerDialog(
    options: List<TranslationEngineOption>,
    selected: ActiveTranslationEngine?,
    onDismiss: () -> Unit,
    onSelect: (ActiveTranslationEngine) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.engine_picker_title)) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                items(options, key = { it.engine.name }) { option ->
                    val isSelected = option.engine == selected
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color =
                            when {
                                isSelected -> colors.primary
                                !option.isConfigured -> colors.onSurfaceVariant
                                else -> colors.onSurface
                            },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(option.engine)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp),
                    )
                    HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_close)) }
        },
    )
}
