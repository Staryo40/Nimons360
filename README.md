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

| No  | Page                                                     | Screenshot                                                              |
| --- | -------------------------------------------------------- | ----------------------------------------------------------------------- |
| 1   | Splash Screen                                            | <img src="Screenshots/Original/Splashscreen.png" width="140">           |
| 2   | Login Screen                                             | <img src="Screenshots/Original/Login.png" width="140">                  |
| 3   | Login Validation: Email Kosong                           | <img src="Screenshots/Original/field_login_kosong_1.png" width="140">   |
| 4   | Login Validation: Password Kosong                        | <img src="Screenshots/Original/field_login_kosong_2.png" width="140">   |
| 5   | Login Validation: Email atau Pass Salah                  | <img src="Screenshots/Original/Fail_login.png" width="140">             |
| 6   | Home Screen                                              | <img src="Screenshots/Original/Home.png" alt="Home Screen" width="140"> |
| 7   | My Families Screen: Sebelum bergabung dengan Family Baru | <img src="Screenshots/Original/my_family_1.png" width="140">            |
| 8   | My Families Screen: Setelah bergabung dengan Family Baru | <img src="Screenshots/Original/my_family_2.png" width="140">            |
| 9   | Families Search Result                                   | <img src="Screenshots/Original/Search_Fam.png" width="140">             |
| 10  | Create Family Screen                                     | <img src="Screenshots/Original/Create_New_Fam.png" width="140">         |
| 11  | Create Family Validation: Nama Family Kosong             | <img src="Screenshots/Original/nama_fam_kosong.png" width="140">        |
| 12  | Join Family Dialog                                       | <img src="Screenshots/Original/Join_Fam_1.png" width="140">             |
| 13  | Join Family Berhasil                                     | <img src="Screenshots/Original/Join_Fam_2.png" width="140">             |
| 14  | Join Family Validation: Salah Kode                       | <img src="Screenshots/Original/join_salah.png" width="140">             |
| 15  | Family Detail Screen: Overview                           | <img src="Screenshots/Original/Fam_detail_1.png" width="140">           |
| 16  | Family Detail Screen: Members Section                    | <img src="Screenshots/Original/Fam_detail_2.png" width="140">           |
| 17  | Leave Family Confirmation                                | <img src="Screenshots/Original/leave_fam.png" width="140">              |
| 18  | Profile Screen                                           | <img src="Screenshots/Original/profile.png" width="140">                |
| 19  | Edit Profile Screen                                      | <img src="Screenshots/Original/edit_profile.png" width="140">           |
| 20  | Profile Screen After Edit                                | <img src="Screenshots/Original/after_edit_profile.png" width="140">     |
| 21  | Map Screen: Location Permission Prompt                   | <img src="Screenshots/Original/grand_permission_loc.png" width="140">   |
| 22  | Map Screen: Location Permission Granted                  | <img src="Screenshots/Original/grand_permission_loc_2.png" width="140"> |
| 23  | Map Screen: Lokasi 1                                     | <img src="Screenshots/Original/fam_loc_1.png" width="140">              |
| 24  | Map Screen: Lokasi 2                                     | <img src="Screenshots/Original/fam_loc_2.png" width="140">              |
| 25  | Map Member Details: Lokasi 1                             | <img src="Screenshots/Original/fam_loc_detail_1.png" width="140">       |
| 26  | Map Member Details: Lokasi 2                             | <img src="Screenshots/Original/fam_loc_detail_2.png" width="140">       |
| 27  | Favorite Location Flow: Sebelum Pin                      | <img src="Screenshots/Original/pin_before.png" width="140">             |
| 28  | Favorite Location Flow: Setelah Pin 1                    | <img src="Screenshots/Original/pin_after.png" width="140">              |
| 29  | Favorite Location Flow: Setelah Pin 2                    | <img src="Screenshots/Original/pin_after_2.png" width="140">            |
| 30  | Favorite Locations Overview: Variant 1                   | <img src="Screenshots/Original/fav1.jpeg" width="140">                  |
| 31  | Favorite Locations Overview: Variant 2                   | <img src="Screenshots/Original/fav2.jpeg" width="140">                  |
| 32  | Favorite Locations Overview: Variant 3                   | <img src="Screenshots/Original/fav3.jpeg" width="140">                  |
| 33  | Connectivity Status: Wi-Fi                               | <img src="Screenshots/Original/internet_wifi.png" width="140">          |
| 34  | Connectivity Status: Mobile                              | <img src="Screenshots/Original/internet_mobile.png" width="140">        |
| 35  | Connectivity Status: Offline                             | <img src="Screenshots/Original/Disconnect.jpeg"  width="140">           |
| 36  | Live Location Active                                     | <img src="Screenshots/Original/live_loc_aktif.png" width="140">         |
| 37  | Live Room Join                                           | <img src="Screenshots/Original/Live_1.png" width="140">                 |
| 38  | Live Room Screen: 1                                      | <img src="Screenshots/Original/live.png" width="140">                   |
| 39  | Live Room Screen: 2                                      | <img src="Screenshots/Original/live_2.png" width="140">                 |

