package `fun`.abbas.android_res_translator.ui.screens.fileeditor

import `fun`.abbas.android_res_translator.core.resources.model.StringResourceFile
import `fun`.abbas.android_res_translator.core.resources.xml.StringsXmlCodec
import `fun`.abbas.android_res_translator.core.translation.TranslationDebugLog
import `fun`.abbas.android_res_translator.core.translation.TranslationOutcome
import `fun`.abbas.android_res_translator.core.translation.vendors.LingvanexLanguageSupport
import `fun`.abbas.android_res_translator.persistence.TranslationProjectFileStore
import `fun`.abbas.android_res_translator.ui.TranslationServices
import `fun`.abbas.android_res_translator.ui.i18n.AppLocale
import `fun`.abbas.android_res_translator.ui.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FileEditorController(
    private val services: TranslationServices,
    private val scope: CoroutineScope,
    fileName: String,
    filePath: String,
    sourceLang: String,
    targetLang: String,
    private val sourceXml: String,
    initialSession: FileEditorSessionSnapshot? = null,
    private val resultPath: String? = null,
    private val onPersistResult: (() -> Unit)? = null,
) {
    private var parsedFile: StringResourceFile = StringResourceFile()
    private var translationJob: Job? = null
    private var flushResultJob: Job? = null
    private var preferredVendorName: String? = null
    var uiLocale: AppLocale = AppLocale.En

    /** 与设置/首页所选翻译引擎一致，见 [ActiveTranslationEngine.vendorName]。 */
    fun setPreferredVendorName(vendorName: String?) {
        preferredVendorName = vendorName?.trim()?.takeIf { it.isNotEmpty() }
        TranslationDebugLog.log(
            "FileEditor",
            "setPreferredVendorName=$preferredVendorName",
        )
    }

    private val _state =
        MutableStateFlow(
            FileEditorState(
                fileName = fileName,
                filePath = filePath,
                sourceLang = sourceLang,
                targetLang = targetLang,
            ),
        )
    val state: StateFlow<FileEditorState> = _state.asStateFlow()

    init {
        if (initialSession != null && initialSession.entries.isNotEmpty()) {
            restoreSession(initialSession)
        } else {
            load(sourceXml)
        }
    }

    fun restoreSession(snapshot: FileEditorSessionSnapshot) {
        parsedFile =
            runCatching { StringsXmlCodec.parse(sourceXml) }
                .getOrElse { StringResourceFile() }
        _state.update {
            it.copy(
                entries =
                    snapshot.entries.map { entry ->
                        if (entry.status is EntryStatus.Translating) {
                            entry.copy(status = EntryStatus.Pending)
                        } else {
                            entry
                        }
                    },
                keyFilter = snapshot.keyFilter,
                isPaused = snapshot.isPaused,
                isRunning = false,
                exportMessage = null,
            )
        }
    }

    fun load(sourceXml: String) {
        parsedFile =
            runCatching { StringsXmlCodec.parse(sourceXml) }
                .getOrElse { StringResourceFile() }
        val entries =
            parsedFile.strings.values.map { entry ->
                XmlEntryUi(
                    key = entry.name,
                    sourceText = entry.value,
                    targetText = null,
                    status = if (entry.translatable) EntryStatus.Pending else EntryStatus.Completed,
                    translatable = entry.translatable,
                )
            }
        _state.update {
            it.copy(
                entries = entries,
                exportMessage = null,
                isPaused = false,
                isRunning = false,
            )
        }
    }

    fun setKeyFilter(query: String) {
        _state.update { it.copy(keyFilter = query) }
    }

    fun setTranslationLanguages(
        sourceLang: String,
        targetLang: String,
    ) {
        _state.update {
            it.copy(
                sourceLang = sourceLang,
                targetLang = targetLang,
            )
        }
        scheduleResultFlush()
    }

    fun onTranslationButtonClick() {
        val current = _state.value
        when {
            current.isRunning && !current.isPaused -> {
                _state.update { it.copy(isPaused = true) }
                translationJob?.cancel()
                translationJob = null
                _state.update { it.copy(isRunning = false) }
            }
            else -> {
                _state.update { it.copy(isPaused = false) }
                startTranslation()
            }
        }
    }

    /**
     * 在全部条目已成功翻译的前提下，清空译文并从头重新翻译。
     */
    fun retranslateAllFromScratch() {
        _state.update { s ->
            s.copy(
                entries =
                    s.entries.map { e ->
                        if (e.translatable && e.status is EntryStatus.Completed) {
                            e.copy(status = EntryStatus.Pending, targetText = null)
                        } else {
                            e
                        }
                    },
                isPaused = false,
                exportMessage = null,
            )
        }
        startTranslation()
    }

    fun retryEntry(key: String) {
        updateEntry(key) { it.copy(status = EntryStatus.Pending, targetText = null) }
        if (!_state.value.isRunning) {
            startTranslation()
        }
    }

    fun exportXml(): String {
        val current = _state.value
        val updatedStrings =
            parsedFile.strings.mapValues { (_, entry) ->
                val ui = current.entries.find { it.key == entry.name }
                val text =
                    when {
                        !entry.translatable -> entry.value
                        ui?.status is EntryStatus.Completed && ui.targetText?.isNotBlank() == true ->
                            ui.targetText.orEmpty()
                        else -> entry.value
                    }
                entry.copy(value = text)
            }
        return StringsXmlCodec.serialize(parsedFile.copy(strings = updatedStrings))
    }

    fun clearExportMessage() {
        _state.update { it.copy(exportMessage = null) }
    }

    fun setExportMessage(message: String) {
        _state.update { it.copy(exportMessage = message) }
    }

    private fun startTranslation() {
        translationJob?.cancel()
        translationJob =
            scope.launch {
                _state.update { it.copy(isRunning = true, isPaused = false) }
                val from = _state.value.sourceLang
                val to = _state.value.targetLang
                TranslationDebugLog.log(
                    "FileEditor",
                    "startTranslation preferredVendor=$preferredVendorName " +
                        "langs=$from->$to lingvanexTargetApi=${LingvanexLanguageSupport.resolveApiTargetCode(to)} " +
                        "lingvanexSupportsTarget=${LingvanexLanguageSupport.supportsAppTargetLanguage(to)}",
                )
                val port = services.segmentPort(preferredVendorName)
                try {
                    while (true) {
                        if (_state.value.isPaused) break
                        val nextKey =
                            _state.value.entries.firstOrNull { entry ->
                                entry.translatable && entry.status is EntryStatus.Pending
                            }?.key ?: break

                        updateEntry(nextKey) { it.copy(status = EntryStatus.Translating) }
                        val entry = _state.value.entries.first { it.key == nextKey }
                        val outcome =
                            port.translateSegment(
                                entry.sourceText,
                                _state.value.sourceLang,
                                _state.value.targetLang,
                            )
                        when (outcome) {
                            is TranslationOutcome.Ok ->
                                updateEntry(nextKey) {
                                    it.copy(
                                        status = EntryStatus.Completed,
                                        targetText = outcome.value.translatedText,
                                    )
                                }
                            is TranslationOutcome.Err ->
                                updateEntry(nextKey) {
                                    it.copy(
                                        status = EntryStatus.Error(outcome.failure.toUserMessage(uiLocale)),
                                        targetText = null,
                                    )
                                }
                        }
                    }
                } catch (_: CancellationException) {
                    // paused / disposed
                } finally {
                    _state.update { it.copy(isRunning = false) }
                    scheduleResultFlush(immediate = true)
                }
            }
    }

    private fun updateEntry(
        key: String,
        transform: (XmlEntryUi) -> XmlEntryUi,
    ) {
        _state.update { state ->
            state.copy(
                entries = state.entries.map { if (it.key == key) transform(it) else it },
            )
        }
        scheduleResultFlush()
    }

    private fun scheduleResultFlush(immediate: Boolean = false) {
        val path = resultPath ?: return
        flushResultJob?.cancel()
        flushResultJob =
            scope.launch {
                if (!immediate) delay(400)
                runCatching {
                    TranslationProjectFileStore.writeResultFromEditorState(
                        resultPath = path,
                        sourceXml = sourceXml,
                        state = _state.value,
                    )
                }
                onPersistResult?.invoke()
            }
    }

    fun dispose() {
        translationJob?.cancel()
        flushResultJob?.cancel()
    }
}
