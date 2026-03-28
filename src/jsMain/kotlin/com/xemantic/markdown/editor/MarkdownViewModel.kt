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

import com.xemantic.markanywhere.SemanticEvent
import com.xemantic.markanywhere.parse.DefaultMarkanywhereParser
import com.xemantic.markanywhere.parse.MarkanywhereParser
import com.xemantic.markanywhere.parse.parse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Represents a single block of markdown content such as a paragraph, heading,
 * code block, or other block-level element separated by blank lines.
 *
 * @param index The position of this block in the document.
 * @param rawText The raw markdown text for this block (without surrounding blank lines).
 */
data class MarkdownBlock(
    val index: Int,
    val rawText: String
)

/**
 * ViewModel for the markdown editor application.
 *
 * Exposes reactive state through [StateFlow]s following the MVVM pattern.
 * Implements an Obsidian-like editing experience where content is divided
 * into blocks. Each block is rendered as HTML and switches to a raw-text
 * editor when focused.
 *
 * @param dispatcher The [CoroutineDispatcher] to use for the coroutine scope.
 *   Defaults to [Dispatchers.Default]. Pass [kotlinx.coroutines.test.UnconfinedTestDispatcher]
 *   in tests for synchronous execution without requiring a real event loop.
 * @param parser The [MarkanywhereParser] to use for parsing markdown.
 *   Defaults to [DefaultMarkanywhereParser]. Can be mocked in tests with Mokkery.
 */
class MarkdownViewModel(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val parser: MarkanywhereParser = DefaultMarkanywhereParser()
) {

    val scope = CoroutineScope(SupervisorJob() + dispatcher)

    val markdownText: StateFlow<String>
        field = MutableStateFlow(INITIAL_MARKDOWN)

    val blocks: StateFlow<List<MarkdownBlock>>
        field = MutableStateFlow(splitIntoBlocks(INITIAL_MARKDOWN))

    val focusedBlockIndex: StateFlow<Int?>
        field = MutableStateFlow(null)

    val parsedMarkdown: Flow<Flow<SemanticEvent>> =
        markdownText.map { markdown -> flowOf(markdown).parse(parser) }

    val parsedBlocks: Flow<List<Flow<SemanticEvent>>> =
        blocks.map { blockList ->
            blockList.map { block -> flowOf(block.rawText).parse(parser) }
        }

    fun onMarkdownChanged(text: String) {
        markdownText.value = text
        blocks.value = splitIntoBlocks(text)
    }

    fun onBlockFocused(index: Int) {
        focusedBlockIndex.value = index
    }

    fun onBlockBlurred(index: Int, text: String) {
        val currentBlocks = blocks.value.toMutableList()
        if (index < currentBlocks.size) {
            currentBlocks[index] = currentBlocks[index].copy(rawText = text)
            val fullText = joinBlocks(currentBlocks)
            val newBlocks = splitIntoBlocks(fullText)
            blocks.value = newBlocks
            markdownText.value = fullText
        }
        focusedBlockIndex.value = null
    }

    companion object {
        internal val INITIAL_MARKDOWN = """
# Welcome to Markdown Editor

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
    }

}

/**
 * Splits a markdown string into a list of [MarkdownBlock]s by blank lines.
 * Code fences (``` ... ```) are kept as a single atomic block.
 */
internal fun splitIntoBlocks(markdown: String): List<MarkdownBlock> {
    if (markdown.isBlank()) return listOf(MarkdownBlock(0, ""))

    val result = mutableListOf<MarkdownBlock>()
    val currentBlock = StringBuilder()
    var inCodeFence = false

    for (line in markdown.lines()) {
        if (line.trimStart().startsWith("```")) {
            inCodeFence = !inCodeFence
        }
        if (!inCodeFence && line.isBlank()) {
            val blockText = currentBlock.toString().trimEnd('\n')
            if (blockText.isNotEmpty()) {
                result.add(MarkdownBlock(result.size, blockText))
            }
            currentBlock.clear()
        } else {
            currentBlock.append(line).append('\n')
        }
    }

    val remaining = currentBlock.toString().trimEnd('\n')
    if (remaining.isNotEmpty()) {
        result.add(MarkdownBlock(result.size, remaining))
    }

    return if (result.isEmpty()) listOf(MarkdownBlock(0, "")) else result
}

/**
 * Joins a list of [MarkdownBlock]s back into a full markdown string,
 * separating blocks with blank lines.
 */
internal fun joinBlocks(blocks: List<MarkdownBlock>): String =
    blocks.joinToString("\n\n") { it.rawText }
