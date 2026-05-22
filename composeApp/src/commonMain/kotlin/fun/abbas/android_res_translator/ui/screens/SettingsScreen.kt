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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `fun`.abbas.android_res_translator.ui.screens.settings.SettingsDangerZone
import `fun`.abbas.android_res_translator.ui.screens.settings.SettingsPageHeader
import `fun`.abbas.android_res_translator.ui.screens.settings.SettingsProvidersGrid
import `fun`.abbas.android_res_translator.ui.screens.settings.SettingsStrategiesCard
import `fun`.abbas.android_res_translator.ui.screens.settings.buildProviderSections
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsRepository
import `fun`.abbas.android_res_translator.ui.settings.AppSettingsSnapshot
import `fun`.abbas.android_res_translator.ui.theme.AppSpacing
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: AppSettingsRepository,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var draft by remember { mutableStateOf(AppSettingsSnapshot()) }

    LaunchedEffect(repository) {
        draft = repository.snapshot.value
    }

    val persist: (AppSettingsSnapshot) -> Unit = { next ->
        draft = next
        scope.launch { repository.replaceAll(next) }
    }

    val scroll = rememberScrollState()
    val providers = buildProviderSections(draft, persist)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = 896.dp)
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(AppSpacing.gutter),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            SettingsPageHeader()
            SettingsProvidersGrid(sections = providers)
            SettingsStrategiesCard(
                draft = draft,
                onDraft = persist,
            )
            SettingsDangerZone()
            Spacer(Modifier.height(AppSpacing.lg))
        }
    }
}
