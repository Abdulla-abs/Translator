package `fun`.abbas.android_res_translator.ui.screens.files

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing

@Composable
fun FileBreadcrumbBar(
    pathSegments: List<String>,
    onHomeClick: () -> Unit,
    onSegmentClick: (Int) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Surface(
                onClick = onHomeClick,
                shape = CircleShape,
                color = colors.surfaceContainerHigh,
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Root Directory",
                    tint = colors.primary,
                    modifier = Modifier.padding(8.dp).size(20.dp),
                )
            }
            pathSegments.forEachIndexed { index, segment ->
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.outline.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp),
                )
                val isLast = index == pathSegments.lastIndex
                if (isLast) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = colors.primary.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                segment,
                                style = AppCodeSmallTextStyle,
                                color = colors.primary,
                                modifier = Modifier.padding(start = 4.dp),
                            )
                        }
                    }
                } else {
                    Surface(
                        onClick = { onSegmentClick(index) },
                        shape = RoundedCornerShape(999.dp),
                        color = colors.surfaceContainerHigh,
                    ) {
                        Text(
                            segment,
                            style = AppCodeSmallTextStyle,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
        IconButton(onClick = onFilterClick) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = colors.onSurfaceVariant)
        }
    }
}
