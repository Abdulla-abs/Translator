package `fun`.abbas.android_res_translator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.platform.rememberOpenExternalUrlHandler
import `fun`.abbas.android_res_translator.ui.screens.about.AboutAcknowledgmentsSection
import `fun`.abbas.android_res_translator.ui.screens.about.AboutContent
import `fun`.abbas.android_res_translator.ui.screens.about.AboutFooterSection
import `fun`.abbas.android_res_translator.ui.screens.about.AboutHeroSection
import `fun`.abbas.android_res_translator.ui.screens.about.AboutLicensesSection
import `fun`.abbas.android_res_translator.ui.screens.about.AboutProjectSection
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val openUrl = rememberOpenExternalUrlHandler()
    val scroll = rememberScrollState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = 1024.dp)
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(AppSpacing.gutter),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
        ) {
            AboutHeroSection()
            AboutProjectSection(onGitHubClick = { openUrl(AboutContent.GITHUB_URL) })
            AboutLicensesSection()
            AboutAcknowledgmentsSection()
            AboutFooterSection()
            Spacer(Modifier.height(88.dp))
        }
    }
}
