package com.example.assignment.data.repository

import com.google.gson.annotations.SerializedName

data class GroqResponse(
    @SerializedName("text") val text: String
)