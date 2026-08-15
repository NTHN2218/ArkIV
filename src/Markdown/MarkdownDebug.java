package Markdown;

public class MarkdownDebug {

    // Master switch -- when false, NO markdown-related logs print at all,
    // regardless of VERBOSE. Flip on only when actively working on/debugging markdown.
    public static boolean ACTIVE = true;

    // When ACTIVE is true, this controls density: false = summary lines only,
    // true = full per-insert [MD] trace layered on top.
    public static boolean VERBOSE = false;

    private MarkdownDebug() {}

    // Gated detail-level logs (the [MD] insert trace, parser step lines, etc.)
    public static void log(String msg) {
        if (ACTIVE && VERBOSE) System.out.println(msg);
    }

    // Always-visible-when-ACTIVE summary logs (one line per render, etc.)
    public static void summary(String msg) {
        if (ACTIVE) System.out.println(msg);
    }
}