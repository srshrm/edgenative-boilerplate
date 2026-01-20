# AEM KMP Boilerplate

A Kotlin Multiplatform (KMP) boilerplate for
migrating [AEM Edge Delivery Services (EDS)](https://www.aem.live/) sites to native **Android**, **iOS**, and **Desktop (JVM)** applications. This starter project provides a complete foundation for
rendering EDS content natively using Compose Multiplatform.

## What It Does

This boilerplate fetches content from AEM EDS sites via JSON and renders it natively on all
platforms. It includes:

- **Block Rendering System** - Native UI components for common EDS blocks (Hero, Cards, Columns)
- **Navigation** - Type-safe routing with Navigation 3 and deep linking support
- **Theming** - Material 3 design with customizable colors and typography
- **Push Notifications** - Cross-platform notifications using Firebase (Android/iOS) and KMPNotifier
- **Image Loading** - Efficient image handling with Coil
- **Network Layer** - Ktor-based HTTP client with platform-specific engines
- **DataStore** - Cross-platform preferences storage

## Quick Start

### Configure Your EDS Site

Update `composeApp/src/commonMain/kotlin/com/adobe/aem_kmp_boilerplate/data/EdsConfig.kt`:

```kotlin
val DefaultEdsConfig = EdsConfig(
    siteUrl = "https://your-site.aem.live",
    homePath = "",  // Optional: custom home page path (e.g., "emea/en/products")
)
```

### Run the App

**Android:**

```bash
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

**Desktop (JVM):**

```bash
./gradlew :desktopApp:run
```

**iOS:**

```bash
open iosApp/iosApp.xcodeproj
# Run from Xcode
```

## Project Structure (AGP 9.0)

This project uses the new AGP 9.0 module separation pattern:

```
aem-kmp-boilerplate/
├── composeApp/          # Shared KMP library (com.android.kotlin.multiplatform.library)
│   └── src/
│       ├── commonMain/  # Shared code for all platforms
│       ├── androidMain/ # Android platform implementations
│       ├── iosMain/     # iOS platform implementations
│       └── jvmMain/     # Desktop platform implementations
├── androidApp/          # Android application module (com.android.application)
├── desktopApp/          # Desktop application module (kotlin.jvm)
└── iosApp/              # iOS app wrapper (SwiftUI)
```

### Module Dependency Flow

```
composeApp (shared library)
    ↑           ↑           ↑
androidApp  desktopApp   iosApp
```

- **[composeApp](./composeApp)** - Shared Kotlin Multiplatform code
    - `blocks/` - EDS block renderers (Hero, Cards, Columns, etc.)
    - `data/` - Data models and EDS configuration
    - `navigation/` - Navigation routes and link handling
    - `network/` - HTTP client and API service
    - `screens/` - Screen composables
    - `theme/` - Material 3 theming
- **[androidApp](./androidApp)** - Android application entry point
- **[desktopApp](./desktopApp)** - Desktop application entry point
- **[iosApp](./iosApp)** - iOS app wrapper (SwiftUI entry point)

## Customization

### Add Custom EDS Blocks

1. Create a new composable in `composeApp/src/commonMain/.../blocks/YourBlock.kt`
2. Add it to `blocks/BlockRenderer.kt`

### Update Branding

- **Colors**: Edit `composeApp/.../theme/Color.kt`
- **Typography**: Edit `composeApp/.../theme/Typography.kt`
- **App Name**: 
  - Android: `androidApp/src/main/res/values/strings.xml`
  - iOS: `iosApp/iosApp/Info.plist`
  - Desktop: `desktopApp/src/main/kotlin/main.kt`

## Documentation

For detailed architecture, migration guides, and development instructions,
see [CLAUDE.md](./CLAUDE.md).

## Tech Stack

| Component             | Version       | Purpose                          |
|-----------------------|---------------|----------------------------------|
| Kotlin                | 2.3.0         | Language                         |
| Compose Multiplatform | 1.10.0        | Shared UI framework              |
| AGP                   | 9.0.0         | Android Gradle Plugin            |
| Gradle                | 9.2.1         | Build system                     |
| Ktor                  | 3.3.3         | Networking                       |
| Koin                  | 4.1.1         | Dependency Injection             |
| Coil                  | 3.3.0         | Image Loading                    |
| Navigation 3          | 1.0.0-alpha06 | Type-safe Navigation             |
| KMPNotifier           | 1.6.1         | Push Notifications               |
| Firebase BOM          | 34.8.0        | Cloud Messaging & Analytics      |
| DataStore             | 1.2.0         | Preferences Storage              |

## Build Commands

```bash
# Android
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug

# Desktop
./gradlew :desktopApp:run
./gradlew :desktopApp:packageDmg    # macOS distribution

# iOS Framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Full build
./gradlew build
```

---

Learn more
about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
and [AEM Edge Delivery Services](https://www.aem.live/).
