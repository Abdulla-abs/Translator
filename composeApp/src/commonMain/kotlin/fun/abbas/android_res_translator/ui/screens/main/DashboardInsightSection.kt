package `fun`.abbas.android_res_translator.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.components.AppGlassCard
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidx.compose.foundation.BorderStroke

@Composable
fun DashboardInsightSection(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth >= 700.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                EfficiencyCard(Modifier.weight(3f))
                SmartSyncCard(Modifier.weight(1f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                EfficiencyCard(Modifier.fillMaxWidth())
                SmartSyncCard(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun EfficiencyCard(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    AppGlassCard(modifier = modifier.height(200.dp)) {
        Box(Modifier.fillMaxWidth().height(168.dp)) {
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        colors.surfaceContainerHigh.copy(alpha = 0.35f),
                                        colors.background,
                                    ),
                            ),
                        ),
            )
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        colors.background,
                                        colors.background.copy(alpha = 0f),
                                    ),
                            ),
                        ),
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    "Efficiency +24%",
                    style = MaterialTheme.typography.displayLarge,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Your translation workflow has significantly improved this week using AI-powered context mapping and automated KMP bridging.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SmartSyncCard(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.height(200.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.tertiaryContainer.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, colors.tertiary.copy(alpha = 0.2f)),
    ) {
        Box(Modifier.fillMaxWidth().height(200.dp)) {
            Box(
                modifier =
                    Modifier
                        .size(96.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 24.dp, y = (-24).dp)
                        .clip(CircleShape)
                        .background(colors.tertiary.copy(alpha = 0.2f)),
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(AppSpacing.lg),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = colors.tertiary,
                    modifier = Modifier.size(48.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(
                        "Smart Sync",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onTertiaryContainer,
                    )
                    Text(
                        "Automatically sync localized changes to your active KMP modules.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onTertiaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}
