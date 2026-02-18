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

- **ViewModel:** `MarkdownViewModel.kt` - exposes state as `StateFlow` properties
- **View:** `MarkdownEditorView.kt` - constructs DOM using `com.xemantic.kotlin.js.dom` DSL
- **Entry Point:** `main.kt`

### MVVM Design Principles

- The **ViewModel** has no DOM dependency and holds all application state
- The **View** is a pure function: `fun markdownEditorView(viewModel: MarkdownViewModel): Node`
- The ViewModel accepts a `CoroutineDispatcher` and service dependencies via constructor injection, enabling testing without a real browser environment
- State is exposed as `StateFlow<T>` using the `-Xexplicit-backing-fields` pattern:
  ```kotlin
  val markdownText: StateFlow<String>
      field = MutableStateFlow("initial value")
  ```
  This exposes the immutable `StateFlow` type publicly while keeping the mutable backing field private to the class.

### View Binding Pattern

The View uses `kotlinx.coroutines.flow.launchIn` and `onEach` to bind ViewModel state to DOM elements reactively:

```kotlin
viewModel.someState.onEach { value ->
    element.textContent = value
}.launchIn(viewModel.scope)
```

Or `collectLatest` when each new emission should cancel the previous processing:

```kotlin
viewModel.scope.launch {
    viewModel.markdownText.collectLatest { markdown ->
        // rebuilds preview on every markdown change
    }
}
```

## Key Conventions

- Kotlin 2.3 language target with `-Xcontext-parameters`, `-Xexplicit-backing-fields`, `-Xskip-prerelease-check`
- DOM DSL from `com.xemantic.kotlin.js` (`xemantic-kotlin-js`)
- Tests use power-assert with `com.xemantic.kotlin.test.assert` / `have`
- Apache License 2.0 headers required on all source files
- Package: `com.xemantic.markdown.editor`

## Testing

### ViewModel Tests with Mokkery

ViewModel tests use [Mokkery](https://mokkery.dev/) to mock dependencies and `kotlinx-coroutines-test` for deterministic coroutine control. Tests do **not** instantiate the DOM view — business logic is verified in isolation.

Key testing imports:
```kotlin
import dev.mokkery.mock
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
```

Standard test structure (Given-When-Then):
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MarkdownViewModelTest {

    @Test
    fun `should update markdown text when markdown changes`() = runTest {
        // given
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val parser = mock<MarkanywhereParser>()
        val viewModel = MarkdownViewModel(dispatcher, parser)

        // when
        viewModel.onMarkdownChanged("# Hello")

        // then
        assert(viewModel.markdownText.value == "# Hello")
    }
}
```

Key patterns:
- Use `runTest` + `UnconfinedTestDispatcher(testScheduler)` so coroutines run eagerly and synchronously in tests
- Mock interface dependencies with `mock<InterfaceType>()`
- Use `MockMode.autoUnit` for interfaces whose methods all return `Unit`
- Use `everySuspend { ... } returns value` to stub suspend function return values
- Use `verifySuspend(VerifyMode.exhaustiveOrder) { ... }` to assert all expected calls occurred in order
- Call `viewModel.onCleared()` (if present) in `@AfterTest` to cancel the coroutine scope

### Test Source Set Location

Currently all tests are in `jsTest` because `MarkanywhereParser` is a JS-only dependency. If/when the parser is abstracted behind a platform-agnostic interface defined in `commonMain`, ViewModel tests can move to `commonTest` and run on all targets (JVM, Native, JS, WASM) — a key benefit of the MVVM pattern.

### Power-Assert Configuration

The power-assert plugin is configured to transform these assertion functions:
```kotlin
powerAssert {
    functions = listOf(
        "com.xemantic.kotlin.test.assert",
        "com.xemantic.kotlin.test.have"
    )
}
```

Use `assert(condition)` for boolean conditions and `have(condition)` for expectations.

## Dependencies

Key dependencies (see `gradle/libs.versions.toml`):

- `xemantic-kotlin-js` - DOM DSL for Kotlin/JS
- `markanywhere-parse` - Markdown parsing interface and default implementation
- `markanywhere-js` - JS-specific markdown rendering (appending semantic events to DOM)
- `mokkery` (plugin) - Mocking framework for Kotlin Multiplatform tests
- `kotlinx-coroutines-test` - `runTest`, `UnconfinedTestDispatcher` for coroutine testing

## Reference

This project follows the MVVM patterns demonstrated in [xemantic-kotlin-js](https://github.com/xemantic/xemantic-kotlin-js), specifically the test structure in `src/commonTest/kotlin/test/mvvm/`.
