// snehil208001/assignment/Assignment-a035ef2b75fea022e6b4681c61cef0498c371724/app/src/main/java/com/example/assignment/ui/viewmodel/KeyboardViewModel.kt
package com.example.assignment.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment.data.repository.GroqRepository
import com.example.assignment.ui.state.KeyboardUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class KeyboardViewModel(private val groqRepository: GroqRepository) : ViewModel() {

    // StateFlow to hold the UI state for the keyboard view
    private val _uiState = MutableStateFlow<KeyboardUiState>(KeyboardUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // SharedFlow to send one-time events, like the transcribed text, to the service
    private val _transcriptionResult = MutableSharedFlow<String>()
    val transcriptionResult = _transcriptionResult.asSharedFlow()

    fun onRecordingStarted() {
        _uiState.update { KeyboardUiState.Recording }
    }

    fun onRecordingStopped(audioFile: File?) {
        if (audioFile == null) {
            _uiState.update { KeyboardUiState.Error("No audio recorded") }
            return
        }

        _uiState.update { KeyboardUiState.Processing }

        viewModelScope.launch(Dispatchers.IO) {
            val result = groqRepository.transcribe(audioFile)
            result.onSuccess { transcribedText ->
                if (transcribedText.isNotBlank()) {
                    _transcriptionResult.emit("$transcribedText ")
                }
                _uiState.update { KeyboardUiState.Idle }
            }.onFailure { exception ->
                _uiState.update { KeyboardUiState.Error("Transcription failed: ${exception.message}") }
            }
        }
    }
}