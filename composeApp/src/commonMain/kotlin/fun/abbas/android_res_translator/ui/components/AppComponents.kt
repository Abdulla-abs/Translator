package `fun`.abbas.android_res_translator.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.theme.AppCardShape
import `fun`.abbas.android_res_translator.ui.theme.AppControlShape
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppOutlineStrokeWidth
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import `fun`.abbas.android_res_translator.ui.theme.appCodeTextStyle

@Composable
fun AppSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title.uppercase(),
        style = AppLabelCapsTextStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = AppSpacing.xs),
    )
}

/**
 * Level 2 卡片：surfaceContainerHigh + 1.5px 描边 + 12px 圆角（DESIGN.md Elevation & Shapes）。
 */
@Composable
fun AppSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppCardShape,
        color = colors.surfaceContainerHigh,
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

@Composable
fun AppPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = AppControlShape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        content = content,
    )
}

@Composable
fun AppOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    useMonospace: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        label = { Text(label) },
        singleLine = singleLine && minLines <= 1,
        minLines = minLines,
        modifier = modifier.fillMaxWidth(),
        shape = AppControlShape,
        textStyle =
            if (useMonospace) {
                appCodeTextStyle()
            } else {
                MaterialTheme.typography.bodyLarge
            },
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.secondary,
                unfocusedBorderColor = colors.outline,
                focusedLabelColor = colors.secondary,
                cursorColor = colors.secondary,
            ),
    )
}

/** 4px 高线性进度条；[inProgress] 为 true 时轻微脉冲。 */
@Composable
fun AppThinProgress(
    progress: Float?,
    modifier: Modifier = Modifier,
    inProgress: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val infinite = rememberInfiniteTransition(label = "progressPulse")
    val pulseAlpha by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(900),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulseAlpha",
    )
    val alphaModifier =
        if (inProgress) {
            Modifier.alpha(pulseAlpha)
        } else {
            Modifier
        }

    if (progress != null) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = modifier.fillMaxWidth().height(4.dp).then(alphaModifier),
            color = colors.primaryContainer,
            trackColor = colors.surfaceContainerHighest,
            strokeCap = StrokeCap.Round,
        )
    } else {
        LinearProgressIndicator(
            modifier = modifier.fillMaxWidth().height(4.dp).then(alphaModifier),
            color = colors.primaryContainer,
            trackColor = colors.surfaceContainerHighest,
            strokeCap = StrokeCap.Round,
        )
    }
}
