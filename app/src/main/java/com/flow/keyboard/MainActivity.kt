package com.flow.keyboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.flow.keyboard.service.GeminiTranslationService
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var geminiService: GeminiTranslationService
    private val RECORD_AUDIO_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        geminiService = GeminiTranslationService(this)

        checkPermissions()
        setupListeners()
        loadSavedApiKey()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST_CODE
            )
        }
    }

    private fun setupListeners() {
        val btnEnable = findViewById<Button>(R.id.btn_enable_ime)
        val btnSelect = findViewById<Button>(R.id.btn_select_ime)
        val btnSaveKey = findViewById<Button>(R.id.btn_save_key)
        val etApiKey = findViewById<TextInputEditText>(R.id.et_api_key)

        // Step 1: Open system Input Method settings
        btnEnable.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivity(intent)
        }

        // Step 2: Show keyboard picker dialog
        btnSelect.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showInputMethodPicker()
        }

        // Step 3: Save Gemini API Key
        btnSaveKey.setOnClickListener {
            val key = etApiKey.text?.toString()?.trim().orEmpty()
            if (key.isBlank()) {
                Toast.makeText(this, "Please enter a valid API key.", Toast.LENGTH_SHORT).show()
            } else {
                geminiService.saveApiKey(key)
                Toast.makeText(this, "Gemini API Key saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSavedApiKey() {
        val savedKey = geminiService.getApiKey()
        if (savedKey.isNotBlank()) {
            val etApiKey = findViewById<TextInputEditText>(R.id.et_api_key)
            etApiKey.setText(savedKey)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Microphone permission is required for Voice-to-English.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
