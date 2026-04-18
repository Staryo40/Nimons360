# Nimons360: Family location safety Android App

<p align="center">
  <a href="https://kotlinlang.org/">
    <img src="https://img.shields.io/badge/Kotlin-1.9.10-blue.svg" alt="Kotlin Version" />
  </a>
  <a href="https://gradle.org/">
    <img src="https://img.shields.io/badge/Gradle-8.3-green.svg" alt="Gradle Version" />
  </a>

  <a href="https://developer.android.com/training/data-storage/room">
    <img src="https://img.shields.io/badge/Room_Persistence-Local_DB-3DDC84.svg?logo=android&logoColor=white" alt="Room" />
  </a>
  <a href="https://en.wikipedia.org/wiki/WebSocket">
    <img src="https://img.shields.io/badge/WebSocket-Real--time-010101.svg?logo=socket.io&logoColor=white" alt="WebSocket" />
  </a>
  <a href="https://developer.android.com/training/articles/keystore">
    <img src="https://img.shields.io/badge/Android_Keystore-Security-FB8C00.svg?logo=google-cloud&logoColor=white" alt="Keystore" />
  </a>
  <a href="https://github.com/osmdroid/osmdroid">
    <img src="https://img.shields.io/badge/osmdroid-OpenStreetMap-73C13D.svg?logo=openstreetmap&logoColor=white" alt="osmdroid" />
  </a>
</p>

<p align="center">
    <img src="public/NimonsIcon432.png" alt="Application Logo" width="500"/>
</p>

Stay connected with your loved ones to ensure their safety. 

UI uses this main color palette
```bash
https://coolors.co/ffeaee-006d77-e29578-f4f6f8-1d3557-d62828-2a9d8f-e9c46a-457b9d
```

<p align="center">
    <img src="public/App.gif" alt="Application interface" width="400"/>
</p>


## List of Content

