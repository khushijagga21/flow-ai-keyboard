package com.flow.keyboard.ui

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.flow.keyboard.R

class KeyboardViewManager(
    private val context: Context,
    private val listener: KeyActionListener
) {

    interface KeyActionListener {
        fun onTextInput(text: String)
        fun onDelete()
        fun onEnter()
        fun onSpace()
    }

    private var isShifted = false
    private var isSymbolsMode = false
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private val row1Letters = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    private val row2Letters = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    private val row3Letters = listOf("z", "x", "c", "v", "b", "n", "m")

    private val row1Symbols = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    private val row2Symbols = listOf("@", "#", "$", "%", "&", "-", "+", "(", ")")
    private val row3Symbols = listOf("*", "\"", "'", ":", ";", "!", "?")

    private val letterButtons = mutableListOf<Button>()
    private var shiftButton: Button? = null
    private var symbolsButton: Button? = null

    fun setupKeyboard(
        row1: LinearLayout,
        row2: LinearLayout,
        row3: LinearLayout,
        row4: LinearLayout
    ) {
        letterButtons.clear()
        setupRow1(row1)
        setupRow2(row2)
        setupRow3(row3)
        setupRow4(row4)
    }

    private fun setupRow1(row: LinearLayout) {
        row.removeAllViews()
        val keys = if (isSymbolsMode) row1Symbols else row1Letters
        for (char in keys) {
            val keyView = createKeyButton(char, 1f) {
                val inputChar = if (isShifted) char.uppercase() else char
                listener.onTextInput(inputChar)
                if (isShifted) {
                    toggleShift()
                }
            }
            letterButtons.add(keyView)
            row.addView(keyView)
        }
    }

    private fun setupRow2(row: LinearLayout) {
        row.removeAllViews()
        val keys = if (isSymbolsMode) row2Symbols else row2Letters
        for (char in keys) {
            val keyView = createKeyButton(char, 1f) {
                val inputChar = if (isShifted) char.uppercase() else char
                listener.onTextInput(inputChar)
                if (isShifted) {
                    toggleShift()
                }
            }
            letterButtons.add(keyView)
            row.addView(keyView)
        }
    }

    private fun setupRow3(row: LinearLayout) {
        row.removeAllViews()

        // Shift / Caps key
        val shiftKey = createActionKeyButton(if (isShifted) "⇪" else "⇧", 1.5f) {
            toggleShift()
        }
        shiftButton = shiftKey
        row.addView(shiftKey)

        // Middle letters/symbols
        val keys = if (isSymbolsMode) row3Symbols else row3Letters
        for (char in keys) {
            val keyView = createKeyButton(char, 1f) {
                val inputChar = if (isShifted) char.uppercase() else char
                listener.onTextInput(inputChar)
                if (isShifted) {
                    toggleShift()
                }
            }
            letterButtons.add(keyView)
            row.addView(keyView)
        }

        // Backspace key
        val backspaceKey = createActionKeyButton("⌫", 1.5f) {
            listener.onDelete()
        }
        row.addView(backspaceKey)
    }

    private fun setupRow4(row: LinearLayout) {
        row.removeAllViews()

        // Switch to Symbols / ABC
        val symKey = createActionKeyButton(if (isSymbolsMode) "ABC" else "?123", 1.5f) {
            toggleSymbols()
        }
        symbolsButton = symKey
        row.addView(symKey)

        // Comma key
        val commaKey = createKeyButton(",", 1f) {
            listener.onTextInput(",")
        }
        row.addView(commaKey)

        // Spacebar
        val spaceKey = createActionKeyButton("English (Hindi AI)", 4.5f) {
            listener.onSpace()
        }
        row.addView(spaceKey)

        // Period key
        val dotKey = createKeyButton(".", 1f) {
            listener.onTextInput(".")
        }
        row.addView(dotKey)

        // Enter key
        val enterKey = createActionKeyButton("↵", 1.5f) {
            listener.onEnter()
        }
        enterKey.setBackgroundColor(ContextCompat.getColor(context, R.color.primary))
        enterKey.setTextColor(Color.WHITE)
        row.addView(enterKey)
    }

    private fun toggleShift() {
        isShifted = !isShifted
        shiftButton?.text = if (isShifted) "⇪" else "⇧"
        shiftButton?.setBackgroundColor(
            if (isShifted) ContextCompat.getColor(context, R.color.primary)
            else ContextCompat.getColor(context, R.color.key_action_bg)
        )
        refreshLetterDisplay()
    }

    private fun toggleSymbols() {
        isSymbolsMode = !isSymbolsMode
        isShifted = false
        // Re-setup rows with symbols or letters
        (letterButtons.firstOrNull()?.parent?.parent as? LinearLayout)?.let { root ->
            val r1 = root.findViewById<LinearLayout>(R.id.row_1)
            val r2 = root.findViewById<LinearLayout>(R.id.row_2)
            val r3 = root.findViewById<LinearLayout>(R.id.row_3)
            val r4 = root.findViewById<LinearLayout>(R.id.row_4)
            if (r1 != null && r2 != null && r3 != null && r4 != null) {
                setupKeyboard(r1, r2, r3, r4)
            }
        }
    }

    private fun refreshLetterDisplay() {
        if (isSymbolsMode) return
        var idx = 0
        val allLetters = row1Letters + row2Letters + row3Letters
        for (btn in letterButtons) {
            if (idx < allLetters.size) {
                val char = allLetters[idx]
                btn.text = if (isShifted) char.uppercase() else char
                idx++
            }
        }
    }

    private fun createKeyButton(text: String, weight: Float, onClick: () -> Unit): Button {
        return Button(context).apply {
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
            params.setMargins(2, 2, 2, 2)
            layoutParams = params
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            setTextColor(ContextCompat.getColor(context, R.color.key_text))
            setBackgroundResource(R.drawable.btn_keyboard_key)
            gravity = Gravity.CENTER
            isAllCaps = false
            setPadding(0, 0, 0, 0)
            setOnClickListener {
                vibrateKey()
                onClick()
            }
        }
    }

    private fun createActionKeyButton(text: String, weight: Float, onClick: () -> Unit): Button {
        return Button(context).apply {
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
            params.setMargins(2, 2, 2, 2)
            layoutParams = params
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(ContextCompat.getColor(context, R.color.key_text))
            setBackgroundResource(R.drawable.btn_keyboard_action)
            gravity = Gravity.CENTER
            isAllCaps = false
            setPadding(0, 0, 0, 0)
            setOnClickListener {
                vibrateKey()
                onClick()
            }
        }
    }

    private fun vibrateKey() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {}
    }
}
