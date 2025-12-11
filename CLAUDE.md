# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AEM KMP Boilerplate is a Kotlin Multiplatform (KMP) application that renders Adobe Experience Manager (AEM) Edge Delivery Services (EDS) content natively on **Android**, **iOS**, and **Desktop (JVM)**. It uses Compose Multiplatform for shared UI and fetches EDS page content via a JSON conversion service.

**Package namespace**: `com.adobe.aem_kmp_boilerplate`

## Build Commands

### Android
```bash
# Build debug APK
./gradlew :composeApp:assembleDebug

# Build release APK
./gradlew :composeApp:assembleRelease

# Install on connected device
./gradlew :composeApp:installDebug
```

### Desktop (JVM)
```bash
# Run desktop application
./gradlew :composeApp:run

# Build distributable packages (DMG, MSI, DEB)
./gradlew :composeApp:packageDistributionForCurrentOS
./gradlew :composeApp:packageDmg    # macOS
./gradlew :composeApp:packageMsi    # Windows
./gradlew :composeApp:packageDeb    # Linux
```

### iOS
```bash
# Build iOS framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
./gradlew :composeApp:linkReleaseFrameworkIosArm64

# Open in Xcode (run from there)
open iosApp/iosApp.xcodeproj
```

## Project Structure

```
aem-kmp-boilerplate/
├── composeApp/                      # Main Kotlin Multiplatform module
│   ├── src/
│   │   ├── commonMain/kotlin/       # Shared code for all platforms
│   │   │   └── com/adobe/aem_kmp_boilerplate/
│   │   │       ├── App.kt           # Main entry point composable
│   │   │       ├── blocks/          # EDS block renderers
│   │   │       ├── data/            # Data models & configuration
│   │   │       ├── navigation/      # Navigation routes & link handling
│   │   │       ├── network/         # HTTP client & API service
│   │   │       ├── notification/    # Push notification service
│   │   │       ├── screens/         # Screen composables
│   │   │       ├── theme/           # Material 3 theming
│   │   │       └── utils/           # Utilities (expect declarations)
│   │   ├── androidMain/kotlin/      # Android-specific implementations
│   │   ├── iosMain/kotlin/          # iOS-specific implementations
│   │   └── jvmMain/kotlin/          # Desktop-specific implementations
│   └── build.gradle.kts             # Module configuration
├── iosApp/                          # iOS SwiftUI wrapper
│   └── iosApp/
│       ├── iOSApp.swift             # iOS app entry + Firebase/notifications
│       ├── ContentView.swift        # SwiftUI → Compose bridge
│       └── GoogleService-Info.plist # Firebase configuration
├── gradle/libs.versions.toml        # Version catalog (all dependencies)
└── build.gradle.kts                 # Root project configuration
```

## Architecture

### EDS Content Flow

```
EdsConfig → EdsApiService → JSON → EdsPage → SectionRenderer → BlockRenderer
```

1. **EdsConfig** (`data/EdsConfig.kt`) - Configures the EDS site connection:
   - `siteUrl`: Base URL of the EDS site (e.g., "https://main--aem-boilerplate--adobe.aem.live")
   - `jsonServiceUrl`: JSON conversion service endpoint

2. **EdsApiService** (`network/EdsApiService.kt`) - Fetches pages:
   - `fetchPage(config, path)` - Fetch any page by path
   - `fetchHomePage(config)` - Fetch the home page

3. **ContentParser** (`data/ContentParser.kt`) - Parses JSON:
   - `parseContentNodes()` - Parse content arrays
   - `parseBlockContent()` - Parse block 3D arrays (rows → columns → items)
   - `extractPlainText()` - Extract text from nested content

4. **SectionRenderer** (`blocks/DefaultContent.kt`) - Renders sections containing blocks and content

5. **BlockRenderer** (`blocks/BlockRenderer.kt`) - Dispatches to specialized block composables

### Data Models

| Model | Purpose |
|-------|---------|
| `EdsPage` | Root page with `metadata` and `content` |
| `SectionContainer` | Section wrapper with optional metadata |
| `ContentNode` | Universal element: heading, paragraph, image, link, block, list |
| `BlockRow` | Row in block content table |
| `BlockColumn` | Column in block row containing `ContentNode` items |

### Block System

Blocks are EDS content structures rendered as native UI. Current implementations:

| Block | File | Description |
|-------|------|-------------|
| `HeroBlock` | `blocks/HeroBlock.kt` | Hero banners with variants (small, large, centered) |
| `CardsBlock` | `blocks/CardsBlock.kt` | Card grid using FlowRow |
| `ColumnsBlock` | `blocks/ColumnsBlock.kt` | Multi-column layouts |
| `GenericBlock` | `blocks/GenericBlock.kt` | Fallback for unrecognized blocks |

