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

/**
 * ViewModel for the markdown editor application.
 *
 * Exposes reactive state through [StateFlow]s following the MVVM pattern.
 * The View observes these flows and updates the DOM accordingly.
 */
class MarkdownViewModel {

    /**
     * A greeting message to display in the UI.
     */
    val greeting: String = "Hello World"

}
