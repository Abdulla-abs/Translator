package `fun`.abbas.android_res_translator.ui.screens.about

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.appVersionLabel
import `fun`.abbas.android_res_translator.ui.components.AppGlassCard
import `fun`.abbas.android_res_translator.ui.theme.AppCodeSmallTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppLabelCapsTextStyle
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import androidrestranslator.composeapp.generated.resources.Res
import androidrestranslator.composeapp.generated.resources.about_acknowledgments
import androidrestranslator.composeapp.generated.resources.about_building_together
import androidrestranslator.composeapp.generated.resources.about_chip_production_ready
import androidrestranslator.composeapp.generated.resources.about_footer_built
import androidrestranslator.composeapp.generated.resources.about_footer_tagline
import androidrestranslator.composeapp.generated.resources.about_free_open_source
import androidrestranslator.composeapp.generated.resources.about_hero_version
import androidrestranslator.composeapp.generated.resources.about_kotlin_version
import androidrestranslator.composeapp.generated.resources.about_licenses_title
import androidrestranslator.composeapp.generated.resources.about_open_source_body
import androidrestranslator.composeapp.generated.resources.about_open_source_title
import androidrestranslator.composeapp.generated.resources.about_view_all_licenses
import androidrestranslator.composeapp.generated.resources.about_view_github
import androidrestranslator.composeapp.generated.resources.app_name
import androidrestranslator.composeapp.generated.resources.common_close
import org.jetbrains.compose.resources.stringResource
import `fun`.abbas.android_res_translator.ui.theme.appCodeTextStyle

@Composable
fun AboutHeroSection(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    AppGlassCard(modifier = modifier.clip(RoundedCornerShape(24.dp))) {
        Box(Modifier.fillMaxWidth()) {
            Box(
                modifier =
                    Modifier
                        .size(280.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 80.dp, y = (-80).dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.1f)),
            )
            BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical = AppSpacing.md)) {
                val wide = maxWidth >= 560.dp
                val iconSize = if (wide) 224.dp else 160.dp
                if (wide) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl),
                    ) {
                        AboutAppIcon(size = iconSize)
                        AboutHeroText(Modifier.weight(1f), centered = false)
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                    ) {
                        AboutAppIcon(size = iconSize)
                        AboutHeroText(centered = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutAppIcon(size: androidx.compose.ui.unit.Dp) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier =
            Modifier
                .size(size)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(colors.primaryContainer, colors.primary),
                    ),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
        )
        Icon(
            Icons.Default.Memory,
            contentDescription = null,
            modifier = Modifier.size(size * 0.55f),
            tint = colors.onPrimaryContainer,
        )
    }
}

@Composable
private fun AboutHeroText(
    modifier: Modifier = Modifier,
    centered: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    val align = if (centered) Alignment.CenterHorizontally else Alignment.Start
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = align,
    ) {
        Text(
            stringResource(Res.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        )
        Text(
            stringResource(Res.string.about_hero_version, appVersionLabel()),
            style = appCodeTextStyle(),
            color = colors.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = AppSpacing.xs),
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        )
        Row(
            modifier =
                Modifier
                    .padding(top = AppSpacing.md)
                    .then(if (centered) Modifier.fillMaxWidth() else Modifier),
            horizontalArrangement =
                Arrangement.spacedBy(
                    AppSpacing.sm,
                    if (centered) Alignment.CenterHorizontally else Alignment.Start,
                ),
        ) {
            AboutStatusChip(stringResource(Res.string.about_chip_production_ready), colors.primary)
            AboutStatusChip(stringResource(Res.string.about_kotlin_version), colors.secondary)
        }
    }
}

@Composable
private fun AboutStatusChip(
    label: String,
    tint: Color,
) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = label.uppercase(),
        style = AppLabelCapsTextStyle.copy(fontWeight = FontWeight.Black, letterSpacing = AppLabelCapsTextStyle.letterSpacing * 1.5f),
        color = tint,
        modifier =
            Modifier
                .border(1.dp, colors.outlineVariant.copy(alpha = 0.3f), CircleShape)
                .background(colors.surfaceContainerHighest, CircleShape)
                .padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Composable
fun AboutProjectSection(
    onGitHubClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        if (maxWidth >= 700.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
                AboutOpenSourceCard(
                    onGitHubClick = onGitHubClick,
                    modifier = Modifier.weight(1f),
                )
                AboutBuildingTogetherCard(Modifier.weight(1f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
                AboutOpenSourceCard(onGitHubClick = onGitHubClick)
                AboutBuildingTogetherCard()
            }
        }
    }
}

