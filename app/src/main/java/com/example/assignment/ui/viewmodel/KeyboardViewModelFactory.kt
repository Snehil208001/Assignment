// snehil208001/assignment/Assignment-a035ef2b75fea022e6b4681c61cef0498c371724/app/src/main/java/com/example/assignment/ui/viewmodel/KeyboardViewModelFactory.kt
package com.example.assignment.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.assignment.data.repository.GroqRepository

class KeyboardViewModelFactory(private val groqRepository: GroqRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeyboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KeyboardViewModel(groqRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}