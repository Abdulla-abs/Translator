package `fun`.abbas.android_res_translator.ui.screens.main

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun LanguagePickerListScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
)
