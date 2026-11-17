# Repository AI Instructions

## Kotlin/Java File Headers

Always include the standard Bread Financial copyright header in every newly generated Kotlin or Java file under `src/main/kotlin/`, `src/main/java/`, `src/test/kotlin/`, or `src/test/java/`. Keep it at the top of the file, use the actual filename, and preserve the field order and wording below:

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

Until the refactor project is explicitly complete, use `31 December 2026` for `{Date}` in every new Kotlin/Java header, even if the file is created later. Do not substitute the current date. Use `2026` for `{Year}`. Keep these values identical across the repository unless the user explicitly changes this policy. For `build.gradle.kts` or `build.gradle`, do not add the copyright header; these are configuration files and use their standard format.


## Target Architecture

- `BreadPartnersCore` module contains reusable Kotlin/Java domain and infrastructure code: models, business rules, services, networking abstractions, and HTML parsing. It is built and tested as a pure JVM module (no Android dependencies) and used by the Android app. Keep Android Framework classes (Activities, Fragments, Views, Services), platform-specific behavior, and Android-only dependencies out of BreadPartnersCore. Keep HTML parsing utilities (such as Jsoup) in BreadPartnersCore.
- `BreadPartners` (or `app`) module is the Android integration layer. It owns UI components, platform-specific implementations, Android SDK composition, device integrations, and adapters that connect those implementations to BreadPartnersCore services. It depends on the `BreadPartnersCore` module and Android-specific dependencies such as AndroidX libraries and Recaptcha.
- Put platform-independent behavior in the `BreadPartnersCore` module and keep Android Framework concerns, UI, and device-specific code in the `BreadPartners` module. Test BreadPartnersCore changes with JUnit unit tests (no Android Framework required); test Android integration and UI changes with Espresso or instrumentation tests on an emulator or device.
- When a type or member must be used by another module in this package, mark it `internal` (Kotlin) or use package-private/`public` visibility (Java) with documentation. Do not assume a synthesized constructor has cross-module visibility; add an explicit public/internal constructor when cross-module construction is needed.

