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
import `fun`.abbas.android_res_translator.ui.translation.LanguagePickerOption
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_close
import androidrestranslator.composeapp.generated.resources.language_picker_no_engine
import androidrestranslator.composeapp.generated.resources.language_picker_no_options
import org.jetbrains.compose.resources.stringResource

@Composable
fun LanguagePickerDialog(
    title: String,
    engine: ActiveTranslationEngine?,
    options: List<LanguagePickerOption>,
    selectedCode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            if (engine == null) {
                Text(
                    stringResource(Res.string.language_picker_no_engine),
                    color = colors.onSurfaceVariant,
                )
            } else if (options.isEmpty()) {
                Text(
                    stringResource(Res.string.language_picker_no_options, engine.displayName),
                    color = colors.onSurfaceVariant,
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(options, key = { it.code }) { option ->
                        val selected = option.code.equals(selectedCode.trim(), ignoreCase = true)
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) colors.primary else colors.onSurface,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(option.code)
                                        onDismiss()
                                    }
                                    .padding(vertical = 12.dp),
                        )
                        HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_close)) }
        },
    )
}
