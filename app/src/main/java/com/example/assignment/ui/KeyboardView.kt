package com.example.assignment.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.assignment.ui.state.KeyboardUiState

@Composable
fun KeyboardView(
    uiState: KeyboardUiState,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp) // Standard keyboard height
            .background(Color(0xFF212121)),
        contentAlignment = Alignment.Center
    ) {
        val buttonColor = when (uiState) {
            is KeyboardUiState.Recording -> Color.Red
            is KeyboardUiState.Processing -> Color.Gray
            else -> MaterialTheme.colorScheme.primary
        }

        Button(
            onClick = { /* This will be handled by pointerInput */ },
            modifier = Modifier
                .size(100.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onPress() // Action on press down
                            tryAwaitRelease() // Wait for release
                            onRelease() // Action on release
                        }
                    )
                },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
        ) {
            when (uiState) {
                is KeyboardUiState.Idle -> Text("Hold")
                is KeyboardUiState.Recording -> Text("...")
                is KeyboardUiState.Processing -> CircularProgressIndicator(color = Color.White)
                is KeyboardUiState.Error -> Text("Retry")
            }
        }
    }
}