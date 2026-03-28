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

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import com.xemantic.markanywhere.SemanticEvent
import com.xemantic.markanywhere.parse.MarkanywhereParser
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Tests for the [MarkdownViewModel], demonstrating the MVVM pattern.
 *
 * Tests live in `jsTest` because [MarkdownViewModel] currently uses JS-only dependencies
 * ([MarkanywhereParser]). The ViewModel is tested without instantiating the DOM view,
 * verifying business logic in isolation.
 *
 * The [MarkanywhereParser] dependency is mocked with
 * [Mokkery](https://mokkery.dev/), and coroutines are driven by [UnconfinedTestDispatcher]
 * so that `launch` blocks execute eagerly within each test, without needing a real event loop.
 */
class MarkdownViewModelTest {

    @Test
    fun `should have initial markdown text with welcome heading`() = runTest {
        // given
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val parser = mock<MarkanywhereParser>()

        // when
        val viewModel = MarkdownViewModel(dispatcher, parser)

        // then
        have(viewModel.markdownText.value.contains("# Welcome to Markdown Editor"))
    }

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

    @Test
    fun `should replace markdown text on subsequent changes`() = runTest {
        // given
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val parser = mock<MarkanywhereParser>()
        val viewModel = MarkdownViewModel(dispatcher, parser)

        // when
        viewModel.onMarkdownChanged("# First")
        viewModel.onMarkdownChanged("# Second")

        // then
        assert(viewModel.markdownText.value == "# Second")
    }

    @Test
    fun `should allow empty markdown text`() = runTest {
        // given
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val parser = mock<MarkanywhereParser>()
        val viewModel = MarkdownViewModel(dispatcher, parser)

        // when
        viewModel.onMarkdownChanged("")

        // then
        assert(viewModel.markdownText.value == "")
    }

    @Test
    fun `should expose parsed markdown as flow of semantic events`() = runTest {
        // given
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val parser = mock<MarkanywhereParser>()
        val events = listOf<SemanticEvent>(
            SemanticEvent.Mark("h1"),
            SemanticEvent.Text("Hello"),
            SemanticEvent.Unmark("h1")
        )
        every { parser.parse(any()) } returns flowOf(*events.toTypedArray())
        val viewModel = MarkdownViewModel(dispatcher, parser)

        // when
        viewModel.onMarkdownChanged("# Hello")

        // then
        val eventsFlow = viewModel.parsedMarkdown.first()
        assert(eventsFlow.toList() == events)
    }

    @Test
    fun `should initialize blocks by splitting initial markdown`() = runTest {
        // given
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val parser = mock<MarkanywhereParser>()

        // when
        val viewModel = MarkdownViewModel(dispatcher, parser)

        // then
        have(viewModel.blocks.value.isNotEmpty())
        have(viewModel.blocks.value.first().rawText.contains("# Welcome to Markdown Editor"))
    }

    @Test
    fun `should update blocks when markdown changes`() = runTest {
        // given
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val parser = mock<MarkanywhereParser>()
        val viewModel = MarkdownViewModel(dispatcher, parser)

        // when
        viewModel.onMarkdownChanged("# Hello\n\nWorld")

        // then
        assert(viewModel.blocks.value.size == 2)
        assert(viewModel.blocks.value[0].rawText == "# Hello")
        assert(viewModel.blocks.value[1].rawText == "World")
    }

    @Test
    fun `should have null focused block index initially`() = runTest {
        // given
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val parser = mock<MarkanywhereParser>()

        // when
        val viewModel = MarkdownViewModel(dispatcher, parser)

        // then
        assert(viewModel.focusedBlockIndex.value == null)
    }

    @Test
    fun `should set focused block index on block focus`() = runTest {
        // given
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val parser = mock<MarkanywhereParser>()
        val viewModel = MarkdownViewModel(dispatcher, parser)

        // when
        viewModel.onBlockFocused(2)

        // then
        assert(viewModel.focusedBlockIndex.value == 2)
    }

    @Test
    fun `should clear focused block index on block blur`() = runTest {
        // given
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val parser = mock<MarkanywhereParser>()
        val viewModel = MarkdownViewModel(dispatcher, parser)
        viewModel.onMarkdownChanged("# Hello\n\nWorld")
        viewModel.onBlockFocused(0)

        // when
        viewModel.onBlockBlurred(0, "# Hello")

        // then
        assert(viewModel.focusedBlockIndex.value == null)
    }

    @Test
    fun `should update block text on blur`() = runTest {
        // given
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val parser = mock<MarkanywhereParser>()
        val viewModel = MarkdownViewModel(dispatcher, parser)
        viewModel.onMarkdownChanged("# Hello\n\nWorld")
        viewModel.onBlockFocused(0)

        // when
        viewModel.onBlockBlurred(0, "## Modified Heading")

        // then
        assert(viewModel.blocks.value[0].rawText == "## Modified Heading")
        assert(viewModel.blocks.value[1].rawText == "World")
    }

    @Test
    fun `should update full markdown text on blur`() = runTest {
        // given
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val parser = mock<MarkanywhereParser>()
        val viewModel = MarkdownViewModel(dispatcher, parser)
        viewModel.onMarkdownChanged("# Hello\n\nWorld")
        viewModel.onBlockFocused(0)

        // when
        viewModel.onBlockBlurred(0, "## Changed")

        // then
        assert(viewModel.markdownText.value == "## Changed\n\nWorld")
    }

    @Test
    fun `should split block into multiple blocks when blank line added on blur`() = runTest {
        // given
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val parser = mock<MarkanywhereParser>()
        val viewModel = MarkdownViewModel(dispatcher, parser)
        viewModel.onMarkdownChanged("# Hello")
        viewModel.onBlockFocused(0)

        // when - user typed a blank line creating a new paragraph
        viewModel.onBlockBlurred(0, "# Hello\n\nNew paragraph")

        // then
        assert(viewModel.blocks.value.size == 2)
        assert(viewModel.blocks.value[0].rawText == "# Hello")
        assert(viewModel.blocks.value[1].rawText == "New paragraph")
    }

}

/**
 * Tests for the [splitIntoBlocks] and [joinBlocks] utility functions.
 */
class BlockSplittingTest {

    @Test
    fun `splitIntoBlocks should return single empty block for blank input`() {
        // when
        val blocks = splitIntoBlocks("")

        // then
        assert(blocks.size == 1)
        assert(blocks[0].rawText == "")
    }

    @Test
    fun `splitIntoBlocks should return single block for text without blank lines`() {
        // when
        val blocks = splitIntoBlocks("# Hello")

        // then
        assert(blocks.size == 1)
        assert(blocks[0].rawText == "# Hello")
    }

    @Test
    fun `splitIntoBlocks should split on blank lines`() {
        // when
        val blocks = splitIntoBlocks("# Hello\n\nWorld")

        // then
        assert(blocks.size == 2)
        assert(blocks[0].rawText == "# Hello")
        assert(blocks[1].rawText == "World")
    }

    @Test
    fun `splitIntoBlocks should assign sequential indices`() {
        // when
        val blocks = splitIntoBlocks("A\n\nB\n\nC")

        // then
        assert(blocks[0].index == 0)
        assert(blocks[1].index == 1)
        assert(blocks[2].index == 2)
    }

    @Test
    fun `splitIntoBlocks should keep code fence as single block`() {
        // given
        val markdown = "```kotlin\nfun main() {}\n```"

        // when
        val blocks = splitIntoBlocks(markdown)

        // then
        assert(blocks.size == 1)
        assert(blocks[0].rawText == markdown)
    }

    @Test
    fun `splitIntoBlocks should keep code fence with surrounding blank lines as single block`() {
        // given
        val markdown = "Before\n\n```kotlin\nfun main() {}\n```\n\nAfter"

        // when
        val blocks = splitIntoBlocks(markdown)

        // then
        assert(blocks.size == 3)
        assert(blocks[0].rawText == "Before")
        assert(blocks[1].rawText == "```kotlin\nfun main() {}\n```")
        assert(blocks[2].rawText == "After")
    }

    @Test
    fun `joinBlocks round-trips with splitIntoBlocks`() {
        // given
        val original = "# Hello\n\nWorld\n\nEnd"

        // when
        val blocks = splitIntoBlocks(original)
        val rejoined = joinBlocks(blocks)

        // then
        assert(rejoined == original)
    }

    @Test
    fun `joinBlocks separates blocks with double newline`() {
        // given
        val blocks = listOf(
            MarkdownBlock(0, "First"),
            MarkdownBlock(1, "Second")
        )

        // when
        val result = joinBlocks(blocks)

        // then
        assert(result == "First\n\nSecond")
    }

}
