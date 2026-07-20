# Market — Lista de Compras Compartida

A shared shopping list app for households. Create a household, invite members via code, and collaborate on shopping lists with real-time sync.

## Features

- **Google Sign-In** — one-tap authentication with Firebase Auth
- **Household Management** — create or join a household with a 6-char invite code
- **Shared Shopping List** — add items, assign stores, real-time Firestore sync
- **Offline Support** — add and view items without network; auto-sync on reconnect
- **Store Management** — custom store ordering, drag-and-drop reorder
- **Price Comparison** — record prices per item per store, see cheapest options
- **Purchase History** — complete trips, view past purchases with totals
- **Role-based Access** — admin vs member permissions
- **Dark Mode** — Material 3 dynamic color with teal (#0D9488) fallback

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt (Dagger) |
| Backend | Firebase (Auth, Firestore) |
| Navigation | Navigation Compose |
| CI/CD | GitHub Actions |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 |
| JDK | 17 |

## Setup

### 1. Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/) and create a project (or use an existing one)
2. Enable **Authentication** → Sign-in method → Google
3. Enable **Cloud Firestore** (start in test mode for development)
4. Add an Android app with package name `com.market`
5. Download `google-services.json` and place it in `app/`

### 2. google-services.json

The file `app/google-services.json` is gitignored. You must provide it yourself:

```
Market/
└── app/
    └── google-services.json   ← from Firebase Console
```

### 3. Signing (Release Builds)

1. Copy `keystore.properties.example` to `keystore.properties`
2. Fill in your keystore credentials
3. Place your `.keystore` file at the path specified in `storeFile`

**Debug builds** use the default Android debug keystore — no configuration needed.

### 4. Required GitHub Secrets (for CI/CD)

| Secret | Description |
|--------|-------------|
| `GOOGLE_SERVICES_JSON` | Base64-encoded `google-services.json` |
| `RELEASE_KEYSTORE_BASE64` | Base64-encoded release keystore file |
| `KEYSTORE_STORE_PASSWORD` | Keystore store password |
| `KEYSTORE_KEY_PASSWORD` | Key password |
| `KEYSTORE_KEY_ALIAS` | Key alias |

To encode a file for secrets:
```bash
base64 -i google-services.json | pbcopy   # macOS
base64 -i google-services.json            # Linux, copy output
```

## Build

### Debug

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

### Release

Requires `keystore.properties` and the release keystore file.

```bash
./gradlew assembleRelease
```

APK output: `app/build/outputs/apk/release/app-release.apk`

## CI/CD

### Build (`.github/workflows/build.yml`)

- **PRs to main**: builds debug APK, uploads as artifact (7-day retention)
- **Push to main**: builds signed release APK, uploads as artifact (30-day retention)
- Caches Gradle dependencies for faster builds

### Release (`.github/workflows/release.yml`)

- **Tag push** (`v*`): builds signed release APK and creates a GitHub Release
- Tag a release:
  ```bash
  git tag v1.0.0
  git push origin v1.0.0
  ```

## Project Structure

```
Market/
├── app/
│   ├── build.gradle.kts          # App module config, signing, dependencies
│   ├── proguard-rules.pro        # R8/ProGuard keep rules
│   └── src/main/java/com/market/
│       ├── MarketApp.kt          # @HiltAndroidApp Application
│       ├── MainActivity.kt       # Single-activity entry point
│       ├── data/                 # Data layer (Firestore data sources, repos)
│       ├── di/                   # Hilt modules
│       ├── domain/               # Domain layer (models, repos, use cases)
│       └── presentation/         # UI (screens, components, theme, nav)
├── .github/workflows/            # CI/CD pipelines
├── build.gradle.kts              # Root Gradle config
├── gradle/wrapper/               # Gradle wrapper
├── gradlew / gradlew.bat         # Gradle wrapper scripts
└── keystore.properties.example   # Signing config template
```

## License

MIT
