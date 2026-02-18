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

import com.xemantic.kotlin.test.have
import kotlin.test.Test

class MarkdownViewModelTest {

    @Test
    fun initialMarkdownTextContainsWelcomeHeading() {
        val viewModel = MarkdownViewModel()
        have(viewModel.markdownText.value.contains("# Welcome to Markdown Editor"))
    }

    @Test
    fun onMarkdownChangedUpdatesMarkdownText() {
        val viewModel = MarkdownViewModel()
        viewModel.onMarkdownChanged("# Hello")
        have(viewModel.markdownText.value == "# Hello")
    }

}
