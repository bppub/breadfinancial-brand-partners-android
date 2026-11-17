# Repository AI Instructions

## Kotlin/Java File Headers

Always include the standard Bread Financial copyright header in every newly generated Kotlin or Java file under `src/main/`, `src/test/`, or `src/androidTest/` in any module (`BreadPartnersSDK` or `app`). Keep it at the top of the file, use the actual filename, and preserve the field order and wording below:

```kotlin
/*
 * File:          {FileName}.kt
 * Author(s):     Bread Financial
 * Date:          {Date}
 *
 * Descriptions:  This file is part of the BreadPartners SDK for Android,
 * providing UI components and functionalities to integrate Bread Financial
 * services into partner applications.
 *
 * © {Year} Bread Financial
 */
```

For Java files, use the same header structure with `.java` extension:

```java
/*
 * File:          {FileName}.java
 * Author(s):     Bread Financial
 * Date:          {Date}
 *
 * Descriptions:  This file is part of the BreadPartners SDK for Android,
 * providing UI components and functionalities to integrate Bread Financial
 * services into partner applications.
 *
 * © {Year} Bread Financial
 */
```

Until the refactor project is explicitly complete, use `31 December 2026` for `{Date}` in every new Kotlin/Java header, even if the file is created later. Do not substitute the current date. Use `2026` for `{Year}`. Keep these values identical across the repository unless the user explicitly changes this policy. Do not add the header to `build.gradle.kts`, `settings.gradle.kts`, `libs.versions.toml`, or other configuration files.


## Target Architecture

- The repo has two Gradle modules: `BreadPartnersSDK` (the shipped Android library, published via JitPack) and `app` (the sample/test app, never published). `app` depends on `BreadPartnersSDK` through `implementation(project(":BreadPartnersSDK"))`. Nothing in `BreadPartnersSDK` may depend on `app`.
- `BreadPartnersSDK` is a single module with two layers separated by package under `com.breadfinancial.breadpartners.sdk`:
  - **Platform-independent layer** (pure Kotlin/JVM, no `android.*`, `androidx.*`, Glide, or Recaptcha imports): `networking/`, `analytics/`, the pure data parts of `core/models/`, and HTML parsing (`HTMLContentParser`, using Jsoup). It holds models, business rules, request building and response handling, and networking abstractions.
  - **Android layer** (may use the Android framework): UI and platform code such as `htmlhandling/uicomponents/`, `ChallengeDialog`, `HTMLContentRenderer`, `LoaderIndicator`, `security/` (Recaptcha), `utilities/imageloader/` (Glide, SVG), and the `BreadPartnersSDK` entry point. It owns SDK composition, device integrations, and the adapters that connect Android implementations (logging, colors/typefaces, Recaptcha, `Context`) to platform-independent code.
- The platform-independent layer must never import from the Android layer. The Android layer may depend on the platform-independent layer. When the platform-independent layer needs an Android capability, define an interface in the platform-independent layer and implement it in the Android layer.
- When touching an existing file that mixes both concerns (for example models using `Color`/`Typeface`, `Logger`, `CommonUtils`), split it so the pure part moves to the platform-independent layer and the Android-specific part stays in the Android layer.
- Do not change the public API or package names that partners import (see `README.md`, e.g. `com.breadfinancial.breadpartners.sdk.core.BreadPartnersSDK`). If a type must move or be renamed, keep source compatibility with a `typealias`.
- Use `internal` for anything not part of the documented public API. When code in another module (`app`) must use a type or member, make it `public` and add an explicit public constructor. Do not rely on a default or synthesized constructor having the visibility you need.
- Test platform-independent code with JUnit unit tests in `src/test` (no Android framework, mocks, or Robolectric). Test Android integration and UI with instrumented tests in `src/androidTest` (Espresso) on an emulator or device. The `app` module is for manual and integration verification only.
- Do not add a separate `BreadPartnersCore` module unless the user explicitly decides to split the module.

