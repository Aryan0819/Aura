package com.aryan.aura.util

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel

class AICoreHelper(context: Context) {
    // This calls the Gemini Nano model on the phone
    private val generativeModel = GenerativeModel(
        modelName = "gemini-nano",
        apiKey = "LOCAL_MODEL" // Nano doesn't need an API key
    )

    suspend fun getEmpatheticSummary(notifications: String): String {
        val prompt = """
            You are Aura, an empathetic AI assistant. 
            Analyze this message: "$notifications"
            Provide a JSON response with exactly these fields:
            - summary: A calm, 1-sentence summary.
            - vibe: One word for the mood (e.g. Joyful, Urgent, Calm, Tense, Casual).
            - reply: A short, kind suggestion for a reply.
            - stress_score: A number from 1 to 10 (1 is peaceful, 10 is very stressful).
            
            Return ONLY the JSON.
        """.trimIndent()
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "{\"summary\": \"Aura is sensing...\", \"vibe\": \"Neutral\", \"reply\": \"Ok\", \"stress_score\": 1}"
        } catch (e: Exception) {
            "{\"summary\": \"Aura is sensing...\", \"vibe\": \"Unknown\", \"reply\": \"...\", \"stress_score\": 1}"
        }
    }
}
