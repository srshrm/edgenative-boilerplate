# Using as a KMP Library

The `composeApp` module is **already configured as a KMP library** and can be integrated into
existing Android, iOS, and
Desktop apps.

## Quick Start

### 1. Add Maven Publishing

Add to `composeApp/build.gradle.kts`:

```kotlin
plugins {
    // ... existing plugins
    id("maven-publish")
}

group = "com.adobe.aem_kmp_boilerplate"
version = "1.0.0"

publishing {
    repositories {
        mavenLocal()
    }
}
```

### 2. Publish

```bash
./gradlew :composeApp:publishToMavenLocal
```

Publishes to `~/.m2/repository/` as:

- `composeApp-android` (Android AAR)
- `composeApp-jvm` (Desktop JAR)
- `composeApp-iosarm64` (iOS framework)
- `composeApp` (Root - auto-resolves to platform)

## Integration Examples

### Android

**1. Add Maven Local** (`settings.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}
```

**2. Add dependency** (`app/build.gradle.kts`):

```kotlin
dependencies {
    implementation("com.adobe.aem_kmp_boilerplate:composeApp:1.0.0")
}
```

**3. Use in Activity:**

```kotlin
import com.adobe.aem_kmp_boilerplate.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
```

### iOS

**1. Build framework:**

```bash
./gradlew :composeApp:linkReleaseFrameworkIosArm64
```

**2. In Xcode:**

- Drag `composeApp/build/XCFrameworks/release/ComposeApp.xcframework` to project
- Select "Embed & Sign"

**3. Use in SwiftUI:**

```swift
import ComposeApp

struct ComposeAppView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

### Desktop

**1. Add Maven Local** (`settings.gradle.kts`):

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}
```

**2. Add dependency:**

```kotlin
dependencies {
    implementation("com.adobe.aem_kmp_boilerplate:composeApp-jvm:1.0.0")
}
```

**3. Use in main:**

```kotlin
import com.adobe.aem_kmp_boilerplate.App

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
```

## Publishing to Maven Central / GitHub Packages

### Maven Central

```kotlin
publishing {
    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String?
                password = project.findProperty("ossrhPassword") as String?
            }
        }
    }
}

plugins { signing }
signing { sign(publishing.publications) }
```

### GitHub Packages

```kotlin
publishing {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/USERNAME/REPOSITORY")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

Then: `./gradlew :composeApp:publish`

## Configuration Notes

### Exposing Dependencies

The library uses `api` for consumer-facing dependencies:

```kotlin
commonMain.dependencies {
    api(libs.kmpnotifier)  // Exposed to consumers
    implementation(libs.ktor.client.core)  // Internal only
}
```

To expose more, change `implementation` to `api`.

### iOS Framework Export

```kotlin
iosTarget.binaries.framework {
    baseName = "ComposeApp"
    isStatic = true
    export(libs.kmpnotifier)  // Accessible from Swift
}
```

## ProGuard Rules (Android)

Add to `proguard-rules.pro`:

```proguard
-keep class androidx.compose.** { *; }
-keep class io.ktor.** { *; }
-keep class coil3.** { *; }
-keep class org.koin.** { *; }
-keepattributes *Annotation*
-keep,includedescriptorclasses class com.adobe.aem_kmp_boilerplate.**$$serializer { *; }
```

## Troubleshooting

**Android - Duplicate classes:**

```kotlin
implementation("...") {
    exclude(group = "androidx.compose.ui", module = "ui")
}
```

**iOS - Framework not found:**

1. Clean: Shift+Cmd+K in Xcode
2. Rebuild: `./gradlew :composeApp:linkReleaseFrameworkIosArm64`
3. Re-embed framework

## Resources

- [KMP Library Publishing](https://kotlinlang.org/docs/multiplatform/multiplatform-publish-lib-setup.html)
