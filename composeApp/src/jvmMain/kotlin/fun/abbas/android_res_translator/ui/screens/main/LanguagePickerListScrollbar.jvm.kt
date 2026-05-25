package `fun`.abbas.android_res_translator.ui.screens.main

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun LanguagePickerListScrollbar(
    listState: LazyListState,
    modifier: Modifier,
) {
    VerticalScrollbar(
        modifier = modifier,
        adapter = rememberScrollbarAdapter(scrollState = listState),
    )
}
