# xemantic-markdown-editor

## Build Commands

- `./gradlew build` - compile and run tests
- `./gradlew jsBrowserDevelopmentRun --continuous` - hot-reload development server
- `./gradlew jsTest` - run tests
- `./gradlew apiCheck` - check API binary compatibility
- `./gradlew apiDump` - update API dump

CI runs `./gradlew build` on PRs.

## Project Overview

A Kotlin/JS browser application compiled to a single JavaScript executable. The application loads via `src/jsMain/resources/index.html` and has no npm dependencies.

## Architecture

MVVM pattern:

- **ViewModel:** `MarkdownViewModel.kt` - exposes state as properties
- **View:** `MarkdownEditorView.kt` - constructs DOM using `com.xemantic.kotlin.js.dom` DSL
- **Entry Point:** `main.kt`

## Key Conventions

- Kotlin 2.3 language target with `-Xcontext-parameters`, `-Xexplicit-backing-fields`, `-Xskip-prerelease-check`
- DOM DSL from `com.xemantic.kotlin.js` (`xemantic-kotlin-js`)
- Tests use power-assert with `com.xemantic.kotlin.test.assert` / `have`
- Apache License 2.0 headers required on all source files
- Package: `com.xemantic.markdown.editor`
