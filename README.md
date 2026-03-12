# 📱 DepremSafe — Android

DepremSafe is an Android application designed to keep users safe during and after earthquakes. It provides real-time alerts, location sharing, AI-powered guidance, and a **mesh network** that allows devices to communicate even when internet connectivity is lost.

> 🔗 Backend API: [DepremSafe Backend](https://github.com/sumeyyekoyuncu/DepremSafe)

---

## 🚀 Features

- 🟢 **Quick Safety Status** — Instantly report "I'm Safe" or "I Need Help" with one tap
- 📍 **Location Sharing** — Share your real-time location with emergency contacts
- 📡 **Mesh Network** — When internet is unavailable, nearby devices form a mesh network via Bluetooth/Wi-Fi Direct to relay location data
- 🔔 **Push Notifications** — FCM-powered earthquake alerts based on your location
- 🤖 **AI Chatbot** — Get earthquake preparedness guidance and post-disaster support via built-in AI assistant
- 📖 **Earthquake Preparedness Guide** — Step-by-step instructions for before, during, and after an earthquake
- 🎬 **Onboarding** — 4-screen onboarding flow for first-time users
- 🔐 **Authentication** — Email/password and Google OAuth2 login

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary language |
| Retrofit | HTTP client for backend API |
| Firebase FCM | Push notifications |
| Bluetooth / Wi-Fi Direct | Mesh network communication |
| Google Auth | Social login |
| MVVM | Architecture pattern |

---

## ⚙️ Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17+
- Android SDK 34
- A running instance of the [DepremSafe Backend](https://github.com/sumeyyekoyuncu/DepremSafe)

### Setup

1. Clone the repository:
```bash
git clone https://github.com/sumeyyekoyuncu/DepremSafe_Android_2.git
```

2. Open the project in Android Studio — Gradle sync will run automatically (may take 2-3 minutes on first launch)

3. Update the backend URL in `app/src/main/java/com/example/depremsafe/data/api/RetrofitClient.kt`:
```kotlin
private const val BASE_URL = "https://your-backend-url.com/"
```

4. Add your `google-services.json` file to the `/app` directory for Firebase/FCM support

5. Run the app ▶️

### Troubleshooting

If you see a **"gradle-wrapper.jar not found"** error:
- A "Download gradle-wrapper.jar" prompt will appear at the bottom
- Click **Download** and Android Studio will resolve it automatically

---

## 📐 Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture:

```
app/
├── data/
│   ├── api/        # Retrofit API definitions
│   └── model/      # Data models
├── ui/
│   ├── screens/    # Fragments & Activities
│   └── viewmodel/  # ViewModels
└── util/           # Helpers, extensions
```

---


