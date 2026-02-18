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

import com.xemantic.kotlin.js.dom.NodeBuilder
import com.xemantic.kotlin.js.dom.html.*
import com.xemantic.kotlin.js.dom.node
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.dom.clear
import org.w3c.files.FileReader
import org.w3c.files.get

/**
 * Creates the markdown editor view.
 *
 * Implements MVVM pattern by binding ViewModel state to DOM elements
 * using reactive StateFlow subscriptions.
 *
 * @param viewModel The view model managing markdown state
 * @return The root DOM node
 */
fun markdownEditorView(
    viewModel: MarkdownViewModel
) = node { div("markdown-editor-app") {

    header("app-header") {
        h1 { +"Markdown Editor" }
        div("action-buttons") {

            val fileInput = fileInput(viewModel) // (hidden)
            input(type = "button", value = "Load File") {
                it.onclick = { fileInput.click() }
            }

            input(type = "button", value = "Save .md") {
                it.disabled = true
                it.onclick = { viewModel.saveMd() }
                viewModel.saveMdEnabled.onEach { enabled ->
                    it.disabled = !enabled
                }.launchIn(viewModel.scope)
            }

            input(type = "button", value = "Clear") {
                it.onclick = {
                    viewModel.clear()
                }
            }

        }

    }

    main("app-main") {
        div("editor-panel") {
            h2 { +"Editor" }
            textarea(
                klass = "markdown-editor",
                placeholder = "Enter Markdown here..."
            ) { editor ->
                editor.oninput = {
                    viewModel.updateMarkdown(editor.value)
                }
                viewModel.markdown.onEach { text ->
                    if (editor.value != text) {
                        editor.value = text
                    }
                }.launchIn(viewModel.scope)
            }
        }
        div("preview-panel") {
            h2 { +"Preview" }
            div("markdown-preview") {
                viewModel.previewState.onEach { state ->
                    it.clear()
                    when (state) {
                        is PreviewState.Message -> +state.text
                        is PreviewState.Rendered -> it.innerHTML = state.html
                        is PreviewState.Error -> div("error-message") {
                            +"Error: ${state.message}"
                        }
                    }
                }.launchIn(viewModel.scope)
            }
        }
    }

}}

fun NodeBuilder.fileInput(viewModel: MarkdownViewModel) = input(
    type = "file"
) {
    it.accept = ".md,.txt,.markdown"
    it.style.display = "none"
    it.onchange = { _ ->
        val file = it.files?.get(0)
        if (file != null) {
            val reader = FileReader()
            reader.onload = {
                val content = reader.result as String
                viewModel.loadFromFile(content)
            }
            reader.readAsText(file)
        }
    }
}
