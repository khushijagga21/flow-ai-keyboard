package com.flow.keyboard

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.flow.keyboard.service.GeminiTranslationService
import com.flow.keyboard.service.VoiceRecognitionHelper
import com.flow.keyboard.ui.KeyboardViewManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FlowKeyboardService : InputMethodService(),
    KeyboardViewManager.KeyActionListener,
    VoiceRecognitionHelper.VoiceCallback {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var geminiService: GeminiTranslationService
    private lateinit var voiceHelper: VoiceRecognitionHelper
    private lateinit var keyboardManager: KeyboardViewManager

    private var rootView: View? = null
    private var micButton: FrameLayout? = null
    private var micIcon: ImageView? = null
    private var statusText: TextView? = null
    private var isRecording = false

    companion object {
        private const val TAG = "FlowKeyboardService"
    }

    override fun onCreate() {
        super.onCreate()
        geminiService = GeminiTranslationService(this)
        voiceHelper = VoiceRecognitionHelper(this, this)
        keyboardManager = KeyboardViewManager(this, this)
    }

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)
        rootView = view

        // Find views
        micButton = view.findViewById(R.id.btn_voice_mic)
        micIcon = view.findViewById(R.id.iv_mic_icon)
        statusText = view.findViewById(R.id.tv_ai_status)
        val btnTranslate = view.findViewById<MaterialButton>(R.id.btn_quick_translate)
        val btnPolish = view.findViewById<MaterialButton>(R.id.btn_ai_polish)
        val btnSettings = view.findViewById<ImageView>(R.id.btn_settings)

        // Setup QWERTY key rows
        val row1 = view.findViewById<LinearLayout>(R.id.row_1)
        val row2 = view.findViewById<LinearLayout>(R.id.row_2)
        val row3 = view.findViewById<LinearLayout>(R.id.row_3)
        val row4 = view.findViewById<LinearLayout>(R.id.row_4)
        keyboardManager.setupKeyboard(row1, row2, row3, row4)

        // Mic Button Click (Hindi Voice -> English Output)
        micButton?.setOnClickListener {
            handleMicClick()
        }

        // Quick Translate Button (Hindi Text -> English Output)
        btnTranslate.setOnClickListener {
            handleTranslateText()
        }

        // AI Polish / Grammar Fix Button
        btnPolish.setOnClickListener {
            handlePolishGrammar()
        }

        // Settings Button (Opens Setup & Config Screen)
        btnSettings.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }

        return view
    }

    private fun handleMicClick() {
        if (isRecording) {
            voiceHelper.stopListening()
        } else {
            statusText?.text = "Listening to Hindi... Speak now!"
            voiceHelper.startListening()
        }
    }

    /**
     * Reads the current text before the cursor, translates Hindi/Hinglish to English,
     * and seamlessly replaces it in the active app.
     */
    private fun handleTranslateText() {
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(0)?.toString()

        val textToTranslate = if (!selectedText.isNullOrBlank()) {
            selectedText
        } else {
            ic.getTextBeforeCursor(500, 0)?.toString()
        }

        if (textToTranslate.isNullOrBlank()) {
            updateStatus("Type Hindi/Hinglish first, then tap Translate.")
            return
        }

        updateStatus("Translating to English with Gemini...")

        serviceScope.launch {
            val result = geminiService.translateHindiToEnglish(textToTranslate)
            result.onSuccess { englishTranslation ->
                if (!selectedText.isNullOrBlank()) {
                    // Replace selected text
                    ic.commitText(englishTranslation, 1)
                } else {
                    // Delete the original Hindi characters and commit translated English
                    ic.deleteSurroundingText(textToTranslate.length, 0)
                    ic.commitText(englishTranslation, 1)
                }
                updateStatus("Translated successfully!")
            }.onFailure { err ->
                Log.e(TAG, "Translation error", err)
                updateStatus(err.message ?: "Translation failed")
            }
        }
    }

    /**
     * Polishes the English text before cursor to fix grammar and phrasing.
     */
    private fun handlePolishGrammar() {
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(0)?.toString()

        val textToPolish = if (!selectedText.isNullOrBlank()) {
            selectedText
        } else {
            ic.getTextBeforeCursor(500, 0)?.toString()
        }

        if (textToPolish.isNullOrBlank()) {
            updateStatus("Type English first, then tap Polish.")
            return
        }

        updateStatus("Polishing grammar with AI...")

        serviceScope.launch {
            val result = geminiService.polishEnglishGrammar(textToPolish)
            result.onSuccess { polishedText ->
                if (!selectedText.isNullOrBlank()) {
                    ic.commitText(polishedText, 1)
                } else {
                    ic.deleteSurroundingText(textToPolish.length, 0)
                    ic.commitText(polishedText, 1)
                }
                updateStatus("Grammar polished!")
            }.onFailure { err ->
                Log.e(TAG, "Polish error", err)
                updateStatus(err.message ?: "Grammar fix failed")
            }
        }
    }

    private fun updateStatus(message: String) {
        statusText?.text = message
    }

    // --- KeyActionListener Callbacks ---

    override fun onTextInput(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    override fun onDelete() {
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(0)
        if (!selectedText.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    override fun onEnter() {
        currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    override fun onSpace() {
        currentInputConnection?.commitText(" ", 1)
    }

    // --- VoiceRecognitionHelper.VoiceCallback Callbacks ---

    override fun onListeningStarted() {
        isRecording = true
        micIcon?.setImageResource(R.drawable.ic_mic_active)
        updateStatus("Listening... Speak Hindi now!")
    }

    override fun onListeningFinished() {
        isRecording = false
        micIcon?.setImageResource(R.drawable.ic_mic)
    }

    override fun onSpeechRecognized(hindiText: String) {
        updateStatus("Hindi: \"$hindiText\" ➔ Translating...")

        // Translate the Hindi voice transcript to English using Gemini API
        serviceScope.launch {
            val result = geminiService.translateHindiToEnglish(hindiText)
            result.onSuccess { englishTranslation ->
                val ic = currentInputConnection
                if (ic != null) {
                    ic.commitText("$englishTranslation ", 1)
                    updateStatus("Typed in English!")
                } else {
                    updateStatus("No active text field found.")
                }
            }.onFailure { err ->
                Log.e(TAG, "Voice translation failed", err)
                // Fallback: If AI fails or no API key, commit recognized text directly
                currentInputConnection?.commitText("$hindiText ", 1)
                updateStatus("Translation failed, typed original text.")
            }
        }
    }

    override fun onError(errorMessage: String) {
        isRecording = false
        micIcon?.setImageResource(R.drawable.ic_mic)
        updateStatus(errorMessage)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroyRecognizer()
    }
}
