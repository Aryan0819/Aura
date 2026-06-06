<<<<<<< HEAD
# Aura Shield 🛡️

**Aura Shield** is an AI-powered Android application designed to protect your cognitive load by intercepting and analyzing incoming notifications from social apps like WhatsApp and Instagram. It provides real-time sentiment analysis, stress scoring, and smart reply suggestions to help you maintain digital well-being.

## 🚀 Key Features

-   **Notification Interception**: Automatically listens to WhatsApp and Instagram messages using a `NotificationListenerService`.
-   **AI Sentiment Analysis**: Powered by Gemini Nano to analyze the "vibe" of incoming messages.
-   **Stress Scoring**: Calculates a stress level (1-10) for each message. High-stress messages trigger a visual "Safety Ring" pulse animation.
-   **Smart Replies**: Generates empathetic and context-aware reply suggestions.
-   **Quick Reply**: Direct "Send" buttons within the Aura UI to instantly reply without opening the messaging app.
-   **Persistence**: Uses a foreground service (`AuraEmpathyService`) to ensure the AI monitoring remains active.
-   **Debug Console**: A built-in log viewer to track raw notification events and AI processing steps.

## 🛠️ Tech Stack

-   **Language**: Kotlin
-   **UI**: Jetpack Compose
-   **AI**: Gemini Nano (Structured JSON Protocol)
-   **Background Processing**: Foreground Services & Notification Listener API
-   **Architecture**: MVVM with StateFlow

## 📸 How it Works

1.  **Intercept**: Aura detects a notification from a supported app.
2.  **Analyze**: The message content is sent to the AI core.
3.  **Vibe Check**: The AI returns a JSON containing:
    ```json
    {
      "summary": "Short summary of the message",
      "vibe": "Sentiment (e.g., Aggressive, Calm, Urgent)",
      "reply": "Suggested empathetic reply",
      "stress_score": 8
    }
    ```
4.  **Shield**: If `stress_score > 7`, the UI pulses red, alerting you to take a breath before engaging.

## ⚙️ Setup & Installation

-   **Minimum SDK**: 26 (Android 8.0)
-   **Target SDK**: 34
-   **Permissions**: 
    -   Notification Listener Access (Required to read messages)
    -   Post Notifications (For foreground service persistence)

### Build Instructions
1. Clone the repository.
2. Open in Android Studio.
3. Sync Gradle and build the `:app` module.
4. Deploy to a physical device or emulator.
5. **Important**: Grant "Notification Access" to Aura Shield in your device settings.

## 🛡️ Privacy
Aura Shield processes message data locally using on-device AI models where possible to ensure your private conversations never leave your device.

---
*Developed for the Aura Hackathon.*
=======
# Aura
>>>>>>> 7b1ef6995570b096e7250231de7fb8f9e02068c3