### Accessibility Testing

| No  | Page | Screenshot |
| --- | ---- | ---------- |
| 1   | Home Screen | <img src="Screenshots/Accessibility_Before/Home/screenshot_Nimons360_2026-04-18-18:00:57.png" alt="Accessibility Before Home Screen" width="140"> |
| 2   | Families Screen | <img src="Screenshots/Accessibility_Before/Family/screenshot_Nimons360_2026-04-18-18:01:41.png" alt="Accessibility Before Families Screen" width="140"> |
| 3   | Profile Screen | <img src="Screenshots/Accessibility_Before/Profile/screenshot_Nimons360_2026-04-18-18:02:22.png" alt="Accessibility Before Profile Screen" width="140"> |
| 4   | Create Family Screen | <img src="Screenshots/Accessibility_Before/Create Family/screenshot_Nimons360_2026-04-18-18:03:30.png" alt="Accessibility Before Create Family Screen" width="140"> |
| 5   | Family Detail Screen | <img src="Screenshots/Accessibility_Before/Detail Family/screenshot_Nimons360_2026-04-18-18:04:03.png" alt="Accessibility Before Family Detail Screen" width="140"> |
| 6   | Map Member Detail Dialog | <img src="Screenshots/Accessibility_Before/Map User Detail/screenshot_Nimons360_2026-04-18-18:04:41.png" alt="Accessibility Before Map Member Detail Dialog" width="140"> |
| 7   | Login Screen | <img src="Screenshots/Accessibility_Before/Login/screenshot_Nimons360_2026-04-18-18:05:49.png" alt="Accessibility Before Login Screen" width="140"> |

*Keterangan dapat dilihat pada `Screenshots/Accessibility_Before`.*

### Improved Interface after Accessibility Testing

| No  | Page             | Screenshot                                                                                |
| --- | ---------------- | ----------------------------------------------------------------------------------------- |
| 1   | Home belum login | <img src="/public/page_screenshots/home_unsigned.png" alt="Home belum login" width="140"> |
| 2   | Login            | <img src="/public/page_screenshots/login.png" alt="Login" width="140">                    |

*Keterangan dapat dilihat pada `Screenshots/Accessibility_After`.*

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

| No | Feature                | Contributors |
|----|------------------------|--------------|
| 1  | Livestreaming          | 13523114     |
| 2  | Network Sensing        | 13523114     |
| 3  | OpenAPI                | 13523114     |
| 4  | Mark Favorite Location | 13523114     |
| 5  | Map & GPS              | 13523068     |
| 6  | Websocket              | 13523068     |
| 7  | User Info              | 13523068     |
| 8  | Internet Status        | 13523068     |
| 9  | Accessibility Testing  | 13523068     |
| 10 | Auth                   | 13523100     |
| 11 | Header & Bottom Navbar | 13523100     |
| 12 | Home                   | 13523100     |
| 13 | Families List          | 13523100     |