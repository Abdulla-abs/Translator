package `fun`.abbas.android_res_translator.ui.screens.resmulti

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.theme.AppCardShape
import `fun`.abbas.android_res_translator.ui.theme.AppTheme
import `fun`.abbas.android_res_translator.ui.theme.AppOutlineStrokeWidth
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.common_back
import androidrestranslator.composeapp.generated.resources.resmulti_detail_version_push
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResMultiProjectHeader(
    projectName: String,
    onBackClick: () -> Unit,
    showPushVersion: Boolean = false,
    pushVersionEnabled: Boolean = false,
    onPushVersionClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.background,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.common_back),
                        tint = colors.primary,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        projectName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                    )
                }
                if (showPushVersion) {
                    IconButton(
                        onClick = onPushVersionClick,
                        enabled = pushVersionEnabled,
                    ) {
                        Icon(
                            Icons.Default.Publish,
                            contentDescription = stringResource(Res.string.resmulti_detail_version_push),
                            modifier = Modifier.size(22.dp),
                            tint =
                                if (pushVersionEnabled) {
                                    colors.primary
                                } else {
                                    colors.onSurface.copy(alpha = 0.38f)
                                },
                        )
                    }
                }
            }
            HorizontalDivider(color = colors.outlineVariant)
        }
    }
}

@Preview
@Composable
private fun ResMultiProjectHeaderPreview() {
    AppTheme {
        ResMultiProjectHeader(
            projectName = previewResMultiProject().displayName,
            onBackClick = {},
            showPushVersion = true,
            pushVersionEnabled = true,
            onPushVersionClick = {},
        )
    }
}

@Preview
@Composable
private fun ResMultiProjectHeaderLongNamePreview() {
    AppTheme {
        ResMultiProjectHeader(
            projectName = "Very Long Multi-file Project Name For Layout Check",
            onBackClick = {},
        )
    }
}

@Composable
fun ResMultiSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppCardShape,
        color = colors.surfaceContainerLow,
        border = BorderStroke(AppOutlineStrokeWidth.dp, colors.outlineVariant),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            content = content,
        )
    }
}

@Preview
@Composable
private fun ResMultiSectionCardPreview() {
    AppTheme {
        ResMultiSectionCard(modifier = Modifier.padding(AppSpacing.md)) {
            Text(
                "Section card sample",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
