# 🌱 AgriAutomationHub

AgriAutomationHub is an advanced Android-based agricultural platform designed to empower farmers with modern technology, real-time data, and AI-driven insights.

## 🚀 Key Features

*   **🔍 Crop Care & Disease Detection**: Identifying plant diseases using on-device ML (TensorFlow Lite) and providing treatment protocols.
*   **💧 Smart Irrigation**: Real-time monitoring and control of irrigation systems via Firebase and IoT sensors.
*   **🌾 AI Farming Assistant**: A GPT-powered chatbot (Azure OpenAI) to answer complex agricultural questions.
*   **📈 Mandi Market Prices**: Real-time market price tracking across India.
*   **📏 Field Measurement**: Accurately calculate farmland area using Google Maps integration.
*   **🌦️ Weather & News**: Localized weather forecasts and the latest agricultural news.

## 🛠️ Technical Stack

*   **Frontend**: Java (Android SDK)
*   **Database**: Firebase Realtime DB, Firestore, Room Persistence Library
*   **Networking**: Retrofit, OkHttp
*   **Machine Learning**: TensorFlow Lite
*   **APIs**: Google Maps, OpenWeatherMap, Azure OpenAI

## ⚙️ Setup & Configuration

### 1. Security (API Keys)
To protect sensitive credentials, this project uses a `local.properties` based key management system.

1.  Open `local.properties` in the project root.
2.  Add the following keys:
    ```properties
    OPENWEATHER_API_KEY=your_key_here
    AZURE_OPENAI_API_KEY=your_key_here
    CROPCARE_API_KEY=your_key_here
    GOOGLE_MAPS_API_KEY=your_key_here
    ```

### 2. Firebase Integration
1.  Add your `google-services.json` to the `app/` directory.
2.  Enable **Realtime Database** and **Firestore** in the Firebase Console.
3.  Set up **Firebase Authentication** (Google Sign-In / Email).

## 🧹 Maintenance Notes

### Code Cleanup
Legacy commented-out code in `MainActivity.java` and `CropCareActivity.java` has been removed to maintain a lean and readable codebase.

### Security
All hardcoded API keys have been migrated to `BuildConfig` or manifest placeholders as of Jan 2026.

---
*Developed for future-ready agriculture.*
