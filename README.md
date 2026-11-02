# AthleteIQ

An Android endurance training prototype built with Kotlin, Jetpack Compose, Material 3 and MVVM.

## Features
- Sign In and Sign Up with email/password validation, password confirmation, and per-account athlete profiles saved in app-private device storage. Passwords use salted PBKDF2 hashes. This is local authentication, not a cloud account.
- Editable athlete profile with name, age, primary sport and weekly availability validation, plus optional weight (kg) and height (cm).
- Add and remove race goals with validated distance and target duration.
- Training dashboard with realistic sample running sessions and weekly distance progress.
- Recommendations using profile availability, sport and calculated race pace.
- Navigation Compose bottom navigation and sign-out flow.

## Structure
- `MainActivity.kt`: activity entry point; original greeting and preview retained.
- `data/AthleteRepository.kt`: typed models and local sample repository.
- `ui/AthleteViewModel.kt`: observable StateFlow, validation and state updates.
- `ui/AthleteApp.kt`: navigation and five Compose screens.
- `ui/theme`: existing application theme.

Accounts and profile edits persist on the device across app restarts. Users sign in again after restarting. Race goals remain in-memory and reset on sign-in or process termination. No backend or wearable integration is connected. Dashboard sessions are illustrative running data, including when another primary sport is selected. Recommendations are rule-based sample content.

## Build and test
Use JDK 25 as configured by the existing Gradle daemon toolchain, Android SDK 37 and the included Gradle wrapper:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Tests cover login/profile/goal validation and ViewModel state updates. Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
