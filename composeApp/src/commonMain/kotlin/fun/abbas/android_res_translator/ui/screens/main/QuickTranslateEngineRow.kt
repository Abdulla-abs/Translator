package `fun`.abbas.android_res_translator.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.components.AppLanguageChip
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import `fun`.abbas.android_res_translator.ui.translation.ActiveTranslationEngine

@Composable
fun QuickTranslateEngineRow(
    selectedEngine: ActiveTranslationEngine?,
    onEditEngine: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = colors.tertiary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    "ENGINE",
                    style = AppLabelCapsTextStyle,
                    color = colors.outline,
                    modifier = Modifier.padding(start = AppSpacing.sm),
                )
            }
            AppLanguageChip(
                label = selectedEngine?.displayName ?: "未选择",
                onClick = onEditEngine,
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = AppSpacing.md),
            color = colors.outlineVariant.copy(alpha = 0.35f),
        )
    }
}
