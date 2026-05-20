package `fun`.abbas.android_res_translator.ui.screens.main

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryRecentXmlProjectRepository(
    private val maxItems: Int = 10,
) {
    private val _projects = MutableStateFlow<List<RecentXmlProject>>(emptyList())
    val projects: StateFlow<List<RecentXmlProject>> = _projects.asStateFlow()

    fun addOrUpdate(project: RecentXmlProject) {
        val without = _projects.value.filterNot { it.id == project.id }
        _projects.value = (listOf(project) + without).take(maxItems)
    }
}
