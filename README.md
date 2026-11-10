# Flow AI Keyboard (Hindi to English Voice & Text)

Flow AI Keyboard is a system-wide custom Android Input Method (Keyboard) that integrates seamlessly into **WhatsApp, ChatGPT, Instagram, Gmail, Telegram**, and any other Android app.

It allows you to:
1. **Speak in Hindi** ➔ Instantly translates to natural, fluent, grammatically correct English and types it directly into the text field.
2. **Type in Hindi / Hinglish** ➔ Tap "Translate" to automatically replace it with proper English.
3. **AI Grammar Polish** ➔ Tap "Polish" to fix any typos, grammar mistakes, and tone in English sentences.
4. **Google Play Store Compliant** ➔ Built using standard Android `InputMethodService` (IME) architecture, fully conforming to Google Play policies.

---

## 🚀 How to Open and Run in Android Studio

1. **Open Project**:
   * Open **Android Studio** (Hedgehog, Iguana, Koala, Ladybug or newer).
   * Click **Open** and select:
     `C:\Users\Khushi Jagga\.gemini\antigravity\scratch\ai-translator-keyboard`
2. **Sync Gradle**:
   * Android Studio will automatically sync the Gradle dependencies (`OkHttp`, `Material`, `Coroutines`, etc.).
3. **Run on Device or Emulator**:
   * Connect your physical Android phone (recommended for microphone testing) or start an Emulator with Google Play services.
   * Click **Run (Shift + F10)**.

---

## 🛠️ Setup Steps in the App

When you launch the app for the first time:
1. **Step 1: Enable Keyboard** ➔ Tap "Enable in Settings" and toggle **Flow AI Keyboard** ON.
2. **Step 2: Select Keyboard** ➔ Tap "Select Keyboard" and choose **Flow AI Keyboard** as your active typing method.
3. **Step 3: Add Gemini API Key** ➔ 
   * Get a free API Key from [Google AI Studio](https://aistudio.google.com/app/apikey).
   * Paste it into the API Key box and click "Save API Key".
4. **Step 4: Live Test** ➔ Tap inside the test box. The keyboard will open!
   * Tap the **Mic icon** and speak in Hindi: *"कल सुबह मिलते हैं भाई"* ➔ Watch it type *"Let's meet tomorrow morning, bro."*
   * Type in Hinglish: *"Mujhe thodi der ho jayegi"* and tap **Translate** ➔ Watch it become *"I will be a bit late."*

---

## 📱 Testing in WhatsApp & ChatGPT

1. Open **WhatsApp** or **ChatGPT**.
2. Tap the chat input box.
3. Flow AI Keyboard appears.
4. **Voice to English**:
   * Tap the **Mic** button.
   * Speak in Hindi.
   * The text automatically translates and inserts into the message bar in flawless English!
5. **Text to English**:
   * Type Hindi or Roman Hinglish.
   * Tap **Translate** in the keyboard toolbar.
   * The Hindi text is replaced with fluent English.

---

## 📦 Google Play Store Submission Guide

This app uses standard Android APIs:
* `InputMethodService` (Standard Android Keyboard API)
* `android.permission.RECORD_AUDIO` (For voice recognition when mic is tapped)
* `android.permission.INTERNET` (For Gemini AI translation)

### Steps to Publish:
1. **Generate Signed Bundle**:
   * In Android Studio, go to **Build > Generate Signed Bundle / APK...**
   * Choose **Android App Bundle (.aab)**.
   * Create or choose your release Keystore.
2. **Google Play Console**:
   * Create an app listing: "Flow AI Keyboard - Hindi to English Voice & Text".
   * Category: *Tools / Productivity*.
   * Under App Content, declare that the microphone permission is used for voice speech-to-text input.
3. **Privacy Policy**:
   * Provide a simple privacy policy stating that voice and text data are processed solely for real-time translation and are never stored or sold.
