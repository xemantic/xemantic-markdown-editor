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
import com.xemantic.markanywhere.SemanticEvent
import com.xemantic.markanywhere.js.appendSemanticEvents
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTextAreaElement

/**
 * Creates the markdown editor view with an Obsidian-like unified editing experience.
 *
 * The document is split into blocks (paragraphs, headings, code blocks, etc.).
 * Each block is rendered as HTML. Clicking a block replaces it with a raw-text
 * textarea for editing. When the textarea loses focus the block is saved and
 * re-rendered as HTML.
 *
 * Follows the MVVM pattern: all state lives in [MarkdownViewModel]; this
 * function only binds ViewModel state to DOM elements.
 *
 * @param viewModel The view model providing state to display
 * @return The root DOM node
 */
fun markdownEditorView(
    viewModel: MarkdownViewModel
) = node { div("obsidian-editor") {
    div("obsidian-content") { content ->
        viewModel.scope.launch {
            combine(
                viewModel.parsedBlocks,
                viewModel.focusedBlockIndex
            ) { parsedBlocks, focusedIdx ->
                parsedBlocks to focusedIdx
            }.collectLatest { (parsedBlocks, focusedIdx) ->
                content.innerHTML = ""

                var focusedTextarea: HTMLTextAreaElement? = null
                val previewsToRender = mutableListOf<Pair<HTMLDivElement, Flow<SemanticEvent>>>()

                parsedBlocks.forEachIndexed { index, eventsFlow ->
                    val wrapper = document.createElement("div") as HTMLDivElement
                    wrapper.className = "obsidian-block"

                    if (index == focusedIdx) {
                        val textarea = document.createElement("textarea") as HTMLTextAreaElement
                        textarea.className = "block-textarea"
                        textarea.value = viewModel.blocks.value[index].rawText
                        textarea.addEventListener("blur", { _ ->
                            viewModel.onBlockBlurred(index, textarea.value)
                        })
                        wrapper.appendChild(textarea)
                        focusedTextarea = textarea
                    } else {
                        val preview = document.createElement("div") as HTMLDivElement
                        preview.className = "block-preview"
                        preview.addEventListener("click", { _ ->
                            viewModel.onBlockFocused(index)
                        })
                        wrapper.appendChild(preview)
                        previewsToRender.add(preview to eventsFlow)
                    }

                    content.appendChild(wrapper)
                }

                focusedTextarea?.focus()

                supervisorScope {
                    for ((preview, eventsFlow) in previewsToRender) {
                        launch {
                            preview.appendSemanticEvents(eventsFlow)
                        }
                    }
                }
            }
        }
    }
}}