- [Features](#features)
- [Libraries](#libraries)
- [Screenshots and Accessibility Testing](#screenshots-and-accessibility-testing)
- [Project Structure](#project-structure)
- [Running the Program](#running-the-program)
- [Creator](#creator)
- [Team Roles](#team-roles)

## Features

## Libraries

- Jetpack Compose – Declarative UI framework
- Material 3 – Modern Material Design components
- AndroidX Core & Lifecycle – Core utilities and lifecycle-aware components
- Navigation (Compose + Fragment) – App navigation handling
- DrawerLayout – Side navigation UI support

- Coil – Image loading for Compose

- Retrofit – REST API client
- Gson – JSON serialization/deserialization
- OkHttp (+ Logging Interceptor) – Networking layer

- Room – SQLite database abstraction
- AndroidX Security Crypto – EncryptedSharedPreferences

- osmdroid – OpenStreetMap-based map rendering
- Google Play Services Location – Location tracking

- AndroidX Media3 (ExoPlayer) – Media playback
- WebRTC – Real-time communication
- Agora VideoUIKit – Live streaming UI components

- Firebase Analytics – User analytics
- Firebase App Distribution – App distribution for testers

- Kotlin Coroutines – Asynchronous programming

- JUnit – Unit testing
- AndroidX Test (JUnit, Espresso) – Instrumented testing
- Compose UI Test – UI testing for Compose
- Kotlin Coroutines Test – Coroutine testing utilities

- Kotlin (Android + Compose)
- KSP (Kotlin Symbol Processing) – Used by Room
- Google Services Plugin – Firebase integration
- Firebase App Distribution Plugin – CI/CD distribution

## Screenshots and Accessibility Testing

### Original Interface
| No | Page             | Screenshot                                                   |
|----|------------------|--------------------------------------------------------------|
| 1  | Home belum login | ![Screenshot](/public/page_screenshots/home_unsigned.png)         |
| 2  | Login            | ![Screenshot](/public/page_screenshots/login.png)                 |

### Accessibility Testing
| No | Page             | Screenshot                                                   |
|----|------------------|--------------------------------------------------------------|
| 1  | Home belum login | ![Screenshot](/public/page_screenshots/home_unsigned.png)         |
| 2  | Login            | ![Screenshot](/public/page_screenshots/login.png)                 |

### Improved Interface after Accessibility Testing
| No | Page             | Screenshot                                                   |
|----|------------------|--------------------------------------------------------------|
| 1  | Home belum login | ![Screenshot](/public/page_screenshots/home_unsigned.png)         |
| 2  | Login            | ![Screenshot](/public/page_screenshots/login.png)                 |

## Project Structure

The source code in `app/src/main/java` is divided as follows:

1. `core`  
   Contains low-level utilities and app-wide helpers.  
   Includes network monitoring, safe API call wrappers, token management, and internal event communication (event bus).

2. `data`  
   Handles all data-related logic.  
   Includes:
    - `model` → Data classes for API, database, and UI state
    - `remote` → API service, Retrofit setup, and interceptors
    - `local` → Room database and DAO definitions
    - `repository` → Abstraction layer combining local + remote data sources
    - `enums` → Shared enums used across the app

3. `ui`  
   Contains all UI-related components.  
   Organized into:
    - `features` → Feature-based screens (auth, families, map, profile, live)
    - `main` → Main navigation, Compose screens, and shared layout structure
    - `theme` → App theming (colors, typography, styling)
    - `shared` → Reusable UI components

4. `viewmodel`  
   Contains all ViewModels and their factories.  
   Responsible for managing UI state, handling business logic, and connecting repositories to the UI layer.

5. `MainActivity.kt`  
   Entry point of the application.  
   Hosts the main UI and navigation container.

6. `MainApplication.kt`  
   Application-level setup.  
   Initializes global dependencies such as repositories, database, and token manager.

---

While `app/src/main/res` contains Android resources used by the app:

1. `anim`  
   Animation resources.

2. `drawable`  
   Images, shapes, and drawable assets.

3. `layout`  
   XML-based UI layouts (used alongside Compose where needed).

4. `mipmap`  
   App launcher icons for different screen densities.

5. `values`  
   Static resources such as strings, colors, dimensions, and themes.

## Program Commands
### Building APK
You could use the APK "app-release" in the "apk" directory and use in an android device or emulator

Should you want to build the project yourself, run the following command in root of this project for debug build:
```bash
./gradlew assembleDebug
```

For release build:
```bash
./gradlew assembleRelease
```

### Additional Commands

Debug command to clear tokens
```bash
adb shell pm clear com.labpro.nimons360
```

## Creators

<table>
    <tr align="left">
        <td><b>NIM</b></td>
        <td><b>Name</b></td>
        <td align="center"><b>GitHub</b></td>
    </tr>
    <tr align="left">
        <td>13523068</td>
        <td>Muh. Rusmin Nurwadin</td>
        <td align="center" >
            <div style="margin-right: 20px;">
            <a href="https://github.com/Rusmn" >
                <img src="https://avatars.githubusercontent.com/u/103303974?v=4" width="48px;" alt=""/> 
                <br/> <sub><b> @Rusmn </b></sub>
            </a><br/>
            </div>
        </td>
    </tr>
    <tr align="left">
        <td>13523100</td>
        <td>Aryo Wisanggeni</td>
        <td align="center" >
            <div style="margin-right: 20px;">
            <a href="https://github.com/Staryo40" >
                <img src="https://avatars.githubusercontent.com/u/139449070?v=4" width="48px;" alt=""/> 
                <br/> <sub><b> @Staryo40 </b></sub>
            </a><br/>
            </div>
        </td>
    </tr>
    <tr align="left">
        <td>13523114</td>
        <td>Guntara Hambali</td>
        <td align="center" >
            <div style="margin-right: 20px;">
            <a href="https://github.com/guntarahmbl" >
                <img src="https://avatars.githubusercontent.com/u/102271055?v=4" width="48px;" alt=""/> 
                <br/> <sub><b> @guntarahmbl </b></sub>
            </a><br/>
            </div>
        </td>
    </tr>
</table>

## Team Roles

| No  | Feature             | Contributors |
|-----|---------------------|--------------------|
| 1   | Login               | 13523064           |
| 2   | Register            | 13523064           |
| 3   | Buyer Navigation Bar| 13523100           |
| 4   | Seller Navigation Bar| 13523064          |
