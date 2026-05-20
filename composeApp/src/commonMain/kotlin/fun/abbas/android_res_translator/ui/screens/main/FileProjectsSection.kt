package `fun`.abbas.android_res_translator.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.components.FileProjectCard
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing

@Composable
fun FileProjectsSection(
    repository: InMemoryRecentXmlProjectRepository,
    onViewAllClick: () -> Unit,
    onUploadClick: () -> Unit,
    onProjectClick: (RecentXmlProject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val projects by repository.projects.collectAsState()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
                Text(
                    "File Projects",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = AppSpacing.sm),
                )
            }
            Text(
                "VIEW ALL",
                style = AppLabelCapsTextStyle,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onViewAllClick),
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns =
                when {
                    maxWidth >= 840.dp -> 3
                    maxWidth >= 600.dp -> 2
                    else -> 1
                }
            val cells = projects.map { Cell.Project(it) } + Cell.Upload
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                cells.chunked(columns).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        row.forEach { cell ->
                            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                                when (cell) {
                                    is Cell.Project -> FileProjectCard(cell.project, onClick = { onProjectClick(cell.project) })
                                    Cell.Upload -> UploadXmlCard(onClick = onUploadClick)
                                }
                            }
                        }
                        repeat(columns - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private sealed interface Cell {
    data class Project(val project: RecentXmlProject) : Cell
    data object Upload : Cell
}

@Composable
private fun UploadXmlCard(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(168.dp)
                .dashedRoundRectBorder(colors.outlineVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainer.copy(alpha = 0.2f),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(shape = CircleShape, color = colors.surfaceContainerHigh, modifier = Modifier.size(48.dp)) {
                Icon(
                    Icons.Default.UploadFile,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = colors.outline,
                )
            }
            Spacer(Modifier.height(AppSpacing.sm))
            Text("Upload XML", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text("Drop your strings.xml here", style = MaterialTheme.typography.bodySmall, color = colors.outline)
        }
    }
}

private fun Modifier.dashedRoundRectBorder(
    color: Color,
    cornerRadius: androidx.compose.ui.unit.Dp = 16.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 2.dp,
): Modifier =
    drawBehind {
        val radius = cornerRadius.toPx()
        drawRoundRect(
            color = color,
            style =
                Stroke(
                    width = strokeWidth.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                ),
            cornerRadius = CornerRadius(radius, radius),
        )
    }
