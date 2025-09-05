package com.example.assignment.data.repository

import com.example.assignment.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class GroqRepository {

    suspend fun transcribe(
        audioFile: File,
        language: String = "en",
        temperature: String = "0.0",
        model: String = "whisper-large-v3"
    ): Result<String> {
        return try {
            // Detect file type (default to m4a if unknown)
            val mimeType = when (audioFile.extension.lowercase()) {
                "wav" -> "audio/wav"
                "mp4" -> "audio/mp4"
                else -> "audio/m4a"
            }

            val filePart = MultipartBody.Part.createFormData(
                "file",
                audioFile.name,
                audioFile.asRequestBody(mimeType.toMediaTypeOrNull())
            )

            val modelBody = model.toRequestBody("text/plain".toMediaTypeOrNull())
            val tempBody = temperature.toRequestBody("text/plain".toMediaTypeOrNull())
            val langBody = language.toRequestBody("text/plain".toMediaTypeOrNull())

            val token = "Bearer ${BuildConfig.GROQ_API_KEY}"

            val response = RetrofitInstance.api.transcribeAudio(
                token,
                filePart,
                modelBody,
                tempBody,
                langBody
            )

            Result.success(response.text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
