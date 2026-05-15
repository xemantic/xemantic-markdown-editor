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
import com.xemantic.markanywhere.js.toSemanticEvents
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Flow operator that replaces `math` [SemanticEvent]s with the MathML produced by Temml.
 *
 * The markdown parser emits inline math (`$x$`) and display math (`$$x$$`) as:
 *
 * ```
 * Mark(name = "math", attributes = mapOf("display" to "block")?)  // "display" only for $$ ... $$
 * Text(text = "<latex source>")
 * Unmark(name = "math")
 * ```
 *
 * The operator buffers the LaTeX between a math mark/unmark pair, hands it to
 * [Temml.render] which builds a real MathML subtree in a detached host, then streams
 * that subtree as `Mark` / `Text` / `Unmark` events via
 * [com.xemantic.markanywhere.js.toSemanticEvents]. The downstream
 * `appendSemanticEvents` resolves the `math` name to the MathML namespace and
 * inherits it for descendants, so no custom renderer is required.
 */
fun Flow<SemanticEvent>.renderMath(): Flow<SemanticEvent> = flow {
    var inMath = false
    var displayMode = false
    val latex = StringBuilder()
    collect { event ->
        when {
            event is Mark && event.name == "math" -> {
                inMath = true
                displayMode = event.attributes?.get("display") == "block"
                latex.clear()
            }
            event is Unmark && event.name == "math" -> {
                inMath = false
                emitAll(renderMathToEvents(latex.toString(), displayMode))
            }
            inMath && event is Text -> {
                latex.append(event.text)
            }
            else -> emit(event)
        }
    }
}

private fun renderMathToEvents(
    latex: String,
    displayMode: Boolean
): Flow<SemanticEvent> {
    val host = document.createElement("span")
    return try {
        Temml.render(latex, host, temmlOptions(displayMode))
        val math = host.firstElementChild ?: return mathErrorEvents(latex)
        math.toSemanticEvents()
    } catch (_: Throwable) {
        mathErrorEvents(latex)
    }
}

private fun mathErrorEvents(
    latex: String
): Flow<SemanticEvent> = flow {
    emit(SemanticEvent.Mark("code", attributes = mapOf("class" to "math-error")))
    emit(SemanticEvent.Text(latex))
    emit(SemanticEvent.Unmark("code"))
}