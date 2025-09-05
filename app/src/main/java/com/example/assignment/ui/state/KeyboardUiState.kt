package com.example.assignment.ui.state

sealed class KeyboardUiState {
    data object Idle : KeyboardUiState()
    data object Recording : KeyboardUiState()
    data object Processing : KeyboardUiState()
    data class Error(val message: String) : KeyboardUiState()
}