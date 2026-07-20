## Testing Capabilities

**Strict TDD Mode**: disabled
**Detected**: 2026-07-19
**Reason**: Greenfield project — no test runner or build system present yet. Expected: JUnit 5, Compose UI Testing, Mockk.

### Test Runner

- Command: `./gradlew test` (not yet available — Gradle project not initialized)
- Framework: JUnit 5 (expected, not detected)

### Test Layers

| Layer       | Available | Tool                          |
| ----------- | --------- | ----------------------------- |
| Unit        | ❌        | — (expected: JUnit 5 + Mockk) |
| Integration | ❌        | — (expected: AndroidX Test)   |
| E2E         | ❌        | — (expected: Compose Testing) |

### Coverage

- Available: ❌
- Command: `./gradlew testDebugUnitTest` (not yet available)

### Quality Tools

| Tool         | Available | Command                  |
| ------------ | --------- | ------------------------ |
| Linter       | ❌        | — (expected: ktlint/detekt) |
| Type checker | ❌        | — (Kotlin is statically typed) |
| Formatter    | ❌        | — (expected: ktlint)     |

### Notes

- Build via GitHub Actions only — no local Android Studio required
- Once `build.gradle.kts` is created, re-run `sdd-init` to update capabilities
- Firebase emulators recommended for local integration testing
- Consider: Robolectric for unit tests without device, Compose Testing for UI assertions
