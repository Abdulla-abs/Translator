package `fun`.abbas.android_res_translator.ui.components.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.core.resources.compare.FlatRowKind
import `fun`.abbas.android_res_translator.core.resources.resmulti.ResMultiImportCompareCell
import `fun`.abbas.android_res_translator.core.resources.resmulti.ResMultiImportCompareMatrix
import `fun`.abbas.android_res_translator.core.resources.resmulti.ResMultiImportCompareRow
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.compare_grid_column_key
import org.jetbrains.compose.resources.stringResource

private val KeyColumnWidth = 148.dp
private val ValueColumnWidth = 180.dp
private val RowHeight = 44.dp
private val CellInteractionCornerRadius = 8.dp

@Composable
fun StickyMultiCompareGrid(
    matrix: ResMultiImportCompareMatrix,
    onDiffCellClick: (ResMultiImportCompareRow, columnIndex: Int, cell: ResMultiImportCompareCell) -> Unit,
    onCellLongPressCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val horizontalScroll = rememberScrollState()
    val keyHeader = stringResource(Res.string.compare_grid_column_key)

    Column(modifier = modifier.fillMaxWidth()) {
        MultiCompareGridHeaderRow(
            keyHeader = keyHeader,
            columnLabels = matrix.columns.map { it.headerLabel },
            horizontalScroll = horizontalScroll,
        )
        HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.4f))
        LazyColumn(modifier = Modifier.heightIn(max = 720.dp)) {
            items(matrix.rows, key = { it.key }) { row ->
                MultiCompareGridDataRow(
                    row = row,
                    horizontalScroll = horizontalScroll,
                    onDiffCellClick = onDiffCellClick,
                    onCellLongPressCopy = onCellLongPressCopy,
                )
                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.25f))
            }
        }
    }
}

@Composable
private fun MultiCompareGridHeaderRow(
    keyHeader: String,
    columnLabels: List<String>,
    horizontalScroll: androidx.compose.foundation.ScrollState,
) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surfaceContainerHigh) {
        Row(
            modifier = Modifier.fillMaxWidth().height(RowHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(KeyColumnWidth)
                        .padding(horizontal = AppSpacing.sm),
            ) {
                Text(
                    keyHeader,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurfaceVariant,
                )
            }
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScroll),
            ) {
                columnLabels.forEach { label ->
                    ValueHeaderCell(label)
                }
            }
        }
    }
}

@Composable
private fun ValueHeaderCell(label: String) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier =
            Modifier
                .width(ValueColumnWidth)
                .padding(horizontal = AppSpacing.sm),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MultiCompareGridDataRow(
    row: ResMultiImportCompareRow,
    horizontalScroll: androidx.compose.foundation.ScrollState,
    onDiffCellClick: (ResMultiImportCompareRow, Int, ResMultiImportCompareCell) -> Unit,
    onCellLongPressCopy: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val rowBg =
        if (row.hasDifference) {
            colors.errorContainer.copy(alpha = 0.55f)
        } else {
            colors.surface
        }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = RowHeight)
                .height(IntrinsicSize.Max)
                .background(rowBg),
    ) {
        InteractiveMultiCompareCell(
            text = row.key,
            contentColor = colors.onSurface,
            secondaryText =
                if (row.kind != FlatRowKind.STRING) {
                    row.kind.name.lowercase().replace('_', ' ')
                } else {
                    null
                },
            secondaryColor = colors.onSurfaceVariant,
            useCodeStyle = true,
            highlightDifference = false,
            onClick = null,
            onLongPressCopy = onCellLongPressCopy,
            modifier = Modifier.width(KeyColumnWidth),
        )
        Row(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(horizontalScroll),
        ) {
            row.cells.forEachIndexed { index, cell ->
                InteractiveMultiCompareCell(
                    text = cell.importValue,
                    contentColor =
                        if (cell.hasDifference) {
                            colors.onErrorContainer
                        } else {
                            colors.onSurface
                        },
                    highlightDifference = cell.hasDifference,
                    onClick =
                        if (cell.hasDifference) {
                            { onDiffCellClick(row, index, cell) }
                        } else {
                            null
                        },
                    onLongPressCopy = onCellLongPressCopy,
                    modifier = Modifier.width(ValueColumnWidth),
                )
            }
        }
    }
}

@Composable
private fun InteractiveMultiCompareCell(
    text: String,
    contentColor: Color,
    onLongPressCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: String? = null,
    secondaryColor: Color = contentColor,
    useCodeStyle: Boolean = false,
    highlightDifference: Boolean,
    onClick: (() -> Unit)?,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val displayText = text.ifEmpty { "—" }
    val copyText = text.trim()

    val interactionOverlay =
        when {
            isPressed -> colors.onSurface.copy(alpha = 0.12f)
            isHovered -> colors.onSurface.copy(alpha = 0.08f)
            else -> Color.Transparent
        }

    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .hoverable(interactionSource)
                .combinedClickable(
                    interactionSource = interactionSource,
                    onClick = { onClick?.invoke() },
                    onLongClick = {
                        if (copyText.isNotEmpty()) {
                            onLongPressCopy(copyText)
                        }
                    },
                ),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (interactionOverlay != Color.Transparent) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(interactionOverlay, RoundedCornerShape(CellInteractionCornerRadius)),
            )
        }
        if (highlightDifference) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            colors.error.copy(alpha = 0.08f),
                            RoundedCornerShape(CellInteractionCornerRadius),
                        ),
            )
        }
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = RowHeight)
                        .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            ) {
                Text(
                    displayText,
                    style = if (useCodeStyle) AppCodeSmallTextStyle else MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                    maxLines = if (useCodeStyle) 2 else 4,
                    overflow = TextOverflow.Ellipsis,
                )
                if (secondaryText != null) {
                    Text(
                        secondaryText,
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryColor,
                    )
                }
            }
        }
    }
}
