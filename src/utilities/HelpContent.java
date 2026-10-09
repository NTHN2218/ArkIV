package utilities;

import java.util.List;

/**
 * Pure data for the Help dialog. Edit THIS file when a feature changes; no layout code lives here.
 *
 * A Table = column headers + a Kind per column + width weights + grouped rows.
 *  - TEXT:     shows the row's next cell as plain text.
 *  - CODE:     shows the row's next cell in a monospace box (raw syntax).
 *  - RENDERED: shows the previous CODE cell run through MarkdownRenderer. Takes NO cell of its own,
 *              so a Markdown row is just row("Bold", "**text**").
 */
public final class HelpContent {

    private HelpContent() {}

    public enum Kind { TEXT, CODE, RENDERED }

    public record Row(String... cells) {}

    public record Group(String title, List<Row> rows) {
        public Group(String title, Row... rows) { this(title, List.of(rows)); }
    }

    public record Table(String[] headers, Kind[] kinds, double[] weights, List<Group> groups) {}

    private static Row row(String... cells) { return new Row(cells); }

    // ── Markdown ────────────────────────────────────────────────────────────
    public static final Table MARKDOWN = new Table(
            new String[]{"Feature", "Syntax", "Result"},
            new Kind[]{Kind.TEXT, Kind.CODE, Kind.RENDERED},
            new double[]{0.20, 0.38, 0.42},
            List.of(
                    new Group("Text style",
                            row("Bold", "**text**"),
                            row("Italic", "*text*"),
                            row("Inline code", "`text`")),

                    new Group("Headings",
                            row("Heading 1", "# Heading"),
                            row("Heading 2", "## Heading"),
                            row("Heading 3", "### Heading")
                    ),

                    new Group("Lists",
                            row("Bullet-list", "- point 1\n- point 2"),
                            row("Numbered-list", "1. item 1\n2. item 2"),
                            row("Check-list", "- [ ] task 1\n- [x] task 2")
                    ),

                    new Group("Color tags",
                            row("Violet", "|v|text|v|"),
                            row("Indigo", "|i|text|i|"),
                            row("Blue",   "|b|text|b|"),
                            row("Green",  "|g|text|g|"),
                            row("Yellow", "|y|text|y|"),
                            row("Orange", "|o|text|o|"),
                            row("Red",    "|r|text|r|"),

                            row("", ""),
                            row("Right-align", "left >>right>>")
                    )
            ));
}