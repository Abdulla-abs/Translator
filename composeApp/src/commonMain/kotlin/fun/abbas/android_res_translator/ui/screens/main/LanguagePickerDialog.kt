package `fun`.abbas.android_res_translator.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            if (options.isEmpty()) {
                Text(
                    text =
                        if (engine == null) {
                            stringResource(Res.string.language_picker_no_engine)
                        } else {
                            stringResource(Res.string.language_picker_no_options, engine.displayName)
                        },
                    color = colors.onSurfaceVariant,
                )
            } else {
                LanguagePickerOptionList(
                    options = options,
                    selectedCode = selectedCode,
                    onSelect = { code ->
                        onSelect(code)
                        onDismiss()
                    },
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_close)) }
        },
    )
}

@Composable
private fun LanguagePickerOptionList(
    options: List<LanguagePickerOption>,
    selectedCode: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    Box(modifier = modifier.heightIn(max = 360.dp).fillMaxWidth()) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp),
        ) {
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
                            .clickable { onSelect(option.code) }
                            .padding(vertical = 12.dp),
                )
                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))
            }
        }
        LanguagePickerListScrollbar(
            listState = listState,
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        )
    }
}
