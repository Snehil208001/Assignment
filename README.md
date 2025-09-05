Voice-to-Text Keyboard for Android
This is a custom Input Method Editor (IME) for Android that allows users to input text by speaking. It records audio, sends it to the Groq API for transcription, and inputs the resulting text into any text field on the device.

## ✨ Key Features
System-Wide Voice Input: Use voice-to-text in any application, just like a standard keyboard.

Simple "Hold to Speak" Interface: Press and hold the microphone button to record; release to transcribe.

Fast Transcription: Powered by the Groq API and the Whisper ASR model for quick and accurate speech-to-text conversion.

Modern Android Tech: Built with Kotlin, Jetpack Compose, and Coroutines for a reactive and efficient architecture.

Clean Architecture: Uses a ViewModel to manage state and separate UI logic from data operations.

## 🚀 Getting Started
### Prerequisites
Android Studio

An Android device or emulator running API level 26 or higher

An API key from Groq

### Setup
Clone the repository:

Bash

git clone https://github.com/your-username/your-repository-name.git
Add your API Key:

Create a file named local.properties in the root directory of the project.

Add your Groq API key to this file:

Properties

GROQ_API_KEY="your_api_key_here"
Build and run the app on your device or emulator using Android Studio.

## ⌨️ How to Use
After installing the app, follow the three steps on the setup screen to enable the keyboard.

### Step 1: Grant Audio Permission
The app needs access to your device's microphone to record your voice. Tap the "Request Microphone Permission" button and allow the permission when prompted.

### Step 2: Enable the Keyboard
You need to enable the "Voice-to-Text Keyboard" in your device's settings.

Tap the "Open Keyboard Settings" button.

Find "Voice-to-Text Keyboard" in the list and toggle it on.

Acknowledge the standard Android security warning.

### Step 3: Switch Input Method
Finally, select the "Voice-to-Text Keyboard" as your active input method.

Tap the "Switch Keyboard" button.

Choose "Voice-to-Text Keyboard" from the list that appears.

Now, whenever you tap on a text field, your new voice keyboard will appear. Press and hold the button to speak, and release it to see your transcribed text.

## 🛠️ Technology Stack
Language: Kotlin

UI: Jetpack Compose

Architecture: Model-View-ViewModel (MVVM)

Asynchronous Programming: Kotlin Coroutines

Dependency Injection (Manual): ViewModelProvider.Factory

Networking:

Retrofit for making API calls

OkHttp for logging network requests

Speech-to-Text: Groq API with the Whisper model

## Project Status
This project is currently under development. It serves as a functional demonstration of creating a custom IME for Android using modern development practices. Future improvements could include:

Adding support for more languages.

Implementing a settings screen for user preferences.

Improving error handling and UI feedback.

Optimizing performance with Baseline Profiles.
