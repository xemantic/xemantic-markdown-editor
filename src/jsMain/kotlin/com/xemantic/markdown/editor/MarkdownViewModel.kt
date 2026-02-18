/*
 * Copyright 2025 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.markdown.editor

import com.xemantic.markanywhere.parse.DefaultMarkanywhereParser
import com.xemantic.markanywhere.parse.MarkanywhereParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the markdown editor application.
 *
 * Exposes reactive state through [StateFlow]s following the MVVM pattern.
 * The View observes these flows and updates the DOM accordingly.
 *
 * @param dispatcher The [CoroutineDispatcher] to use for the coroutine scope.
 *   Defaults to [Dispatchers.Default]. Pass [kotlinx.coroutines.Dispatchers.Unconfined]
 *   in tests for synchronous execution without requiring a test dispatcher.
 */
class MarkdownViewModel(
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    val scope = CoroutineScope(SupervisorJob() + dispatcher)

    val parser: MarkanywhereParser = DefaultMarkanywhereParser()

    private val _markdownText = MutableStateFlow(
        """# Welcome to Markdown Editor

Start typing your markdown here...

## Features

- **Bold** text
- *Italic* text
- `inline code`

## Code Block

```kotlin
fun main() {
    println("Hello, World!")
}
```

> This is a blockquote

---

[Link example](https://example.com)
""".trimIndent()
    )

    val markdownText: StateFlow<String> = _markdownText.asStateFlow()

    fun onMarkdownChanged(text: String) {
        _markdownText.value = text
    }

}
