package `fun`.abbas.android_res_translator.ui.screens.resmulti

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import `fun`.abbas.android_res_translator.ui.theme.AppTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppOutlineStrokeWidth
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_cancel
import androidrestranslator.composeapp.generated.resources.resmulti_detail_feature_coming_soon
import androidrestranslator.composeapp.generated.resources.resmulti_not_ready_hint
import androidrestranslator.composeapp.generated.resources.resmulti_translate_bulk_label
import androidrestranslator.composeapp.generated.resources.resmulti_translate_empty
import androidrestranslator.composeapp.generated.resources.resmulti_translate_empty_hint
import androidrestranslator.composeapp.generated.resources.resmulti_translate_filter_blank
import androidrestranslator.composeapp.generated.resources.resmulti_translate_filter_blank_active
import androidrestranslator.composeapp.generated.resources.resmulti_translate_new_key
import androidrestranslator.composeapp.generated.resources.resmulti_translate_search_placeholder
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResMultiTranslateTab(
    project: ResMultiProject,
    modifier: Modifier = Modifier,
) {
    if (!project.isReady) {
        ResMultiNotReadyPlaceholder(modifier = modifier)
        return
    }

    var searchQuery by remember { mutableStateOf("") }
    var filterMissing by remember { mutableStateOf(false) }
    var isAddingKey by remember { mutableStateOf(false) }
    var newKeyName by remember { mutableStateOf("") }
    var newKeyDesc by remember { mutableStateOf("") }
    var newKeyDefault by remember { mutableStateOf("") }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        ResMultiSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                stringResource(Res.string.resmulti_translate_search_placeholder),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        singleLine = true,
                        shape = AppControlShape,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        ),
                    )
                    OutlinedButton(
                        onClick = { filterMissing = !filterMissing },
                        shape = AppControlShape,
                        border =
                            BorderStroke(
                                AppOutlineStrokeWidth.dp,
                                if (filterMissing) {
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                            ),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                containerColor =
                                    if (filterMissing) {
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                    } else {
                                        MaterialTheme.colorScheme.background
                                    },
                                contentColor =
                                    if (filterMissing) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            ),
                    ) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text(
                            stringResource(
                                if (filterMissing) {
                                    Res.string.resmulti_translate_filter_blank_active
                                } else {
                                    Res.string.resmulti_translate_filter_blank
                                },
                            ),
                            modifier = Modifier.padding(start = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    Button(
                        onClick = { isAddingKey = !isAddingKey },
                        shape = AppControlShape,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            stringResource(Res.string.resmulti_translate_new_key),
                            modifier = Modifier.padding(start = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }

                if (isAddingKey) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        Text(
                            stringResource(Res.string.resmulti_translate_new_key),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        OutlinedTextField(
                            value = newKeyName,
                            onValueChange = { newKeyName = it },
                            label = { Text("Key") },
                            singleLine = true,
                            shape = AppControlShape,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = AppCodeSmallTextStyle,
                        )
                        OutlinedTextField(
                            value = newKeyDesc,
                            onValueChange = { newKeyDesc = it },
                            label = { Text("Description") },
                            singleLine = true,
                            shape = AppControlShape,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = newKeyDefault,
                            onValueChange = { newKeyDefault = it },
                            label = { Text("Default") },
                            singleLine = true,
                            shape = AppControlShape,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = { isAddingKey = false }) {
                                Text(stringResource(Res.string.common_cancel))
                            }
                            Button(
                                onClick = {},
                                enabled = false,
                                shape = AppControlShape,
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                    ),
                            ) {
                                Text(stringResource(Res.string.resmulti_detail_feature_coming_soon))
                            }
                        }
                    }
                }

                Surface(
                    shape = AppControlShape,
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(AppSpacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text(
                                stringResource(Res.string.resmulti_translate_bulk_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                            project.languages
                                .filter { it.langCode != "default" && it.langCode != "en" }
                                .forEach { lang ->
                                    OutlinedButton(
                                        onClick = {},
                                        enabled = false,
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Text(
                                            lang.langCode.uppercase(),
                                            modifier = Modifier.padding(start = 4.dp),
                                            fontSize = 11.sp,
                                        )
                                    }
                                }
                        }
                    }
                }
            }
        }

        ResMultiSectionCard {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
                Text(
                    stringResource(Res.string.resmulti_translate_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(Res.string.resmulti_translate_empty_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
internal fun ResMultiNotReadyPlaceholder(modifier: Modifier = Modifier) {
    ResMultiSectionCard(modifier = modifier.padding(bottom = AppSpacing.lg)) {
        Text(
            stringResource(Res.string.resmulti_not_ready_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun ResMultiTranslateTabReadyPreview() {
    AppTheme {
        ResMultiTranslateTab(
            project = previewResMultiProject(),
            modifier = Modifier.padding(AppSpacing.md),
        )
    }
}

@Preview
@Composable
private fun ResMultiTranslateTabNotReadyPreview() {
    AppTheme {
        ResMultiTranslateTab(
            project = previewResMultiProject(initState = ResMultiInitState.PENDING),
            modifier = Modifier.padding(AppSpacing.md),
        )
    }
}

@Preview
@Composable
private fun ResMultiNotReadyPlaceholderPreview() {
    AppTheme {
        ResMultiNotReadyPlaceholder(modifier = Modifier.padding(AppSpacing.md))
    }
}
