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

import com.xemantic.kotlin.js.dom.html.a
import com.xemantic.kotlin.js.dom.node
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

/**
 * Triggers a download of the given blob with the specified filename.
 */
private fun triggerDownload(blob: Blob, filename: String) {
    val url = URL.createObjectURL(blob)
    try {
        node { a {
            it.href = url
            it.download = filename
            it.click()
        }}
    } finally {
        URL.revokeObjectURL(url)
    }
}

/**
 * Exports the markdown content as a .md file.
 */
fun exportMd(markdown: String, filename: String = "document.md") {
    val blob = Blob(
        arrayOf(markdown),
        BlobPropertyBag(type = "text/markdown")
    )
    triggerDownload(blob, filename)
}

/**
 * Exports the rendered HTML as an .html file.
 */
fun exportHtml(html: String, filename: String = "document.html") {
    val fullHtml = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Document</title>
        </head>
        <body>
        $html
        </body>
        </html>
    """.trimIndent()
    val blob = Blob(
        arrayOf(fullHtml),
        BlobPropertyBag(type = "text/html")
    )
    triggerDownload(blob, filename)
}
