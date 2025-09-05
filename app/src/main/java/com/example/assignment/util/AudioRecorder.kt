package com.example.assignment.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun getAudioFile(): File? = audioFile

    fun start() {
        val outputFile = File(context.cacheDir, "recording.m4a")
        audioFile = outputFile

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                // Handle error
            }
        }
    }

    fun stop() {
        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
            recorder = null
        } catch (e: Exception) {
            // Handle edge cases where stop() is called in an invalid state
        }
    }
}