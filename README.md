<h1 align="center">ArkIV</h1>

<p align="center">
  A keyboard-first, Markdown-native desktop organizer built from scratch in Java Swing.
</p>

<p align="center">
  <img src="docs/screenshot.png" alt="ArkIV" width="900">
</p>

## What is it?

ArkIV is a personal task and note organizer that lives entirely on your machine. Type into the box at the bottom, and your thoughts become cards you can nest, reorder, fold away, and search. Everything is stored as plain JSON, with no accounts, no cloud, and no sync.

## Highlights

- **Registers**: separate workspaces, each backed by its own JSON file, switchable with `Ctrl+Tab`.
- **Nested entries**: sub-entries, collapsible parents, and keyboard reordering.
- **Live Markdown**: headings, bold, italic, lists and task lists render right in the card, with the syntax markers faded out instead of hidden.
- **Editor niceties**: hotstrings (`-[` becomes a checkbox), Obsidian-style selection wrapping, and JetBrains-style caret navigation.
- **Custom dark UI**: rounded dialogs, themed scrollbars and checkboxes, all hand-painted in Swing with JetBrains Mono.

## Built with

Java 17+, Swing, [Gson](https://github.com/google/gson), [commonmark-java](https://github.com/commonmark/commonmark-java)

## Running it

Add the libraries above, drop the JetBrains Mono fonts into `assets/fonts/`, and run `ArkIV.main()`. ArkIV creates its data folders on first launch.