# Firebase Setup Guide

This boilerplate is configured to **build and run without Firebase**. Push notifications will not
work until you
complete Firebase setup.

## Current Status

The app includes placeholder Firebase configuration files:

- `androidApp/google-services.json` (Android)
- `iosApp/iosApp/GoogleService-Info.plist` (iOS)

These placeholder files allow you to:

- ✅ Build the app successfully
- ✅ Change `applicationId` without errors
- ✅ Run and test the app
- ❌ Push notifications won't work (requires real Firebase setup)

## How It Works

### Android

The Google Services plugin is applied **conditionally** in `androidApp/build.gradle.kts`:

```kotlin
// Apply Google Services plugin only if real Firebase configuration exists
val googleServicesFile = file("google-services.json")
val hasRealFirebaseConfig = googleServicesFile.exists() &&
        !googleServicesFile.readText().contains("YOUR_PROJECT_ID")

if (hasRealFirebaseConfig) {
    apply(plugin = libs.plugins.googleServices.get().pluginId)
}
```

This checks if `google-services.json`:

1. Exists
2. Doesn't contain placeholder text "YOUR_PROJECT_ID"

If both conditions are met, the Google Services plugin is applied. Otherwise, it's skipped.

### iOS

Firebase initialization is conditional in `iosApp/iosApp/iOSApp.swift`:

```swift
// Initialize Firebase only if properly configured
if isFirebaseConfigured() {
    FirebaseApp.configure()
    print("✅ Firebase initialized successfully")
} else {
    print("⚠️ Firebase not configured - using placeholder GoogleService-Info.plist")
    print("   Push notifications will not work until you add a real Firebase configuration")
}
```

When you run the iOS app, check the console output to see if Firebase is configured.

## Setting Up Firebase (Optional)

If you want to enable push notifications:

### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Add project" or use an existing project
3. Follow the setup wizard

### Step 2: Add Android App

1. In Firebase Console, click "Add app" → Android (⚙️ icon)
2. **Package name**: Use your `applicationId` from `androidApp/build.gradle.kts`
    - Example: `com.yourcompany.yourapp`
3. Download `google-services.json`
4. Replace `androidApp/google-services.json` with the downloaded file
5. Rebuild the app - Google Services plugin will now be applied automatically

### Step 3: Add iOS App

1. In Firebase Console, click "Add app" → iOS (🍎 icon)
2. **Bundle ID**: Use your `PRODUCT_BUNDLE_IDENTIFIER` from `iosApp/Configuration/Config.xcconfig`
    - Example: `com.yourcompany.yourapp`
3. Download `GoogleService-Info.plist`
4. Replace `iosApp/iosApp/GoogleService-Info.plist` with the downloaded file
5. Rebuild the app - Firebase will now be initialized automatically

### Step 4: Verify Setup

**Android:**

```bash
./gradlew :androidApp:assembleDebug
# Should build without "No matching client found" error
```

**iOS:**
Run the app from Xcode and check the console output:

- ✅ Success: `Firebase initialized successfully`
- ❌ Not configured: `Firebase not configured - using placeholder`

## Troubleshooting

### Error: "No matching client found for package name"

This means the Google Services plugin is being applied but the `package_name` in
`google-services.json` doesn't match
your `applicationId`.

**Solutions:**

1. **Use placeholder file** (current setup): The plugin won't be applied, app will build
   successfully
2. **Update Firebase config**: Create a new Android app in Firebase with your new `applicationId`

### Changed applicationId but want to keep Firebase

If you changed `applicationId` and want to keep using Firebase:

1. Go to Firebase Console → Project Settings
2. Add a new Android app with the new `applicationId`
3. Download the new `google-services.json`
4. Replace the old file

**Note:** The same Firebase project can have multiple Android apps (e.g., debug/release variants
with different IDs).

### iOS App Won't Build

If iOS build fails with Firebase errors:

1. Check that `GoogleService-Info.plist` is in `iosApp/iosApp/` directory
2. Verify the file is added to the Xcode project target
3. Clean build folder in Xcode (Shift+Cmd+K)
4. Rebuild

### Testing Without Firebase

You can fully test the app without Firebase:

- Content loading from EDS ✅
- Navigation ✅
- All blocks rendering ✅
- Theming ✅
- Push notifications ❌ (requires Firebase)