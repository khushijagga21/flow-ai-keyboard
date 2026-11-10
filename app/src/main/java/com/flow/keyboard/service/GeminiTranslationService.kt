package com.flow.keyboard.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.flow.keyboard.model.AiMode
import com.flow.keyboard.model.GeminiContent
import com.flow.keyboard.model.GeminiGenerationConfig
import com.flow.keyboard.model.GeminiPart
import com.flow.keyboard.model.GeminiRequest
import com.flow.keyboard.model.GeminiResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiTranslationService(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("flow_keyboard_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiTranslation"
        private const val BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
        const val PREF_GEMINI_API_KEY = "gemini_api_key"
    }

    fun getApiKey(): String {
        return prefs.getString(PREF_GEMINI_API_KEY, "") ?: ""
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString(PREF_GEMINI_API_KEY, key.trim()).apply()
    }

    /**
     * Translates Hindi/Hinglish speech or typed text into natural, grammatically correct English.
     */
    suspend fun translateHindiToEnglish(inputText: String): Result<String> {
        val prompt = """
            You are an expert Hindi-to-English translator and grammar engine.
            Input text is either written in Hindi (Devanagari), Hinglish (Roman Hindi), or mixed.
            Your task:
            1. Translate the exact meaning into natural, fluent, grammatically flawless English.
            2. Match the tone (conversational, professional, or casual based on context).
            3. Output ONLY the final translated English sentence. Do NOT enclose in quotation marks. Do NOT add greetings, prefixes, or explanations.

            Input: $inputText
        """.trimIndent()

        return executeGeminiPrompt(prompt)
    }

    /**
     * Rewrites and polishes English text to fix grammatical, punctuation, and phrasing errors.
     */
    suspend fun polishEnglishGrammar(inputText: String): Result<String> {
        val prompt = """
            You are an elite English grammar and writing assistant.
            Fix all grammatical, spelling, and phrasing errors in the following text to make it natural and professional.
            Output ONLY the corrected text. Do NOT enclose in quotation marks. Do NOT provide explanations.

            Input: $inputText
        """.trimIndent()

        return executeGeminiPrompt(prompt)
    }

    private suspend fun executeGeminiPrompt(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                Exception("Please set your Gemini API Key in the Flow AI Keyboard app settings.")
            )
        }

        try {
            val url = "$BASE_URL?key=$apiKey"
            val requestBodyObj = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt)),
                        role = "user"
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.2f,
                    maxOutputTokens = 1000
                )
            )

            val jsonBody = gson.toJson(requestBodyObj)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseString = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    Log.e(TAG, "API Error: ${response.code} -> $responseString")
                    return@withContext Result.failure(
                        Exception("Gemini API Error (${response.code}). Check your API Key.")
                    )
                }

                val geminiResponse = gson.fromJson(responseString, GeminiResponse::class.java)
                val output = geminiResponse.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?.trim()

                if (!output.isNullOrBlank()) {
                    Result.success(output)
                } else {
                    Result.failure(Exception("Empty translation response from Gemini."))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network or translation exception", e)
            Result.failure(e)
        }
    }
}
