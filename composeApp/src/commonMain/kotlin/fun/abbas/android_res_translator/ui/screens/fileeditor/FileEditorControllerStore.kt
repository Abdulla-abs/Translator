package `fun`.abbas.android_res_translator.ui.screens.fileeditor

import `fun`.abbas.android_res_translator.core.resources.planner.TranslationWorkflowMode
import `fun`.abbas.android_res_translator.ui.TranslationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 按 [key] 缓存 [FileEditorController]，离开 [FileEditorScreen] 后翻译任务继续在 [scope] 中运行。
 */
class FileEditorControllerStore(
    private val services: TranslationServices,
    private val scope: CoroutineScope,
) {
    private val controllers = mutableMapOf<String, FileEditorController>()
    private val stateObservers = mutableMapOf<String, Job>()

    fun getOrCreate(
        key: String,
        fileName: String,
        filePath: String,
        sourceLang: String,
        targetLang: String,
        sourceXml: String,
        initialSession: FileEditorSessionSnapshot? = null,
        resultPath: String? = null,
        workflowMode: TranslationWorkflowMode = TranslationWorkflowMode.FULL,
        targetBaselineXml: String? = null,
        forceTranslation: Boolean = false,
        onTargetBaselinePersist: ((String) -> Unit)? = null,
        onStateChange: ((FileEditorState) -> Unit)? = null,
        onPersistResult: (() -> Unit)? = null,
    ): FileEditorController {
        val existing = controllers[key]
        if (existing != null) {
            if (onStateChange != null) {
                observeState(key, existing, onStateChange)
            }
            return existing
        }
        val controller =
            FileEditorController(
                services = services,
                scope = scope,
                fileName = fileName,
                filePath = filePath,
                sourceLang = sourceLang,
                targetLang = targetLang,
                sourceXml = sourceXml,
                initialSession = initialSession,
                resultPath = resultPath,
                workflowMode = workflowMode,
                targetBaselineXml = targetBaselineXml,
                forceTranslation = forceTranslation,
                onTargetBaselinePersist = onTargetBaselinePersist,
                onPersistResult = onPersistResult,
            )
        controllers[key] = controller
        if (onStateChange != null) {
            observeState(key, controller, onStateChange)
        }
        return controller
    }

    private fun observeState(
        key: String,
        controller: FileEditorController,
        onStateChange: (FileEditorState) -> Unit,
    ) {
        stateObservers[key]?.cancel()
        stateObservers[key] =
            scope.launch {
                controller.state.collect { onStateChange(it) }
            }
    }

    fun currentState(key: String): FileEditorState? = controllers[key]?.state?.value

    fun release(key: String) {
        stateObservers.remove(key)?.cancel()
        controllers.remove(key)?.dispose()
    }
}
