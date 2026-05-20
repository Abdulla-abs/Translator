package `fun`.abbas.android_res_translator.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.components.AppGlassCard
import `fun`.abbas.android_res_translator.ui.settings.AppAppearance
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsSnapshot
import `fun`.abbas.android_res_translator.ui.settings.ConsumerMode
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import `fun`.abbas.android_res_translator.ui.theme.appCodeTextStyle

@Composable
fun SettingsPageHeader(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = colors.primary, modifier = Modifier.size(28.dp))
            Text(
                "Tool Configuration",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
        }
        Text(
            "Manage your translation engine API keys and project-wide localization strategies.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
fun SettingsProvidersGrid(
    sections: List<SettingsProviderModel>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val twoColumns = maxWidth >= 600.dp
        if (twoColumns) {
            val rows = mutableListOf<List<SettingsProviderModel>>()
            var i = 0
            while (i < sections.size) {
                val current = sections[i]
                if (current.spanFullWidth) {
                    rows.add(listOf(current))
                    i += 1
                } else if (i + 1 < sections.size && !sections[i + 1].spanFullWidth) {
                    rows.add(listOf(sections[i], sections[i + 1]))
                    i += 2
                } else {
                    rows.add(listOf(current))
                    i += 1
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        row.forEach { provider ->
                            SettingsProviderCard(
                                provider = provider,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1 && !row[0].spanFullWidth) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                sections.forEach { provider ->
                    SettingsProviderCard(provider = provider)
                }
            }
        }
    }
}

@Composable
fun SettingsProviderCard(
    provider: SettingsProviderModel,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val iconColor = provider.iconTint.toColor(colors)
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = colors.surfaceContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceContainerHigh),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(provider.icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                    }
                    Text(
                        provider.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                }
                provider.tag?.let { tag ->
                    Text(
                        tag.uppercase(),
                        style = AppLabelCapsTextStyle.copy(fontWeight = FontWeight.Black),
                        color = colors.onTertiaryContainer,
                        modifier =
                            Modifier
                                .background(colors.tertiaryContainer, CircleShape)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val fieldColumns = provider.spanFullWidth && maxWidth >= 520.dp && provider.fields.size > 1
                if (fieldColumns) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                        provider.fields.forEach { field ->
                            SettingsKeyField(field = field, modifier = Modifier.weight(1f))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                        provider.fields.forEach { field ->
                            SettingsKeyField(field = field)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsKeyField(
    field: SettingsFieldModel,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            field.label.uppercase(),
            style = AppLabelCapsTextStyle,
            color = colors.onSurfaceVariant.copy(alpha = 0.6f),
        )
        OutlinedTextField(
            value = field.value,
            onValueChange = field.onValueChange,
            placeholder = { Text(field.placeholder, style = appCodeTextStyle()) },
            singleLine = true,
            visualTransformation =
                if (field.isSecret) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = if (field.isSecret) KeyboardType.Password else KeyboardType.Text,
                ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = appCodeTextStyle(),
            leadingIcon = {
                Icon(
                    Icons.Default.Key,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp),
                )
            },
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.background,
                    unfocusedContainerColor = colors.background,
                    focusedBorderColor = colors.secondaryContainer,
                    unfocusedBorderColor = colors.outlineVariant.copy(alpha = 0.5f),
                    cursorColor = colors.primary,
                ),
        )
    }
}

@Composable
fun SettingsStrategiesCard(
    draft: AppSettingsSnapshot,
    onDraft: (AppSettingsSnapshot) -> Unit,
    /** 主题切换立即落库，使 [AppRoot] 中 AppTheme 能读到新 snapshot（无需再点 Save）。 */
    onAppearancePersist: (AppSettingsSnapshot) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    AppGlassCard(modifier = modifier) {
        Box(Modifier.fillMaxWidth()) {
            Icon(
                Icons.Default.Storage,
                contentDescription = null,
                tint = colors.secondary.copy(alpha = 0.08f),
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(120.dp)
                        .padding(top = AppSpacing.sm),
            )
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(22.dp))
                    Text(
                        "Translation Strategies",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                AppearanceThemeRow(
                    selected = draft.appAppearance,
                    onSelect = { appearance ->
                        val next = draft.copy(appAppearance = appearance)
                        onDraft(next)
                        onAppearancePersist(next)
                    },
                )

                MergeStrategyRow(
                    selected = draft.consumerMode,
                    onSelect = { onDraft(draft.copy(consumerMode = it)) },
                )

                StrategyToggleRow(
                    title = "Force Translate",
                    description = "Translate elements explicitly marked with translatable=\"false\".",
                    checked = draft.forceTranslation,
                    onCheckedChange = { onDraft(draft.copy(forceTranslation = it)) },
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.outlineVariant.copy(alpha = 0.35f)),
                )

                LocalizationDefaultsRow(
                    sourceLang = draft.defaultSourceLang,
                    targetLang = draft.defaultTargetLang,
                    onSourceChange = { onDraft(draft.copy(defaultSourceLang = it)) },
                    onTargetChange = { onDraft(draft.copy(defaultTargetLang = it)) },
                )
            }
        }
    }
}

