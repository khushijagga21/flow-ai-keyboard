package com.flow.keyboard.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class VoiceRecognitionHelper(
    private val context: Context,
    private val listener: VoiceCallback
) : RecognitionListener {

    interface VoiceCallback {
        fun onListeningStarted()
        fun onListeningFinished()
        fun onSpeechRecognized(hindiText: String)
        fun onError(errorMessage: String)
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening: Boolean = false

    companion object {
        private const val TAG = "VoiceRecognition"
    }

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        if (isListening) {
            stopListening()
            return
        }

        if (!isAvailable()) {
            listener.onError("Speech recognition not available on this device.")
            return
        }

        try {
            destroyRecognizer()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceRecognitionHelper)
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                // Set Hindi as primary language, with English fallback
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-IN", "hi-Latn"))
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }

            speechRecognizer?.startListening(intent)
            isListening = true
            listener.onListeningStarted()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognizer", e)
            isListening = false
            listener.onError(e.message ?: "Failed to start mic")
        }
    }

    fun stopListening() {
        if (!isListening) return
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recognizer", e)
        }
        isListening = false
    }

    fun destroyRecognizer() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying recognizer", e)
        }
        speechRecognizer = null
        isListening = false
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "Speech recognizer ready")
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "Beginning of speech detected")
    }

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        Log.d(TAG, "End of speech detected")
        isListening = false
        listener.onListeningFinished()
    }

    override fun onError(error: Int) {
        isListening = false
        listener.onListeningFinished()
        val errorMsg = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking closer."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy. Try again."
            SpeechRecognizer.ERROR_SERVER -> "Google Speech Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard in time"
            else -> "Speech recognition error ($error)"
        }
        listener.onError(errorMsg)
    }

    override fun onResults(results: Bundle?) {
        isListening = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim()
        if (!text.isNullOrBlank()) {
            listener.onSpeechRecognized(text)
        } else {
            listener.onError("Could not recognize Hindi speech.")
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