@Composable
private fun AboutOpenSourceCard(
    onGitHubClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    AppGlassCard(modifier = modifier.clip(RoundedCornerShape(16.dp))) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    stringResource(Res.string.about_open_source_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                )
                Text(
                    stringResource(Res.string.about_open_source_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = AppSpacing.md),
                )
            }
            Row(
                modifier =
                    Modifier
                        .padding(top = AppSpacing.xl)
                        .clickable(onClick = onGitHubClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    stringResource(Res.string.about_view_github),
                    style = AppLabelCapsTextStyle.copy(fontWeight = FontWeight.Black),
                    color = colors.primary,
                )
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun AboutBuildingTogetherCard(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth().height(240.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.3f)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    colors.surfaceContainerHigh.copy(alpha = 0.45f),
                                    colors.surfaceContainerLow,
                                    colors.background,
                                ),
                        ),
                    ),
        ) {
            Column(
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(AppSpacing.lg),
            ) {
                Text(
                    stringResource(Res.string.about_building_together),
                    style = AppLabelCapsTextStyle.copy(fontWeight = FontWeight.Black),
                    color = colors.secondary,
                )
                Text(
                    stringResource(Res.string.about_free_open_source),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = AppSpacing.xs),
                )
            }
        }
    }
}

@Composable
fun AboutLicensesSection(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    var showAll by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = colors.primary, modifier = Modifier.size(26.dp))
            Text(
                stringResource(Res.string.about_licenses_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns =
                when {
                    maxWidth >= 1024.dp -> 4
                    maxWidth >= 640.dp -> 2
                    else -> 1
                }
            val rows = AboutContent.featuredLicenses.chunked(columns)
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                rows.forEach { rowLicenses ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        rowLicenses.forEach { license ->
                            LicenseCard(license = license, modifier = Modifier.weight(1f))
                        }
                        repeat(columns - rowLicenses.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Row(
                modifier = Modifier.clickable { showAll = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    stringResource(Res.string.about_view_all_licenses),
                    style = AppLabelCapsTextStyle.copy(fontWeight = FontWeight.Black),
                    color = colors.primary,
                )
                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
            }
        }
    }

    if (showAll) {
        AlertDialog(
            onDismissRequest = { showAll = false },
            title = { Text(stringResource(Res.string.about_licenses_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    AboutContent.allLicenses.forEach { license ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(license.name, fontWeight = FontWeight.Bold)
                                Text(license.license, style = AppCodeSmallTextStyle, color = colors.secondary)
                            }
                            Text(
                                license.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAll = false }) {
                    Text(stringResource(Res.string.common_close))
                }
            },
        )
    }
}

@Composable
private fun LicenseCard(
    license: OpenSourceLicense,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.2f)),
    ) {
        Column(Modifier.padding(AppSpacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(license.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(license.license, style = AppCodeSmallTextStyle, color = colors.secondary)
            }
            Text(
                license.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        }
    }
}

@Composable
fun AboutAcknowledgmentsSection(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    AppGlassCard(modifier = modifier.clip(RoundedCornerShape(16.dp))) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val stacked = maxWidth < 520.dp
                if (stacked) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                        Text(
                            stringResource(Res.string.about_acknowledgments),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        ContributorAvatarRow()
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(Res.string.about_acknowledgments),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        ContributorAvatarRow()
                    }
                }
            }

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val cols = if (maxWidth >= 1024.dp) 4 else 2
                val rows = AboutContent.contributorOrgs.chunked(cols)
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
                    rows.forEach { rowOrgs ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                        ) {
                            rowOrgs.forEach { org ->
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        org.handle,
                                        style = appCodeTextStyle().copy(fontWeight = FontWeight.Bold),
                                        color = colors.primary,
                                    )
                                    Text(
                                        org.tag.uppercase(),
                                        style = AppLabelCapsTextStyle,
                                        color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(top = AppSpacing.xs),
                                    )
                                }
                            }
                            repeat(cols - rowOrgs.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributorAvatarRow() {
    val colors = MaterialTheme.colorScheme
    val initials = listOf("A", "B", "C")
    Row(verticalAlignment = Alignment.CenterVertically) {
        initials.forEachIndexed { index, label ->
            Box(
                modifier =
                    Modifier
                        .offset(x = (-12 * index).dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceContainerHigh)
                        .border(2.dp, colors.surfaceContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = AppCodeSmallTextStyle,
                    color = colors.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Box(
            modifier =
                Modifier
                    .offset(x = (-12 * initials.size).dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceContainerLowest)
                    .border(2.dp, colors.surfaceContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "+${AboutContent.extraContributorCount}",
                style = AppCodeSmallTextStyle.copy(fontWeight = FontWeight.Bold),
                color = colors.onSurface,
            )
        }
    }
}

@Composable
fun AboutFooterSection(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(
            Icons.Default.Favorite,
            contentDescription = null,
            tint = colors.error,
            modifier = Modifier.size(20.dp),
        )
        Text(
            stringResource(Res.string.about_footer_tagline),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(Res.string.about_footer_built, stringResource(Res.string.about_kotlin_version)),
            style = AppLabelCapsTextStyle.copy(fontWeight = FontWeight.Black),
            color = colors.onSurfaceVariant.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
        )
    }
}
