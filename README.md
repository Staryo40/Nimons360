# MAD Assignment 2026 Milestone 1

## List of Content

Debug command to clear tokens

```bash
adb shell pm clear com.labpro.nimons360
```

## UI Consistency
```bash
https://coolors.co/ffeaee-006d77-e29578-f4f6f8-1d3557-d62828-2a9d8f-e9c46a-457b9d
```
1. Core Brand Colors
   These are your main UI drivers that will give the app its sophisticated, tech-focused tracking identity.

   - Primary: Deep Teal
      HEX: #006D77
      RGB: (0, 109, 119)
      Usage: Primary action buttons ("Sign In", "Create", "Save"), the Top Header background, the active Bottom Navbar icon, and your own location pin/orientation arrow on the Map.
   - Secondary/Accent: Soft Coral
      HEX: #E29578
      RGB: (226, 149, 120)
      Usage: Secondary actions like the "Join" buttons in the Discover Families list, the floating "+" button, and the background for the 6-character Family Code display box.

2. Backgrounds & Neutrals
   Instead of harsh pure black and white, these tinted neutrals keep the app looking modern and reduce eye strain, which is great for a map-heavy app.

   - Background Base: Cool Off-White
       HEX: #F4F6F8
       RGB: (244, 246, 248)
       Usage: The main background color for the Home, Families, and Profile pages.
   - Surface/Card: Pure White
      HEX: #FFFFFF
      RGB: (255, 255, 255)
      Usage: The background for the Family list cards, the bottom sheets (like User Info and Edit Name), and the pop-ups.
   - Text Primary: Dark Navy
      HEX: #1D3557
      RGB: (29, 53, 87)
      Usage: Main headings, Family Names, and User Full Names. This provides excellent contrast against the off-white background for your Accessibility Testing bonus.
   - Text Secondary: Slate Gray
      HEX: #6C757D
      RGB: (108, 117, 125)
      Usage: Secondary information like emails ({NIM}@std.stei.itb.ac.id), member counts, and timestamps.

3. Semantic / Status Colors
   These colors communicate system status directly to the user, which is critical for the map, battery tracking, and network sensing features.

   - Success / Good: Mint Green
       HEX: #2A9D8F
       RGB: (42, 157, 143)
       Usage: High battery percentage (sent via WebSocket), active Wi-Fi internet status icon, and success messages.
   - Destructive / Danger: Crimson Red 
       HEX: #D62828
       RGB: (214, 40, 40)
       Usage: Strictly for destructive actions. Use this for the "Sign Out" text on the Profile page, the "Leave Family" button, and the Disconnected Network Pop-up icon.
   - Caution / Warning: Saffron Yellow
      HEX: #E9C46A
      RGB: (233, 196, 106)
      Usage: Low battery warning on the User Info bottom sheet, or to highlight the "Not a member" status on the Family Detail page.
   - Information: Muted Blue
      HEX: #457B9D
      RGB: (69, 123, 157)
      Usage: General informational icons, the "Mobile" data network status, or neutral pop-up dialogues.

## Project Structure
```bash
C:.
├───java/com/labpro/nimons360
│   ├── MainActivity.kt        # Entry point: Handles UI 
│   ├── MainApplication.kt     # Global app context & initialization
│   ├── core/                  # App-wide cross-cutting concerns
│   │   ├── events/            # Singletons for event handling (e.g., AuthEventBus)
│   │   └── utils/             # Helper functions and Kotlin Extensions
│   ├── data/                  # The "Model" in MVVM: Data handling and logic
│   │   ├── enums/             # Global enums (e.g., UserRole, ApiStatus)
│   │   ├── local/             # Local storage (Room Database, SharedPrefs)
│   │   ├── model/             # POJOs and Data classes (API/DB entities)
│   │   ├── remote/            # Network layer (Retrofit interfaces, DTOs)
│   │   └── repository/        # Single source of truth for data access
│   ├── ui/                    # The "View" in MVVM: All UI-related components
│   │   ├── features/          # Unified secondary/XML-based modules
│   │   │   ├── auth/          # XML-based Login/Register (Activities/Fragments)
│   │   │   └── profile/       # XML-based Profile/Drawer (Fragments)
│   │   ├── main/              # Modern Compose-based main flow
│   │   │   ├── MainContent.kt # Compose Skeleton (Scaffold, Header, BottomNav)
│   │   │   ├── NavGraph.kt    # Navigation routes and hardcoded logic
│   │   │   └── screens/       # Individual Composable tab implementations
│   │   └── theme/             # Jetpack Compose styling and theme definitions
│   └── viewmodel/             # The "VM" in MVVM: Bridge between UI and Data
│       ├── AuthViewModel.kt   # Logic for Authentication
│       └── MainViewModel.kt   # Logic for Main Content and Tabs
└── res/                       # Android XML Resources
    ├── drawable/              # Vector graphics and bitmap images
    ├── layout/                # UI definitions for XML Activities/Fragments
    ├── menu/                  # Actions for Toolbar, Drawer, and BottomNav
    ├── mipmap-.../            # App icons for various screen densities
    ├── values/                # Strings, Colors, Dimens, and Style definitions
    └── xml/                   # Configuration files (e.g., Network Security)
```

## Libraries Used

- osmdroid (`org.osmdroid:osmdroid-android`)
  Used for the XML-based map screen because it is open source, does not need billing, supports pan/zoom well, and fits the milestone requirement.
- Google Play Services Location (`com.google.android.gms:play-services-location`)
  Used through `FusedLocationProviderClient` to read the latest user location with better accuracy and lower battery cost than manual provider handling.
- OkHttp WebSocket (`com.squareup.okhttp3:okhttp`)
  Used for the live presence socket to send `update_presence`, keep the connection alive with `ping`, and receive realtime member position updates.
- Android Sensor Framework (`SensorManager`, `TYPE_ROTATION_VECTOR`)
  Used to derive azimuth/rotation in degrees so the current user marker can reflect phone orientation and include the value in presence payloads.
- Material Components (`com.google.android.material:material`)
  Used for cards, dialogs, and the permission/status surfaces on the Map screen so the XML UI stays visually consistent with the rest of the app.
