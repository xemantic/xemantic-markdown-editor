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

import com.xemantic.kotlin.js.dom.invoke
import kotlinx.browser.document
import kotlinx.browser.window

/**
 * Default example markdown to show on first load.
 */
private val DEFAULT_MARKDOWN = """
# Hello, Markdown!

Welcome to the **Xemantic Markdown Editor**.

## Features

- Live preview
- Load and save `.md` files
- Clean, minimal interface

## Example

Write some `code` inline, or a code block:

```
fun hello() = "Hello, World!"
```

> Quotes work too!
""".trimIndent()

/**
 * Main entry point for the Markdown Editor application.
 *
 * Sets up the MVVM architecture by creating the ViewModel and View,
 * then renders the UI.
 */
fun main() {
    val viewModel = MarkdownViewModel(
        notifier = { message -> window.alert(message) }
    )
    val view = markdownEditorView(viewModel)
    document.body!! {
        +view
    }
    viewModel.updateMarkdown(DEFAULT_MARKDOWN)
}
