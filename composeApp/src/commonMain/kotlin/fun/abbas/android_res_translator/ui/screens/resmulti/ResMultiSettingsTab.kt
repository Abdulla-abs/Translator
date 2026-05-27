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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import `fun`.abbas.android_res_translator.ui.theme.AppOutlineStrokeWidth
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.resmulti_detail_feature_coming_soon
import androidrestranslator.composeapp.generated.resources.resmulti_settings_add_column
import androidrestranslator.composeapp.generated.resources.resmulti_settings_add_locale
import androidrestranslator.composeapp.generated.resources.resmulti_settings_diagnostic_desc
import androidrestranslator.composeapp.generated.resources.resmulti_settings_diagnostic_title
import androidrestranslator.composeapp.generated.resources.resmulti_settings_factory_reset
import androidrestranslator.composeapp.generated.resources.resmulti_settings_general_title
import androidrestranslator.composeapp.generated.resources.resmulti_settings_locales_title
import androidrestranslator.composeapp.generated.resources.resmulti_settings_primary_source
import androidrestranslator.composeapp.generated.resources.resmulti_settings_project_name
import androidrestranslator.composeapp.generated.resources.resmulti_settings_save
import androidrestranslator.composeapp.generated.resources.resmulti_settings_source_path
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResMultiSettingsTab(
    project: ResMultiProject,
    modifier: Modifier = Modifier,
) {
    if (!project.isReady) {
        ResMultiNotReadyPlaceholder(modifier = modifier)
        return
    }

    var projectName by remember(project.displayName) { mutableStateOf(project.displayName) }
    var sourcePath by remember(project.sourceResPath) { mutableStateOf(project.sourceResPath) }
    var newLocaleCode by remember { mutableStateOf("") }
    var newLocaleFolder by remember { mutableStateOf("") }
    var newLocaleName by remember { mutableStateOf("") }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        ResMultiSectionCard {
            Text(
                stringResource(Res.string.resmulti_settings_general_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = AppSpacing.md),
            )
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text(stringResource(Res.string.resmulti_settings_project_name)) },
                    singleLine = true,
                    shape = AppControlShape,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = sourcePath,
                    onValueChange = { sourcePath = it },
                    label = { Text(stringResource(Res.string.resmulti_settings_source_path)) },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    singleLine = true,
                    shape = AppControlShape,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = AppCodeSmallTextStyle,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = {},
                        enabled = false,
                        shape = AppControlShape,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            stringResource(Res.string.resmulti_settings_save),
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        }

        ResMultiSectionCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                modifier = Modifier.padding(bottom = AppSpacing.md),
            ) {
                Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    stringResource(Res.string.resmulti_settings_locales_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                modifier = Modifier.padding(bottom = AppSpacing.lg),
            ) {
                project.languages.forEach { lang ->
                    Surface(
                        shape = AppControlShape,
                        color = MaterialTheme.colorScheme.background,
                        border = BorderStroke(AppOutlineStrokeWidth.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            Column {
                                Text(
                                    lang.langCode.uppercase(),
                                    style = AppCodeSmallTextStyle.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    lang.folderName,
                                    style = AppCodeSmallTextStyle.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (lang.langCode == "default" || lang.langCode == "en") {
                                Surface(
                                    shape = AppControlShape,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                ) {
                                    Text(
                                        stringResource(Res.string.resmulti_settings_primary_source),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                    )
                                }
                            } else {
                                IconButton(onClick = {}, enabled = false) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            Text(
                stringResource(Res.string.resmulti_settings_add_locale),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = AppSpacing.sm),
            )
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                OutlinedTextField(
                    value = newLocaleCode,
                    onValueChange = { newLocaleCode = it },
                    label = { Text("ISO Code") },
                    singleLine = true,
                    shape = AppControlShape,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = newLocaleFolder,
                    onValueChange = { newLocaleFolder = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    shape = AppControlShape,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    OutlinedTextField(
                        value = newLocaleName,
                        onValueChange = { newLocaleName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        shape = AppControlShape,
                        modifier = Modifier.weight(1f),
                    )
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
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text(
                            stringResource(Res.string.resmulti_settings_add_column),
                            modifier = Modifier.padding(start = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }

        ResMultiSectionCard {
            Text(
                stringResource(Res.string.resmulti_settings_diagnostic_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = AppSpacing.xs),
            )
            Text(
                stringResource(Res.string.resmulti_settings_diagnostic_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = AppSpacing.md),
            )
            OutlinedButton(
                onClick = {},
                enabled = false,
                shape = AppControlShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(
                    stringResource(Res.string.resmulti_settings_factory_reset),
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            Text(
                stringResource(Res.string.resmulti_detail_feature_coming_soon),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        }
    }
}

@Preview
@Composable
private fun ResMultiSettingsTabReadyPreview() {
    AppTheme {
        ResMultiSettingsTab(
            project = previewResMultiProject(),
            modifier = Modifier.padding(AppSpacing.md),
        )
    }
}

@Preview
@Composable
private fun ResMultiSettingsTabNotReadyPreview() {
    AppTheme {
        ResMultiSettingsTab(
            project = previewResMultiProject(initState = ResMultiInitState.PENDING),
            modifier = Modifier.padding(AppSpacing.md),
        )
    }
}
