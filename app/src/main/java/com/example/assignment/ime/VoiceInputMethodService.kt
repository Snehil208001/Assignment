// snehil208001/assignment/Assignment-a035ef2b75fea022e6b4681c61cef0498c371724/app/src/main/java/com/example/assignment/ime/VoiceInputMethodService.kt
package com.example.assignment.ime

import android.inputmethodservice.InputMethodService
import android.util.Log
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
import com.example.assignment.ui.viewmodel.KeyboardViewModel
import com.example.assignment.ui.viewmodel.KeyboardViewModelFactory
import com.example.assignment.util.AudioRecorder
import kotlinx.coroutines.*

class VoiceInputMethodService : InputMethodService(),
    ViewModelStoreOwner,
    LifecycleOwner,
    SavedStateRegistryOwner {

    // --- ViewModel and Dependencies ---
    private lateinit var viewModel: KeyboardViewModel
    private lateinit var audioRecorder: AudioRecorder

    // Coroutine scope for observing ViewModel events
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // --- Lifecycle Implementations ---
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        Log.d("VoiceIME", "onCreate called")
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        // Initialize dependencies and ViewModel
        audioRecorder = AudioRecorder(this)
        val groqRepository = GroqRepository()
        val viewModelFactory = KeyboardViewModelFactory(groqRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[KeyboardViewModel::class.java]

        // Observe transcription results from the ViewModel
        serviceScope.launch {
            viewModel.transcriptionResult.collect { text ->
                commitTextToInput(text)
            }
        }
        Log.d("VoiceIME", "onCreate completed")
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        return ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@VoiceInputMethodService)
            setViewTreeViewModelStoreOwner(this@VoiceInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@VoiceInputMethodService)

            setContent {
                val state by viewModel.uiState.collectAsState()
                KeyboardView(
                    uiState = state,
                    onPress = ::startRecording,
                    onRelease = ::stopRecordingAndTranscribe
                )
            }
        }
    }

    private fun startRecording() {
        Log.d("VoiceIME", "Starting recording")
        try {
            audioRecorder.start()
            viewModel.onRecordingStarted()
        } catch (e: Exception) {
            Log.e("VoiceIME", "Error starting recording", e)
        }
    }

    private fun stopRecordingAndTranscribe() {
        Log.d("VoiceIME", "Stopping recording")
        try {
            audioRecorder.stop()
            viewModel.onRecordingStopped(audioRecorder.getAudioFile())
        } catch (e: Exception) {
            Log.e("VoiceIME", "Error in stopRecordingAndTranscribe", e)
        }
    }

    private fun commitTextToInput(text: String) {
        val inputConnection = currentInputConnection
        if (inputConnection != null) {
            inputConnection.commitText(text, 1)
        } else {
            Log.e("VoiceIME", "InputConnection is null, cannot commit text.")
        }
    }

    // --- Standard Service Lifecycle Methods ---
    override fun onBindInput() {
        super.onBindInput()
        Log.d("VoiceIME", "onBindInput called")
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceScope.cancel() // Cancel the scope
        viewModelStore.clear()
    }
}