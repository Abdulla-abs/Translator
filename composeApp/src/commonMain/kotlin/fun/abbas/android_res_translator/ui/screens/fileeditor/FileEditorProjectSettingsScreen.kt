package `fun`.abbas.android_res_translator.ui.screens.fileeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.components.AppGlassCard
import `fun`.abbas.android_res_translator.ui.navigation.AppBackHandler
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_back
import androidrestranslator.composeapp.generated.resources.file_editor_project_settings
import androidrestranslator.composeapp.generated.resources.file_editor_project_settings_subtitle
import androidrestranslator.composeapp.generated.resources.settings_force_translate
import androidrestranslator.composeapp.generated.resources.settings_force_translate_hint
import org.jetbrains.compose.resources.stringResource

@Composable
fun FileEditorProjectSettingsScreen(
    projectDisplayName: String,
    forceTranslation: Boolean,
    onForceTranslationChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    AppBackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            Surface(color = colors.background, tonalElevation = 0.dp) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back),
                            tint = colors.primary,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(Res.string.file_editor_project_settings),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                        )
                        Text(
                            projectDisplayName,
                            style = AppCodeSmallTextStyle,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
                HorizontalDivider(color = colors.outlineVariant)
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(AppSpacing.gutter),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Text(
                    stringResource(Res.string.file_editor_project_settings_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                AppGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f).padding(end = AppSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            Text(
                                stringResource(Res.string.settings_force_translate),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                stringResource(Res.string.settings_force_translate_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = forceTranslation,
                            onCheckedChange = onForceTranslationChange,
                            enabled = enabled,
                            colors =
                                SwitchDefaults.colors(
                                    checkedThumbColor = colors.onSecondaryContainer,
                                    checkedTrackColor = colors.secondaryContainer,
                                    uncheckedThumbColor = colors.onSurfaceVariant,
                                    uncheckedTrackColor = colors.surfaceVariant,
                                ),
                        )
                    }
                }
            }
        }
    }
}
