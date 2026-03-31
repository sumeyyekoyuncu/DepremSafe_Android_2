# DepremSafe — Android

An Android application designed to keep users informed and connected during and after earthquakes. DepremSafe delivers real-time seismic alerts, one-tap safety reporting, AI-powered guidance, and a mesh network that allows devices to communicate via Bluetooth and Wi-Fi Direct even when internet connectivity is lost.

> **Backend API:** [DepremSafe Backend](https://github.com/sumeyyekoyuncu/DepremSafe)

---

## Features

| Domain | Capabilities |
|---|---|
| **Safety Status** | One-tap "I'm Safe" / "I Need Help" reporting |
| **Location Sharing** | Real-time location sharing with emergency contacts |
| **Mesh Network** | Bluetooth and Wi-Fi Direct mesh for offline device-to-device communication |
| **Push Notifications** | FCM-powered earthquake alerts based on user location |
| **AI Assistant** | Built-in chatbot for preparedness guidance and post-disaster support |
| **Preparedness Guide** | Step-by-step instructions for before, during, and after an earthquake |
| **Onboarding** | 4-screen first-launch onboarding flow |
| **Authentication** | Email/password and Google OAuth2 login |

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary language |
| MVVM | Architecture pattern |
| Retrofit | HTTP client for backend API communication |
| Firebase FCM | Push notification delivery |
| Bluetooth / Wi-Fi Direct | Offline mesh network communication |
| Google Auth | Social login |

---

## Architecture

The application follows **MVVM (Model-View-ViewModel)** architecture, separating UI state management from business logic and data access.

```
app/
├── data/
│   ├── api/        # Retrofit service definitions and API client
│   └── model/      # Data transfer objects and domain models
├── ui/
│   ├── screens/    # Fragments and Activities
│   └── viewmodel/  # ViewModels and UI state holders
└── util/           # Helper classes and Kotlin extensions
```

---

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 17+
- Android SDK 34
- A running instance of the [DepremSafe Backend](https://github.com/sumeyyekoyuncu/DepremSafe)

### Setup

**1. Clone the repository**

```bash
git clone https://github.com/sumeyyekoyuncu/DepremSafe_Android_2.git
```

**2. Open in Android Studio**

Open the project directory. Gradle sync will run automatically — this may take 2–3 minutes on first launch.

**3. Configure the backend URL**

Update the base URL in `app/src/main/java/com/example/depremsafe/data/api/RetrofitClient.kt`:

```kotlin
private const val BASE_URL = "https://your-backend-url.com/"
```

**4. Add Firebase configuration**

Place your `google-services.json` file in the `/app` directory to enable FCM push notifications.

**5. Run the application**

Build and run the project using the Android Studio run button or `Shift + F10`.

---

## Troubleshooting

**`gradle-wrapper.jar` not found**

A download prompt will appear at the bottom of Android Studio. Click **Download** and the IDE will resolve the dependency automatically.