**Adding a new block:**
1. Create `blocks/YourBlock.kt`:
```kotlin
@Composable
fun YourBlock(
    rows: List<BlockRow>,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val edsConfig = LocalEdsConfig.current
    // Render your block UI
}
```
2. Add case in `BlockRenderer.kt`:
```kotlin
blockName.contains("yourblock") -> {
    YourBlock(rows = blockContent, onLinkClick = onLinkClick, modifier = modifier)
}
```

### Navigation

Uses **Navigation 3** (`androidx.navigation3`) with type-safe routes:

- **Routes** (`navigation/Routes.kt`):
  - `Home` - Home screen (object)
  - `PageDetail(path: String)` - Any EDS page by path

- **LinkHandler** (`navigation/LinkHandler.kt`):
  - `shouldNavigateInternally()` - Check if URL is internal
  - `extractPath()` - Extract path from URL for navigation
  - `isAnchorLink()` / `isSpecialProtocol()` - Handle special URLs

### Platform-Specific Code (expect/actual)

| Expect Declaration | Purpose |
|-------------------|---------|
| `createPlatformHttpClient()` | HTTP engine (OkHttp for Android/JVM, Darwin for iOS) |
| `openUrl(url: String)` | Open URL in system browser |

### Theme System

| File | Purpose |
|------|---------|
| `theme/Theme.kt` | `AemAppTheme` - Material 3 wrapper with dark/light support |
| `theme/Color.kt` | Adobe Red brand colors (light/dark variants) |
| `theme/Typography.kt` | Typography with custom font support |
| `theme/Spacing.kt` | Spacing tokens (8dp grid), icon sizes, corner radii |

**Custom fonts**: Add `.ttf` files to `composeResources/font/` and uncomment font loading in `Typography.kt`.

### Push Notifications

Uses **KMPNotifier** library for cross-platform notifications:

| Platform | Setup |
|----------|-------|
| Android | `AndroidApp.kt` initializes NotifierManager, requires `google-services.json` |
| iOS | `iOSApp.swift` initializes Firebase and NotifierManager, requires `GoogleService-Info.plist` |
| Desktop | `main.kt` initializes with icon path |

**NotificationService** (`notification/NotificationService.kt`) provides:
- `showNotification()` - Show local notification
- `getPushToken()` - Get FCM/APNs token
- `subscribeToTopic()` / `unsubscribeFromTopic()` - Topic subscriptions
- `setNotificationListener()` - Handle notification events

## Migrating an EDS Site

To point this app to a different EDS site:

### 1. Update EdsConfig (`data/EdsConfig.kt`)

```kotlin
val DefaultEdsConfig = EdsConfig(
    siteUrl = "https://main--aem-boilerplate--adobe.aem.live",
    jsonServiceUrl = "https://your-json-service.workers.dev"  // Optional custom service
)
```

### 2. URL Pattern
The app constructs URLs as:
- **Site URL**: `https://main--aem-boilerplate--adobe.aem.live`
- **JSON URL**: `{jsonServiceUrl}?url={siteUrl}/{path}&head=false`

### 3. Custom Blocks
If the EDS site has custom blocks:
1. Create block composables in `blocks/`
2. Add cases to `BlockRenderer.kt`

### 4. Theme Customization
Update `theme/Color.kt` with brand colors:
```kotlin
val Primary = Color(0xFF...)  // Your primary brand color
```

### 5. App Identity
- Android: Update `composeApp/src/androidMain/res/values/strings.xml` for app name
- iOS: Update `iosApp/iosApp/Info.plist`
- Desktop: Update window title in `jvmMain/kotlin/.../main.kt`

## Key Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Compose Multiplatform | 1.10.0-rc01 | Shared UI framework |
| Kotlin | 2.2.21 | Language |
| Ktor | 3.3.3 | HTTP networking |
| Coil | 3.3.0 | Image loading |
| Koin | 4.1.1 | Dependency injection |
| Navigation 3 | 1.0.0-alpha06 | Navigation |
| KMPNotifier | 1.6.1 | Push notifications |
| Firebase BOM | 34.6.0 | Firebase services |

## CompositionLocal

`LocalEdsConfig` provides `EdsConfig` throughout the composable tree for URL resolution. Screens wrap content with:
```kotlin
CompositionLocalProvider(LocalEdsConfig provides edsConfig) { ... }
```

## Important Files to Modify

| Task | Files |
|------|-------|
| Change EDS site | `data/EdsConfig.kt` |
| Add new block | `blocks/NewBlock.kt`, `blocks/BlockRenderer.kt` |
| Change colors | `theme/Color.kt` |
| Add custom fonts | `theme/Typography.kt`, `composeResources/font/` |
| Modify navigation | `navigation/Routes.kt`, `navigation/AppNavigation.kt` |
| Android app config | `androidMain/AndroidManifest.xml`, `composeApp/build.gradle.kts` |
| iOS app config | `iosApp/iosApp/Info.plist` |
