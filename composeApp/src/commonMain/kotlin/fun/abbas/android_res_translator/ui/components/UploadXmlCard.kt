package `fun`.abbas.android_res_translator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.files.DroppedXmlFile
import `fun`.abbas.android_res_translator.ui.files.rememberUploadXmlDropModifier
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun UploadXmlCard(
    titleRes: StringResource,
    hintRes: StringResource,
    onClick: () -> Unit,
    onDrop: (List<DroppedXmlFile>) -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color? = null,
) {
    val colors = MaterialTheme.colorScheme
    val titleColor = textColor ?: colors.onSurface
    val hintColor = textColor ?: colors.outline
    val dropModifier = rememberUploadXmlDropModifier(onDrop)
    Surface(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(168.dp)
                .dashedRoundRectBorder(colors.outlineVariant.copy(alpha = 0.35f))
                .then(dropModifier),
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
                    tint = if (textColor != null) textColor else colors.outline,
                )
            }
            Spacer(Modifier.height(AppSpacing.sm))
            Text(
                stringResource(titleRes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = titleColor,
            )
            Text(
                stringResource(hintRes),
                style = MaterialTheme.typography.bodySmall,
                color = hintColor,
            )
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
