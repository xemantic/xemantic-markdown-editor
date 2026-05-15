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

import org.w3c.dom.Element

external interface TemmlOptions {
    var displayMode: Boolean?
    var throwOnError: Boolean?
    var errorColor: String?
    var xml: Boolean?
    var strict: Boolean?
}

/**
 * The shape of the [Temml] runtime object.
 *
 * Mirrors the relevant subset of the default export from the `temml` npm package.
 */
external interface TemmlApi {

    /**
     * Renders [expression] (LaTeX) into [baseNode], replacing its contents with the
     * produced MathML element.
     */
    fun render(
        expression: String,
        baseNode: Element,
        options: TemmlOptions = definedExternally
    )

    /**
     * Renders [expression] (LaTeX) to a MathML string.
     *
     * With `displayMode = true` the produced root carries `display="block"`,
     * otherwise the math is rendered inline.
     */
    fun renderToString(
        expression: String,
        options: TemmlOptions = definedExternally
    ): String

}

/**
 * Default export of the `temml` npm package — the [Temml](https://temml.org/)
 * LaTeX-to-MathML converter.
 *
 * The package exposes only a default export carrying `render`, `renderToString`,
 * etc. as members, so this is bound as a `@JsModule` `val` rather than an
 * `external object` (the latter would emit a namespace import and fail at
 * webpack time with "renderToString was not found in temml").
 */
@JsModule("temml")
external val Temml: TemmlApi

internal fun temmlOptions(displayMode: Boolean): TemmlOptions =
    js("({})").unsafeCast<TemmlOptions>().apply {
        this.displayMode = displayMode
        throwOnError = false
    }
