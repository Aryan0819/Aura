# Aura Shield 🛡️

**Aura Shield** is an AI-powered Android application designed to protect your cognitive load by intercepting and analyzing incoming notifications from social apps like WhatsApp and Instagram. It provides real-time sentiment analysis, stress scoring, and smart reply suggestions to help you maintain digital well-being.

[![Android Target SDK](https://img.shields.io/badge/Target%20SDK-34-brightgreen.svg)](https://developer.android.com)
[![Minimum SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.github.com/v8.0)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20StateFlow-blue.svg)]()
[![License](https://img.shields.io/badge/License-MIT-purple.svg)]()

Aura Shield serves as an intelligent, zero-trust **agentic UX layer** that forms a cognitive buffer between you and your digital ecosystem. By intercepting high-friction notification storms (like WhatsApp and Instagram) and processing them through on-device hardware acceleration, Aura shields human attention from micro-decision fatigue.

[Explore Architecture](#-system-architecture) • [Key Features](#-key-features) • [Installation](#%EF%B8%8F-setup--installation) • [Privacy Protocol](#%EF%B8%8F-privacy--zero-trust-protocol)
---

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
-   **State Management:** Clean Architecture MVVM layered over modern asynchronous `StateFlow` primitives
-   **Background Lifecycle:** Persistent Android Foreground Services + System Notification Access APIs
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

1. **Intercept:** Aura captures a high-friction notification payload at the system layer.
2. **Analyze:** The raw text payload is pushed into the localized AI pipeline.
3. **Structured Vibe Check:** The specialized local engine compiles context details into a unified JSON schema:
    ```json
    {
      "summary": "Short summary of the message",
      "vibe": "Sentiment (e.g., Aggressive, Calm, Urgent)",
      "reply": "Suggested empathetic reply",
      "stress_score": 8
    }
    ```
4. **Shield Activation:** If the calculated `stress_score` passes a threshold ($> 7$), the UI shifts dynamically into an empathetic, soft-tone warning state, encouraging a micro-pause before engagement.

---

## ⚙️ Setup & Installation

### Environment Configurations
* **Minimum Android Environment:** API Level 26 (Android 8.0)
* **Target Android Environment:** API Level 34 (Android 14)
* **Required System Clearances:**
    * `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE` *(To parse digital messaging friction)*
    * `android.permission.POST_NOTIFICATIONS` *(To preserve service lifecycle stability)*


### Build Instructions
1. Clone the repository.
2. Open in Android Studio.
3. Sync Gradle and build the `:app` module.
4. Deploy to a physical device or emulator.
5. **Important**: Grant "Notification Access" to Aura Shield in your device settings.

## 🛡️ Privacy
Aura Shield processes message data locally using on-device AI models where possible to ensure your private conversations never leave your device.

---