@Composable
private fun AppearanceThemeRow(
    selected: AppAppearance,
    onSelect: (AppAppearance) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            "Interface theme",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Color palette only; typography and spacing stay as in the app design system.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceContainerHighest.copy(alpha = 0.5f))
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val items =
                listOf(
                    AppAppearance.Classic to "Classic",
                    AppAppearance.GeekAbyss to "Geek Abyss",
                    AppAppearance.MinimalistPorcelain to "Porcelain",
                )
            items.forEach { (appearance, label) ->
                val isSelected = selected == appearance
                val bg = if (isSelected) colors.secondaryContainer else Color.Transparent
                val fg = if (isSelected) colors.onSecondaryContainer else colors.onSurfaceVariant
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .clickable { onSelect(appearance) }
                            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = AppLabelCapsTextStyle.copy(fontWeight = FontWeight.Black),
                        color = fg,
                    )
                    if (isSelected) {
                        Text("✓", style = AppLabelCapsTextStyle, color = fg)
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(AppSpacing.sm))
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.outlineVariant.copy(alpha = 0.25f)),
    )
}

@Composable
private fun MergeStrategyRow(
    selected: ConsumerMode,
    onSelect: (ConsumerMode) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val stacked = maxWidth < 560.dp
        if (stacked) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                MergeStrategyCopy()
                MergeStrategySegmented(selected = selected, onSelect = onSelect)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MergeStrategyCopy(modifier = Modifier.weight(1f).padding(end = AppSpacing.md))
                MergeStrategySegmented(selected = selected, onSelect = onSelect)
            }
        }
    }
    Spacer(Modifier.height(AppSpacing.sm))
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.outlineVariant.copy(alpha = 0.25f)),
    )
}

@Composable
private fun MergeStrategyCopy(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text("Merge Strategy", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(
            "Define how incoming translations from AI engines are integrated into existing XML resource files.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MergeStrategySegmented(
    selected: ConsumerMode,
    onSelect: (ConsumerMode) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceContainerHighest.copy(alpha = 0.5f))
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MergeSegment(
            label = "Incremental",
            selected = selected == ConsumerMode.FILLED,
            onClick = { onSelect(ConsumerMode.FILLED) },
        )
        MergeSegment(
            label = "Overwrite",
            selected = selected == ConsumerMode.ALL_REPLACE,
            onClick = { onSelect(ConsumerMode.ALL_REPLACE) },
        )
    }
}

@Composable
private fun MergeSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val bg = if (selected) colors.secondaryContainer else Color.Transparent
    val fg = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant
    Text(
        text = label.uppercase(),
        style = AppLabelCapsTextStyle.copy(fontWeight = FontWeight.Black),
        color = fg,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(bg)
                .clickable(onClick = onClick)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
    )
}

@Composable
private fun StrategyToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        )
    }
}

@Composable
private fun LocalizationDefaultsRow(
    sourceLang: String,
    targetLang: String,
    onSourceChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Text("Localization Defaults", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 480.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    SettingsKeyField(
                        field =
                            SettingsFieldModel(
                                label = "Source Language",
                                placeholder = "en",
                                isSecret = false,
                                value = sourceLang,
                                onValueChange = onSourceChange,
                            ),
                        modifier = Modifier.weight(1f),
                    )
                    SettingsKeyField(
                        field =
                            SettingsFieldModel(
                                label = "Target Language",
                                placeholder = "zh",
                                isSecret = false,
                                value = targetLang,
                                onValueChange = onTargetChange,
                            ),
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    SettingsKeyField(
                        field =
                            SettingsFieldModel(
                                label = "Source Language",
                                placeholder = "en",
                                isSecret = false,
                                value = sourceLang,
                                onValueChange = onSourceChange,
                            ),
                    )
                    SettingsKeyField(
                        field =
                            SettingsFieldModel(
                                label = "Target Language",
                                placeholder = "zh",
                                isSecret = false,
                                value = targetLang,
                                onValueChange = onTargetChange,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsDangerZone(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.errorContainer.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.errorContainer.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = colors.error, modifier = Modifier.size(22.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    "Advanced: Terminal Sync",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.error,
                )
                Text(
                    "Synchronizing with remote Git repositories may overwrite localized XML configurations. " +
                        "Ensure your workspace is clean before syncing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * 设置页底部悬浮保存区，对齐 React `thrid_page` 固定底栏 + Material3 [ExtendedFloatingActionButton]。
 * 仅由 [SettingsScreen] 挂载，离开设置 Tab 即不可见。
 */
@Composable
fun SettingsFloatingSaveAction(
    onSave: () -> Unit,
    savedHint: String?,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Transparent,
                                    colors.background.copy(alpha = 0.85f),
                                    colors.background,
                                ),
                        ),
                    ),
        )
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = AppSpacing.gutter, vertical = AppSpacing.md),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            savedHint?.let { hint ->
                Text(
                    hint,
                    color = colors.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End,
                )
            }
            ExtendedFloatingActionButton(
                onClick = onSave,
                containerColor = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(AppSpacing.sm))
                Text(
                    "SAVE CONFIGURATIONS",
                    style = AppLabelCapsTextStyle.copy(fontWeight = FontWeight.Black),
                )
            }
        }
    }
}
