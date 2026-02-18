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
import com.xemantic.markanywhere.parse.MarkanywhereParser
import dev.mokkery.mock
import kotlin.coroutines.ExperimentalCoroutinesApi
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
@OptIn(ExperimentalCoroutinesApi::class)
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

}
