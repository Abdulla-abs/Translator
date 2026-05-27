package `fun`.abbas.android_res_translator.ui.screens.resmulti

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import `fun`.abbas.android_res_translator.ui.theme.AppTheme
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.resmulti_about_badge
import androidrestranslator.composeapp.generated.resources.resmulti_about_desc
import androidrestranslator.composeapp.generated.resources.resmulti_about_feature_ai
import androidrestranslator.composeapp.generated.resources.resmulti_about_feature_ai_desc
import androidrestranslator.composeapp.generated.resources.resmulti_about_feature_xml
import androidrestranslator.composeapp.generated.resources.resmulti_about_feature_xml_desc
import androidrestranslator.composeapp.generated.resources.resmulti_about_footer
import androidrestranslator.composeapp.generated.resources.resmulti_about_howto_step1
import androidrestranslator.composeapp.generated.resources.resmulti_about_howto_step2
import androidrestranslator.composeapp.generated.resources.resmulti_about_howto_step3
import androidrestranslator.composeapp.generated.resources.resmulti_about_howto_title
import androidrestranslator.composeapp.generated.resources.resmulti_about_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResMultiAboutTab(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        ResMultiSectionCard {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text(
                        stringResource(Res.string.resmulti_about_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                stringResource(Res.string.resmulti_about_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = AppSpacing.md),
            )
            Text(
                stringResource(Res.string.resmulti_about_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            ResMultiAboutFeatureCard(
                icon = Icons.Default.AutoAwesome,
                title = stringResource(Res.string.resmulti_about_feature_ai),
                description = stringResource(Res.string.resmulti_about_feature_ai_desc),
                iconTint = MaterialTheme.colorScheme.tertiary,
                iconBackground = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                modifier = Modifier.weight(1f),
            )
            ResMultiAboutFeatureCard(
                icon = Icons.Default.Code,
                title = stringResource(Res.string.resmulti_about_feature_xml),
                description = stringResource(Res.string.resmulti_about_feature_xml_desc),
                iconTint = MaterialTheme.colorScheme.secondary,
                iconBackground = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                modifier = Modifier.weight(1f),
            )
        }

        ResMultiSectionCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                modifier = Modifier.padding(bottom = AppSpacing.md),
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    stringResource(Res.string.resmulti_about_howto_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Text(
                    stringResource(Res.string.resmulti_about_howto_step1),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(
                    shape = AppControlShape,
                    color = MaterialTheme.colorScheme.background,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                ) {
                    Text(
                        """
                        <resources>
                            <string name="app_name">RepluGin Test</string>
                            <string name="welcome_message">Welcome!</string>
                        </resources>
                        """.trimIndent(),
                        style = AppCodeSmallTextStyle,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(AppSpacing.sm),
                    )
                }
                Text(
                    stringResource(Res.string.resmulti_about_howto_step2),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(Res.string.resmulti_about_howto_step3),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(Res.string.resmulti_about_footer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(14.dp).padding(horizontal = 4.dp),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ResMultiAboutFeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    iconTint: androidx.compose.ui.graphics.Color,
    iconBackground: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    ResMultiSectionCard(modifier = modifier) {
        Surface(
            shape = AppControlShape,
            color = iconBackground,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(6.dp),
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = AppSpacing.sm),
        )
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppSpacing.xs),
        )
    }
}

@Preview
@Composable
private fun ResMultiAboutTabPreview() {
    AppTheme {
        ResMultiAboutTab(modifier = Modifier.padding(AppSpacing.md))
    }
}
