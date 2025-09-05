package com.example.assignment.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.assignment.data.repository.GroqRepository
import com.example.assignment.ui.KeyboardView
import com.example.assignment.ui.state.KeyboardUiState
import com.example.assignment.util.AudioRecorder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VoiceInputMethodService : InputMethodService(),
    ViewModelStoreOwner,
    LifecycleOwner,
    SavedStateRegistryOwner {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val _uiState = MutableStateFlow<KeyboardUiState>(KeyboardUiState.Idle)
    private val uiState = _uiState.asStateFlow()

    private lateinit var audioRecorder: AudioRecorder
    private lateinit var groqRepository: GroqRepository

    // LifecycleOwner Implementation
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    // ViewModelStoreOwner Implementation
    override val viewModelStore = ViewModelStore()

    // SavedStateRegistryOwner Implementation
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("VoiceIME", "onCreate called")
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        audioRecorder = AudioRecorder(this)
        groqRepository = GroqRepository()
        android.util.Log.d("VoiceIME", "onCreate completed")
    }

    override fun onCreateInputView(): View? {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        return try {
            ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@VoiceInputMethodService)
                setViewTreeViewModelStoreOwner(this@VoiceInputMethodService)
                setViewTreeSavedStateRegistryOwner(this@VoiceInputMethodService)

                setContent {
                    val state by uiState.collectAsState()
                    KeyboardView(
                        uiState = state,
                        onPress = ::startRecording,
                        onRelease = ::stopRecordingAndTranscribe
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("VoiceIME", "Error creating input view", e)
            null
        }
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        android.util.Log.d("VoiceIME", "onStartInputView called")
    }

    override fun onBindInput() {
        super.onBindInput()
        android.util.Log.d("VoiceIME", "onBindInput called")
    }

    private fun startRecording() {
        android.util.Log.d("VoiceIME", "Starting recording")
        _uiState.update { KeyboardUiState.Recording }
        try {
            audioRecorder.start()
        } catch (e: Exception) {
            android.util.Log.e("VoiceIME", "Error starting recording", e)
            _uiState.update { KeyboardUiState.Error("Failed to start recording") }
        }
    }

    private fun stopRecordingAndTranscribe() {
        android.util.Log.d("VoiceIME", "Stopping recording")
        try {
            audioRecorder.stop()
            val audioFile = audioRecorder.getAudioFile()

            if (audioFile == null) {
                android.util.Log.e("VoiceIME", "Audio file is null")
                _uiState.update { KeyboardUiState.Error("No audio recorded") }
                return
            }

            _uiState.update { KeyboardUiState.Processing }

            serviceScope.launch(Dispatchers.IO) {
                val result = groqRepository.transcribe(audioFile)
                withContext(Dispatchers.Main) {
                    result.onSuccess { transcribedText ->
                        android.util.Log.d("VoiceIME", "Transcribed: $transcribedText")
                        if (transcribedText.isNotBlank()) {
                            val inputConnection = currentInputConnection
                            if (inputConnection != null) {
                                inputConnection.commitText("$transcribedText ", 1)
                            } else {
                                android.util.Log.e("VoiceIME", "InputConnection is null")
                            }
                        }
                        _uiState.update { KeyboardUiState.Idle }
                    }.onFailure { exception ->
                        android.util.Log.e("VoiceIME", "Transcription failed", exception)
                        _uiState.update { KeyboardUiState.Error("Transcription failed: ${exception.message}") }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("VoiceIME", "Error in stopRecordingAndTranscribe", e)
            _uiState.update { KeyboardUiState.Error("Error: ${e.message}") }
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceJob.cancel()
        viewModelStore.clear()
    }
}