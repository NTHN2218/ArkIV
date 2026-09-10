### Right-Align (`]>text]>`) via CommonMark Extension

**Status:** Deferred / parked. Not scheduled for any specific version. Revisit as its own dedicated planning + implementation chat when picked back up.

**Package/location (proposed):** `Markdown` package, alongside `MarkdownRenderer`/`MarkdownDebug` — e.g. a new `RightAlignExtension` (parser extension) + `RightAlign` (custom AST node).

#### Architecture — three pieces, all inside CommonMark's own extension model

1. **Custom `DelimiterProcessor`**, registered via `Parser.ParserExtension` (same extension point already used for `commonmark-ext-task-list-items`).
    - Delimiter character: `>` (not `>>` as a literal string — CommonMark delimiter processors work on runs of one repeated character; `>>` is expressed as `getMinLength() == 2` on the `>` character).
    - Standard delimiter-run matching (open/close pairing, left-/right-flanking rules) is handled by CommonMark's existing machinery — same mechanism that pairs `*`/`**` for emphasis/strong.
2. **Positional accept/reject check inside `process()`:** before accepting a matched opener/closer pair, walk forward from the closer node. If the next sibling is **not** `null` and **not** a `SoftLineBreak`/`HardLineBreak`, reject the match (`return 0`). CommonMark's own convention then falls back to literal `>` characters — same fallback behavior mismatched `**bold` already gets today. No separate pre-scan step, no separate fallback logic to write — reuse CommonMark's built-in rejection path.
3. **Custom `Node` subtype** (`RightAlign extends CustomNode`) — on acceptance, wraps the already-parsed inline children between opener and closer (same technique CommonMark's core `EmphasisDelimiterProcessor` uses for `Emphasis`/`StrongEmphasis`). Because the children are already-parsed inline nodes, nested markdown (`>>**bold**>>`) comes through correctly with zero extra work — this is the main win of doing it this way instead of pre-scanning raw strings.
4. **Renderer side:** one new `visit(RightAlign node)` case added to `MarkdownRenderer`'s AST visitor. Applies right-aligned `TabStop`/`TabSet` paragraph attributes (tab stop at the field's right margin, tab character inserted before the wrapped content), then renders the children normally through the existing visitor recursion.

#### Integration point to remember: width-cache coupling

Right-align rendering is **not** independent of `MarkdownRenderer` in isolation — the `TabStop` position depends on the field's current pixel width (`mainEntryTextWidth` / `subEntryTextWidth`), which is dynamically computed and can change on window resize via the existing `computeTextPaneWidths()` / `applyFixedTextWidth()` reflow system. Any implementation needs the right-align tab-stop attributes recomputed whenever that reflow runs, not just at initial render — otherwise a resize would leave the right-aligned text pinned to a stale margin.

#### Known edge cases / things to verify with real test input, not just assumed correct

- **Blockquote collision** (above) — needs an explicit decision: accept as documented limitation, or require a non-`>` character before the opener.
- **Delimiter-run scan order:** commonMark resolves delimiter matches across the whole paragraph in a particular order; if multiple `>` runs or other unresolved delimiters (e.g. a later `*`) exist on the same line, verify the sibling-check still correctly detects "more content follows" regardless of what order other delimiters get resolved in.
- **Multiple `>>...>>` spans on one line** — matching behavior (which pairs with which) is undefined in the current spec; needs a decision later (likely: reject/treat as literal, keep it simple).
- **Escaping:** `\>` is valid CommonMark backslash-escape syntax and will already resolve to literal `>` before delimiter scanning — this is actually a free escape hatch for users who want literal `>>text>>`, worth keeping as documented behavior rather than fighting it.
- **Hard vs. soft line breaks:** both should count as "end of line" for the closer check — treat identically.

#### Trade-off, stated plainly

This is meaningfully more implementation work than the earlier pre-scan design (a real `DelimiterProcessor` + custom node + sibling-inspection logic, roughly comparable in complexity to the task-list-items extension, plus the blockquote edge case to resolve). What it buys you: CommonMark's `Parser.parse()` remains the single, complete authority over the raw text — no separate text-scanning layer before or after it. That was the explicit requirement driving this choice, and it's satisfied.