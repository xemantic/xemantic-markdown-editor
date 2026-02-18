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

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Represents the state of the markdown preview panel.
 */
sealed interface PreviewState {

    /**
     * A status message to display.
     *
     * @property text The message text.
     */
    data class Message(val text: String) : PreviewState

    /**
     * Rendering failed with an error.
     *
     * @property message The error message.
     */
    data class Error(val message: String) : PreviewState

    /**
     * The markdown has been successfully rendered to HTML.
     *
     * @property html The rendered HTML string.
     */
    data class Rendered(val html: String) : PreviewState

}

/**
 * ViewModel for managing markdown editor state.
 *
 * Implements MVVM pattern by exposing reactive state through individual StateFlows
 * and providing methods to update the markdown content. Derived flows encapsulate
 * UI decisions so the View can bind directly without logic.
 *
 * @param notifier The notifier used to display messages to the user
 */
class MarkdownViewModel(
    private val notifier: Notifier
) {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val maxContentLength = 100_000
    private val debounceDelayMs = 300L
    private var renderJob: Job? = null

    /**
     * The markdown source text.
     */
    val markdown: StateFlow<String>
        field = MutableStateFlow("")

    private val isRendering = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val renderedHtml = MutableStateFlow<String?>(null)

    /**
     * Whether the Copy HTML button should be enabled.
     */
    val copyHtmlEnabled: StateFlow<Boolean> = renderedHtml
        .map { it != null }
        .stateIn(scope, SharingStarted.Eagerly, false)

    /**
     * Whether the Save .md button should be enabled.
     */
    val saveMdEnabled: StateFlow<Boolean> = markdown
        .map { it.isNotBlank() }
        .stateIn(scope, SharingStarted.Eagerly, false)

    /**
     * The current state of the markdown preview panel.
     */
    val previewState: StateFlow<PreviewState> = combine(
        isRendering, error, renderedHtml, markdown
    ) { rendering, err, html, _ ->
        when {
            rendering -> PreviewState.Message("Rendering...")
            err != null -> PreviewState.Error(err)
            html != null -> PreviewState.Rendered(html)
            else -> PreviewState.Message("Start typing to see a preview")
        }
    }.stateIn(scope, SharingStarted.Eagerly, PreviewState.Message("Start typing to see a preview"))

    /**
     * Updates the markdown source text.
     *
     * Validates that the content length doesn't exceed the maximum allowed length.
     * Triggers debounced rendering.
     *
     * @param text The new markdown text
     */
    fun updateMarkdown(text: String) {
        val validated = if (text.length > maxContentLength) {
            console.warn("Markdown content exceeds maximum length of $maxContentLength characters. Truncating.")
            text.take(maxContentLength)
        } else {
            text
        }

        markdown.value = validated
        renderedHtml.value = null
        error.value = null

        renderJob?.cancel()
        renderJob = scope.launch {
            delay(debounceDelayMs)
            renderMarkdown()
        }
    }

    /**
     * Renders the current markdown using marked.js.
     */
    private fun renderMarkdown() {
        val currentMarkdown = markdown.value

        if (currentMarkdown.isBlank()) {
            renderedHtml.value = null
            error.value = null
            isRendering.value = false
            return
        }

        isRendering.value = true
        error.value = null

        try {
            val html = MarkedModule.marked(currentMarkdown)
            renderedHtml.value = html
            error.value = null
            isRendering.value = false
        } catch (e: Throwable) {
            console.error("Failed to render markdown:", e)
            renderedHtml.value = null
            error.value = e.message ?: "Failed to render markdown"
            isRendering.value = false
        }
    }

    /**
     * Copies the rendered HTML to clipboard.
     */
    fun copyHtml() {
        val html = renderedHtml.value
        if (html != null) {
            notifier.notify("HTML copied to clipboard!")
        } else {
            notifier.notify("No rendered HTML to copy.")
        }
    }

    /**
     * Saves the current markdown as a .md file.
     */
    fun saveMd() {
        val currentMarkdown = markdown.value
        if (currentMarkdown.isNotBlank()) {
            exportMd(currentMarkdown)
        } else {
            notifier.notify("No markdown content to save.")
        }
    }

    /**
     * Loads markdown content from a file.
     */
    fun loadFromFile(content: String) {
        updateMarkdown(content)
    }

    /**
     * Clears the current markdown content.
     */
    fun clear() {
        renderJob?.cancel()
        markdown.value = ""
        renderedHtml.value = null
        error.value = null
        isRendering.value = false
    }

}
