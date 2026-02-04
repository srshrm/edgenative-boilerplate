# CLAUDE.md

Kotlin Multiplatform app rendering AEM Edge Delivery Services content natively on Android, iOS, and
Desktop.

**Package**: `com.adobe.aem_kmp_boilerplate`

## Install/Build Commands

```bash
# Android
./gradlew :androidApp:installDebug

# Desktop
./gradlew :desktopApp:run

# iOS (macOS only)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
open iosApp/iosApp.xcodeproj

# Full build
./gradlew build
```

## Project Structure

```
composeApp/src/commonMain/kotlin/com/adobe/aem_kmp_boilerplate/
├── App.kt              # Main entry composable
├── blocks/             # EDS block renderers (HeroBlock, CardsBlock, etc.)
├── data/               # Models (EdsPage, ContentNode) + EdsConfig
├── navigation/         # Routes + LinkHandler (Navigation 3)
├── network/            # EdsApiService + HTTP client
├── screens/            # Screen composables
└── theme/              # Color, Typography, Spacing tokens
```

**Platform modules**: `androidApp/`, `desktopApp/`, `iosApp/`

## Architecture

```
EdsConfig → EdsApiService → JSON → EdsPage → SectionRenderer → BlockRenderer
```

| Component     | File                       | Purpose                           |
|---------------|----------------------------|-----------------------------------|
| EdsConfig     | `data/EdsConfig.kt`        | Site URL, home path, JSON service |
| EdsApiService | `network/EdsApiService.kt` | Fetch pages via JSON service      |
| ContentParser | `data/ContentParser.kt`    | Parse JSON to models              |
| BlockRenderer | `blocks/BlockRenderer.kt`  | Dispatch to block composables     |

## Adding a Block

1. Create `blocks/YourBlock.kt`:

```kotlin
@Composable
fun YourBlock(rows: List<BlockRow>, onLinkClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val edsConfig = LocalEdsConfig.current
    // Render UI
}
```

2. Add case in `BlockRenderer.kt`:

```kotlin
blockName.contains("yourblock") -> YourBlock(rows = blockContent, onLinkClick = onLinkClick)
```

## Key Files to Modify

| Task            | Files                                           |
|-----------------|-------------------------------------------------|
| Change EDS site | `data/EdsConfig.kt`                             |
| Add block       | `blocks/NewBlock.kt`, `blocks/BlockRenderer.kt` |
| Change colors   | `theme/Color.kt`                                |
| Add fonts       | `theme/Typography.kt`, `composeResources/font/` |
| Modify nav      | `navigation/Routes.kt`, `AppNavigation.kt`      |

## EdsConfig Example

```kotlin
val DefaultEdsConfig = EdsConfig(
    siteUrl = "https://www.example.com",
    homePath = "emea/en/products",  // Optional: custom home page
    jsonServiceUrl = "https://mhast-html-to-json.aemrnd.workers.dev"
)
```

## Navigation

- **Navigation 3** with type-safe routes (`Home`, `PageDetail(path)`)
- **ModalNavigationDrawer** for global menu (hamburger icon)
- **LinkHandler**: internal URLs → Navigation, external → system browser

## Theme

| File            | Purpose                   |
|-----------------|---------------------------|
| `Color.kt`      | Brand colors (light/dark) |
| `Typography.kt` | Font families             |
| `Spacing.kt`    | 8dp grid tokens           |

## Platform-Specific (expect/actual)

| Declaration                  | Purpose                  |
|------------------------------|--------------------------|
| `createPlatformHttpClient()` | HTTP engine per platform |
| `openUrl(url)`               | System browser           |

## Dependencies

Compose Multiplatform 1.10.0, Kotlin 2.3.0, AGP 9.0, Ktor 3.3.3, Coil 3.3.0, Navigation 3

## Claude Code Permissions

Pre-approved commands in `.claude/settings.json` for EDS to KMP migration workflow:

| Category   | Allowed Commands                                        |
|------------|---------------------------------------------------------|
| Gradle     | `./gradlew *` (assemble, build, installDebug, etc.)     |
| Git        | status, diff, log, add, commit, branch, checkout, stash |
| GitHub CLI | `gh pr *`, `gh issue *`                                 |
| Mobile     | `adb *`, `xcrun simctl *`                               |
| Files      | Read/Edit/Write all project files                       |
| MCP Tools  | Playwright, KMP Migrator, Mobile MCP                    |

Protected: `.env`, `.env.*`, `local.properties` (read denied)