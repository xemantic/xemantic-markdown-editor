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
import com.xemantic.kotlin.js.dom.event.onInput
import com.xemantic.kotlin.js.dom.html.*
import com.xemantic.markanywhere.SemanticEvent
import com.xemantic.markanywhere.js.appendSemanticEvents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.dom.clear

/**
 * Creates the markdown editor view.
 *
 * Implements an Obsidian-like split-pane layout with a markdown editor on the left
 * and a rendered HTML preview on the right. Follows the MVVM pattern by binding
 * ViewModel state to DOM elements.
 *
 * @param viewModel The view model providing state to display
 * @return The root DOM node
 */
fun NodeBuilder<*>.markdownEditorView(
    viewModel: MarkdownViewModel
) {

    div("grid") {

        section("s6 small-padding") {
            textarea(
                "x-editor surface-container small-padding",
                name = "Markdown Editor",
                placeholder = "Start typing your markdown here..."
            ) {
                node.value = viewModel.markdownText.value
                onInput {
                    viewModel.onMarkdownChanged(node.value)
                }
            }
        }

        section("s6") {
            div("x-preview small-padding scroll") {
                viewModel.scope.launch {
                    viewModel.parsedMarkdown.collectLatest { events ->
                        node.clear()
                        node.appendSemanticEvents(
                            events
                                .addTableStripes()
                                .renderMath()
                        )
                    }
                }
            }
        }

    }

}

private fun Flow<SemanticEvent>.addTableStripes() = map {
    if (it is Mark && it.name == "table") {
        it.addClass("stripes")
    } else {
        it
    }
}

private fun SemanticEvent.Mark.addClass(
    className: String
): SemanticEvent.Mark {
    val existingClass = attributes?.get("class")
    val newClass = if (existingClass != null) "$existingClass $className" else className
    return copy(attributes = (attributes ?: emptyMap()) + ("class" to newClass))
}
