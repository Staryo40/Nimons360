# MAD Assignment 2026 Milestone 1

## List of Content

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