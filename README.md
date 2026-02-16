# EdgeNative Boilerplate

A Kotlin Multiplatform (KMP) boilerplate for
migrating [AEM Edge Delivery Services (EDS)](https://www.aem.live/) sites
to native **Android**, **iOS**, and **Desktop (JVM)** applications. This starter project provides a
complete foundation
for rendering EDS content natively using Compose Multiplatform.

## What It Does

This boilerplate fetches content from AEM EDS sites via their `.plain.html` endpoints and renders it
natively on all platforms using [ksoup](https://github.com/fleeksoft/ksoup) for HTML parsing. It
includes:

- **Block Rendering System** - Native UI components for common EDS blocks (Hero, Cards, Columns)
- **Navigation** - Type-safe routing with Navigation 3 and deep linking support
- **Theming** - Material 3 design with customizable colors and typography
- **Image Loading** - Efficient image handling with Coil
- **Network Layer** - Ktor-based HTTP client with platform-specific engines
- **Library Export Ready** - Can be used as a KMP library in existing Android, iOS, and Desktop apps

## Quick Start

> **Note:** This boilerplate builds successfully without any configuration changes. Follow these
> steps to customize it
> for your app.

### 1. Configure Your EDS Site

Update `composeApp/src/commonMain/kotlin/com/aem/data/EdsConfig.kt`:

```kotlin
val DefaultEdsConfig = EdsConfig(
    siteUrl = "https://your-site.aem.live",
    homePath = "",  // Optional: custom home page path (e.g., "emea/en/products")
)
```

### 2. Update App Identifiers (Required for Publishing)

**Android** (`androidApp/build.gradle.kts`):

```kotlin
android {
  defaultConfig {
    applicationId = "com.yourcompany.yourapp"  // Must be unique on Play Store
  }
}
```

**iOS** (`iosApp/Configuration/Config.xcconfig`):

```
TEAM_ID=YOUR_APPLE_TEAM_ID
PRODUCT_NAME=YourAppName
PRODUCT_BUNDLE_IDENTIFIER=com.yourcompany.yourapp  # Must be unique on App Store
```

**Desktop** (`desktopApp/build.gradle.kts`):

```kotlin
nativeDistributions {
  packageName = "YourAppName"
}
```

**Note:** Leave `namespace` and package structure unchanged. Only the application identifiers above
need to be
customized.

### 3. Update App Icons/Logo (Recommended)

**Android:**

- Replace launcher icons in `androidApp/src/main/res/mipmap-*` folders
- Use [Icon Kitchen](https://icon.kitchen/) to generate all icon sizes
- Or use Android Studio: Right-click `res` → New → Image Asset

**iOS:**

- Replace icons in `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`
- Use Xcode: Open `Assets.xcassets` → Select `AppIcon` → Drag and drop your icon
- Or use [Icon Kitchen](https://icon.kitchen/) to generate all iOS icon sizes

**Desktop:**

- Replace icon at `desktopApp/src/main/resources/common/ic_notification.png`
- Update the icon reference in `desktopApp/build.gradle.kts` under `nativeDistributions`

**For detailed guides:**

- 📋 [Complete Customization Checklist](./BOILERPLATE_CUSTOMIZATION.md)
- 📦 [Using as a KMP Library](./LIBRARY_EXPORT.md) - Integrate into existing apps

### 4. Run the App

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
edgenative-boilerplate/
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

## Usage Modes

This boilerplate can be used in two ways:

### 1. Standalone Apps (Default)

Build complete Android, iOS, and Desktop applications

### 2. KMP Library

Integrate the `composeApp` module into existing apps as a library. The module is already configured
with
`com.android.kotlin.multiplatform.library` plugin and ready for export.

See [**LIBRARY_EXPORT.md**](./LIBRARY_EXPORT.md) for:

- Publishing to Maven Local/Central or GitHub Packages
- Integration examples for existing Android, iOS, and Desktop apps
- API configuration and dependency management
- ProGuard rules and troubleshooting

**Quick example:**

```bash
# Publish to Maven Local
./gradlew :composeApp:publishToMavenLocal

# Use in existing Android app
implementation("com.aem:composeApp:1.0.0")
```

## Customization

### Add Custom EDS Blocks

1. Create a new composable in `composeApp/src/commonMain/.../blocks/YourBlock.kt`
2. Add it to `blocks/BlockRenderer.kt`

### Update Branding

- **Colors**: Edit `composeApp/.../theme/Color.kt`
- **Typography**: Edit `composeApp/.../theme/Typography.kt`
- **App Name**:
    - Android: `androidApp/src/main/res/values/strings.xml`
    - iOS: `iosApp/iosApp/Info.plist` (edit for visible name), plus
      `iosApp/Configuration/Config.xcconfig` (
      `PRODUCT_NAME`)
    - Desktop: `desktopApp/src/main/kotlin/main.kt`

## Documentation

For detailed architecture, migration guides, and development instructions,
see [CLAUDE.md](./CLAUDE.md).

## Tech Stack

| Component             | Version       | Purpose                   |
|-----------------------|---------------|---------------------------|
| Kotlin                | 2.3.0         | Language                  |
| Compose Multiplatform | 1.10.0        | Shared UI framework       |
| AGP                   | 9.0.0         | Android Gradle Plugin     |
| Gradle                | 9.2.1         | Build system              |
| Ktor                  | 3.4.0         | Networking                |
| Ksoup                 | 0.2.5         | HTML Parsing (plain.html) |
| Koin                  | 4.1.1         | Dependency Injection      |
| Coil                  | 3.3.0         | Image Loading             |
| Navigation 3          | 1.0.0-alpha06 | Type-safe Navigation      |

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
about [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform/get-started.html)
and [AEM Edge Delivery Services](https://www.aem.live/).
