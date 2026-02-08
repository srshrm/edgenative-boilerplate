# Boilerplate Customization Guide

This guide shows you the **minimal changes** needed to customize this boilerplate for your own app
and publish it
without conflicts.

## ✅ What Works Out of the Box

The boilerplate is configured to build successfully without any changes:

- ✅ Builds on Android, iOS, and Desktop
- ✅ Change `applicationId` without errors
- ✅ All features work out of the box

## Required Changes for Publishing

### 1. Android Application ID

**File:** `androidApp/build.gradle.kts`

```kotlin
android {
    namespace = "com.aem.app"  // Leave as is
    
    defaultConfig {
        applicationId = "com.yourcompany.yourapp"  // CRITICAL: Must be unique on Play Store
        versionCode = 1
        versionName = "1.0"
    }
}
```

**Important:**

- `applicationId` - **MUST** be unique on Google Play Store

### 2. Android App Name

**File:** `androidApp/src/main/res/values/strings.xml`

```xml
<resources>
    <string name="app_name">Your App Name</string>
</resources>
```

### 3. iOS Bundle Identifier

**File:** `iosApp/Configuration/Config.xcconfig`

```
TEAM_ID=YOUR_APPLE_TEAM_ID        # Get from developer.apple.com

PRODUCT_NAME=YourAppName
PRODUCT_BUNDLE_IDENTIFIER=com.yourcompany.yourapp  # CRITICAL: Must be unique on App Store

CURRENT_PROJECT_VERSION=1
MARKETING_VERSION=1.0
```

**Important:**

- `PRODUCT_BUNDLE_IDENTIFIER` - **MUST** be unique on App Store
- `TEAM_ID` - **REQUIRED** for building/signing the app

### 4. Desktop Package Name

**File:** `desktopApp/build.gradle.kts`

```kotlin
compose.desktop {
    application {
        nativeDistributions {
            packageName = "YourAppName"  # Change this
            packageVersion = "1.0.0"
        }
    }
}
```

### 5. Shared Module (No Changes Needed)

**File:** `composeApp/build.gradle.kts`

```kotlin
android {
    namespace = "com.aem"  // Leave as is
}
```

**Note:** The `namespace` and package structure can remain unchanged. The `applicationId` (Android)
and
`PRODUCT_BUNDLE_IDENTIFIER` (iOS) are sufficient to make your app unique.

## Update App Icons/Logo

### Android Icons

**Location:** `androidApp/src/main/res/mipmap-*` folders

Replace launcher icons in all density folders:

- `mipmap-mdpi/` (48x48 dp)
- `mipmap-hdpi/` (72x72 dp)
- `mipmap-xhdpi/` (96x96 dp)
- `mipmap-xxhdpi/` (144x144 dp)
- `mipmap-xxxhdpi/` (192x192 dp)

**Files to replace:**

- `ic_launcher.webp` - Standard launcher icon
- `ic_launcher_round.webp` - Round launcher icon
- `ic_launcher_foreground.webp` - Foreground layer (for adaptive icons)

**Tools:**

- [Icon Kitchen](https://icon.kitchen/)
- Android Studio: Right-click `res` → New → Image Asset

### iOS Icons

**Location:** `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`

**Using Xcode:**

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select `Assets.xcassets` in project navigator
3. Select `AppIcon`
4. Drag and drop icon images (Xcode will resize automatically)

**Icon sizes needed:**

- 20x20 pt, 29x29 pt, 40x40 pt, 60x60 pt, 76x76 pt, 83.5x83.5 pt
- Each at @1x, @2x, @3x resolution

**Tools:**

- [Icon Kitchen](https://icon.kitchen/)
- Xcode built-in resizing

### Desktop Icon

**Location:** `desktopApp/src/main/resources/common/ic_notification.png`

Replace the icon image (recommended size: 256x256 px or larger). For platform-specific icons, update
`desktopApp/build.gradle.kts`:

```kotlin
nativeDistributions {
    macOS {
        iconFile.set(project.file("src/main/resources/common/app_icon.icns"))
    }
    windows {
        iconFile.set(project.file("src/main/resources/common/app_icon.ico"))
    }
    linux {
        iconFile.set(project.file("src/main/resources/common/app_icon.png"))
    }
}
```

**Icon formats:**

- macOS: `.icns` file
- Windows: `.ico` file
- Linux: `.png` file (512x512 px)

## Build Verification

After making changes, verify builds work:

### Android

```bash
./gradlew :androidApp:assembleDebug
# Should complete with "BUILD SUCCESSFUL"
```

### Desktop

```bash
./gradlew :desktopApp:run
# Should launch the app
```

### iOS

```bash
open iosApp/iosApp.xcodeproj
# Build and run from Xcode
```

## Checklist Before Publishing

**Required:**

- [ ] Changed `applicationId` (Android)
- [ ] Changed `PRODUCT_BUNDLE_IDENTIFIER` (iOS)
- [ ] Changed `TEAM_ID` (iOS)
- [ ] Changed app name in `strings.xml` (Android)
- [ ] Changed `PRODUCT_NAME` (iOS)
- [ ] Changed `packageName` (Desktop)
- [ ] Updated EDS site URL in `EdsConfig.kt`
- [ ] Tested build on all target platforms

**Recommended:**

- [ ] Updated app icons/logo (Android mipmap folders, iOS Assets.xcassets, Desktop resources)

**Leave Unchanged:**

- [ ] `namespace` fields (Android/Shared) - Keep as `com.aem`
- [ ] Package folder structure - Keep as is
- [ ] Package declarations in Kotlin files - Keep as is

**Optional:**

- [ ] Customized theme colors
- [ ] Updated splash screens
