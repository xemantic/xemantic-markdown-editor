/*
 * Copyright 2026 Kazimierz Pogoda / Xemantic
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

import org.intellij.lang.annotations.Language

@Language("markdown")
const val MARKDOWN_EXAMPLE = """
---
title: "Parser Stress Test"
author: "Anonymous"
date: 2026-05-08
tags: [markdown, testing, edge-cases]
draft: false
---

# Markdown Parser Stress Test

A deliberately gnarly document that exercises CommonMark, GFM, and several
common extensions. If your parser survives this without screaming, it's
probably in decent shape.

## Heading 2 with `inline code` and *emphasis*
### Heading 3 — em-dash, en–dash, ellipsis…
#### Heading 4 with [a link](https://example.com "Title in quotes")
##### Heading 5 with **bold _and italic_** mixed
###### Heading 6 — the deepest ATX

####### Not a heading (7 hashes)

# Heading with trailing hashes ##

---

## Paragraphs, breaks, and whitespace

A normal paragraph. Soft break here
continues on the next line. Hard break here  
(two trailing spaces) ends the line. Backslash hard break\
also ends the line.

A paragraph with    multiple    interior    spaces    that should collapse
in HTML output but not in code.

&nbsp;&nbsp;&nbsp;Three non-breaking spaces, then text.

___

## Emphasis and strong

*italic with asterisks* and _italic with underscores_.
**bold with asterisks** and __bold with underscores__.
***bold italic*** and ___bold italic underscored___.
**bold with _nested italic_ inside**.
~~strikethrough~~ (GFM) and ~~**bold strike**~~.
==highlighted text== (extension; should degrade gracefully).
H~2~O and E=mc^2^ (sub/sup, extension).

Tricky: a*b*c should italicize, but a*b *c should not in strict CMark.
foo_bar_baz should NOT italicize (intra-word underscore).
foo*bar*baz SHOULD italicize (intra-word asterisk).

Escaped: \*not italic\*, \`not code\`, \\backslash, \# not a heading.

---

## Lists

### Unordered, tight

- one
- two
- three

### Unordered, loose

- one

- two

- three

### Ordered with custom start

5. five
6. six
7. seven

### Mixed and deeply nested

1. First
   - nested unordered
     1. nested ordered
        - deeper
          - deeper still
            > with a blockquote inside
            >
            > ```python
            > def deep():
            >     return "still readable?"
            > ```
2. Second with **bold**, `code`, and a [link](#links).
3. Third
   1. with
   2. multiple
   3. children

### Task list (GFM)

- [ ] open task
- [x] completed task
- [X] also completed
- [ ] task with **formatting** and `code`
  - [ ] nested subtask
  - [x] nested done

### List item containing multiple paragraphs

1. First paragraph of item one.

   Second paragraph, must be indented.

   ```
   even a code block
   ```

   > and a blockquote

2. Second item.

### Tricky list continuations

- item with a
  hanging indent that should join
- item followed by

  a paragraph that belongs to it
- item followed by a blank line, then text

not part of the list.

---

## Blockquotes

> Single-line quote.

> Multi-line
> quote with
> several lines.

> Lazy continuation
without the leading marker.

> Nested:
>
> > second level
> >
> > > third level
> > >
> > > > fourth level — getting silly

> Blockquote with **emphasis**, `code`, and a list:
>
> 1. one
> 2. two
>     - nested
>
> And a code fence:
>
> ```js
> const greet = (name) => `hello, ${'$'}{name}`;
> ```

---

## Code

Inline `code`, inline ``code with ` backtick``, and inline ``` `` double-backtick `` ```.

Indented code block:

    function indented() {
      return "four-space indent";
    }

Fenced, no language:

```
plain fenced block
  preserves   spacing
```

Fenced with language hint:

```python
from __future__ import annotations
import asyncio
from dataclasses import dataclass

@dataclass(frozen=True)
class Token:
    kind: str
    value: str | None = None

async def tokens() -> list[Token]:
    return [Token("EOF")]

if __name__ == "__main__":
    print(asyncio.run(tokens()))
```

```rust
fn main() {
    let xs: Vec<u32> = (0..10).filter(|n| n % 2 == 0).collect();
    println!("{:?}", xs);
}
```

```kotlin
fun <T> List<T>.second(): T? = if (size >= 2) this[1] else null
```

Tilde fences:

~~~yaml
key: value
list:
  - one
  - two
nested:
  a: 1
  b: [1, 2, 3]
~~~

Nested fences (info string includes a backtick count):

````markdown
You can put ``` inside a four-backtick fence.

```js
console.log("hi");
```
````

Fence with weird info string:

```text {.line-numbers startFrom=10 highlight="2,5-7"}
line 1
line 2
line 3
line 4
line 5
line 6
line 7
```

---

## Links and references

Inline: [Example](https://example.com).
With title: [Example](https://example.com "the title").
Autolink: <https://example.com/path?q=1&r=2>.
Email autolink: <hello@example.com>.
Bare URL (GFM): https://example.com/bare?x=1#frag.
Relative: [README](./README.md).
Anchor: [back to top](#markdown-parser-stress-test).
Empty text: [](https://example.com).

Reference style: [ref one][1], [ref two][named], and [collapsed][].

URL with parens and special chars:
[wiki](https://en.wikipedia.org/wiki/Markdown_(markup_language) "Markdown (lang)")
and [encoded](https://example.com/path%20with%20spaces?a=%26&b=%2B).

[1]: https://example.com/one "one"
[named]: https://example.com/two
  "two with continuation title"
[collapsed]: https://example.com/three

Footnote reference[^short] and another[^long-name-with-dashes].

[^short]: A short footnote.
[^long-name-with-dashes]: A longer footnote with **formatting**, `code`,
    and a second paragraph.

    Even a code block:

    ```
    multi-line footnote body
    ```

---

## Images

Inline: ![alt text](https://cataas.com/cat "title").
Reference: ![alt][img-ref].
Empty alt: ![](https://cataas.com/cat).
Image inside a link: [![alt](https://cataas.com/cat)](https://example.com).

[img-ref]: https://example.com/ref.png "ref title"

---

## Tables (GFM)

| Left | Center | Right | Default |
| :--- | :----: | ----: | ------- |
| a    | b      | c     | d       |
| `x`  | **y**  | *z*   | [w](#)  |
| 1    | 2      | 3     | 4       |

Table with pipes inside cells:

| Expression       | Result  |
| ---------------- | ------- |
| `a \| b`         | bitwise |
| `a && b`         | logical |
| `"foo \| bar"`   | string  |

Misaligned table that should still parse:

| col 1 | col 2 | col 3 |
|--|--|--|
|short|medium length|a much longer cell that throws off alignment|
|x|y|z|

Empty cells:

| A | B | C |
|---|---|---|
| 1 |   | 3 |
|   | 2 |   |

---

## Definition list (extension)

Term 1
:   Definition for term 1.

Term 2
:   First definition for term 2.
:   Second definition, with a paragraph.

    And a continuation paragraph.

Compound term *with formatting*
:   Definition with `code` and a [link](#).

---

## Math (extension)

Inline: ${'$'}E = mc^2$ and $\int_0^\infty e^{-x^2}\,dx = \tfrac{\sqrt{\pi}}{2}$.

Block:

$$
\frac{\partial}{\partial t}\Psi(\mathbf{r}, t)
= -\frac{i}{\hbar}\hat{H}\Psi(\mathbf{r}, t)
$$

$$
\begin{aligned}
\nabla \cdot \mathbf{E} &= \frac{\rho}{\varepsilon_0} \\
\nabla \cdot \mathbf{B} &= 0 \\
\nabla \times \mathbf{E} &= -\frac{\partial \mathbf{B}}{\partial t} \\
\nabla \times \mathbf{B} &= \mu_0\mathbf{J} + \mu_0\varepsilon_0\frac{\partial \mathbf{E}}{\partial t}
\end{aligned}
$$

Tricky: a $5 dollar bill — the lone `$` should not start math.

---

## Inline HTML

A paragraph with <em>inline HTML emphasis</em> and a <span style="color: red">styled span</span>.

<details>
<summary>Click to expand (HTML inside Markdown)</summary>

Inside the details block, **markdown should still work** if your parser
allows it. Some parsers require a blank line and `markdown="1"`.

- one
- two
- three

</details>

<kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>P</kbd> opens the command palette.

Self-closing: line<br/>break and a horizontal <hr/> rule.

A raw HTML comment that should be stripped or preserved:

<!-- TODO: revisit this section -->

---

## Escapes and edge cases

\*literal asterisks\*, \_literal underscores\_, \`literal backticks\`,
\[literal brackets\], \(literal parens\), \#literal hash, \+literal plus,
\-literal minus, \.literal dot, \!literal bang, \|literal pipe.

Numeric and named entities: &copy; &amp; &lt; &gt; &#9731; &#x2603;

Emoji shortcodes (extension): :smile: :rocket: :+1: :heart:
Literal emoji: 🙂 🚀 👍 ❤️

Mixed-script: Привет, 你好, مرحبا, שלום, こんにちは, 🇩🇪 🇵🇱 🇹🇼.

Zero-width joiners: 👨‍👩‍👧‍👦 (family).

A line ending in a backslash means hard break:\
new line.

A paragraph that starts with a number that looks like an ordered list:
1984 was a year, not a list item. (Unless followed by `. `.)

---

## Horizontal rules

---
***
___
- - -
* * *
_ _ _

---

## A long, mixed-content section

> Quoted intro paragraph that mentions [a link](https://example.com),
> some `inline code`, and **bold text**.

1. **First item** — with a paragraph.

   ```js
   // and a code block
   const x = [1, 2, 3].map(n => n ** 2);
   ```

   > and a quote inside the list item

   - sub-bullet a
   - sub-bullet b
     - sub-sub-bullet
       1. ordered inside unordered
       2. with a [footnote ref][^short]

2. **Second item** — with a table:

   | step | action       | done |
   |-----:|:-------------|:----:|
   |    1 | initialize   |  ✅  |
   |    2 | run          |  ⏳  |
   |    3 | clean up     |  ❌  |

3. **Third item** — with math: $\sum_{i=1}^{n} i = \frac{n(n+1)}{2}$.

---

## Pathological cases

A paragraph immediately followed by a heading
## without a blank line — some parsers handle this, strict CMark requires the blank.

> blockquote
# heading touching a blockquote

```
unclosed fence... what happens at EOF?
"""
