#!/usr/bin/env kotlin

package com.aryan.aura.util

import com.google.ai.client.generativeai.GenerativeModel

class AICoreHelper {
    // This calls the Gemini Nano model on the phone
    private val generativeModel = GenerativeModel(
        modelName = "gemini-nano",
        apiKey = "LOCAL_MODEL" // Nano doesn't need an API key
    )

    suspend fun getEmpatheticSummary(notifications: String): String {
        val prompt = "You are Aura, an empathetic AI. Summarize these messages to reduce stress: $notifications"
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Take a breath. Nothing urgent right now."
        } catch (e: Exception) {
            "Error sensing vibes."
        }
    }
}