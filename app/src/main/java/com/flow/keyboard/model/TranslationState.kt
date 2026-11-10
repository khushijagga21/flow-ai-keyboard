package com.flow.keyboard.model

enum class AiMode {
    TRANSLATE_HINDI_TO_ENGLISH,
    POLISH_ENGLISH_GRAMMAR
}

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    data class Processing(val recognizedText: String) : VoiceState()
    data class Success(val originalText: String, val translatedText: String) : VoiceState()
    data class Error(val message: String) : VoiceState()
}

// Gemini API Request models
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = GeminiGenerationConfig(
        temperature = 0.2f,
        maxOutputTokens = 1000
    )
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val temperature: Float,
    val maxOutputTokens: Int
)

// Gemini API Response models
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContentResponse?
)

data class GeminiContentResponse(
    val parts: List<GeminiPartResponse>?
)

data class GeminiPartResponse(
    val text: String?
)
