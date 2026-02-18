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

import com.xemantic.kotlin.js.dom.html.*
import com.xemantic.kotlin.js.dom.node

/**
 * Creates the markdown editor view.
 *
 * Implements MVVM pattern by binding ViewModel state to DOM elements.
 *
 * @param viewModel The view model providing state to display
 * @return The root DOM node
 */
fun markdownEditorView(
    viewModel: MarkdownViewModel
) = node { div("markdown-editor-app") {
    div("greeting") {
        +viewModel.greeting
    }
}}
