package Markdown;

import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;

import Markdown.Extensions.ColorTag.ColorTagExtension;
import Markdown.Extensions.ColorTag.ColorTagPreprocessor;
import Markdown.Extensions.RightAlign.RightAlignExtension;

import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

/**
 * Public entry point for markdown rendering.
 * Callers (TaskItem, etc.) only ever need render() -- everything else in this
 * package is internal machinery they should never touch directly.
 *
 * Safe to call repeatedly on the same StyledDocument (e.g. every time an entry
 * is edited and re-saved) -- existing content is cleared first each time.
 */
public class MarkdownRenderer {

    // One shared Parser instance -- commonmark's Parser is stateless per-parse-call
    // and safe to reuse across many render() invocations.
    private static final Parser PARSER = Parser.builder()
            .extensions(List.of(
                    TaskListItemsExtension.create(),
                    ColorTagExtension.create(),
                    RightAlignExtension.create()))
            .includeSourceSpans(IncludeSourceSpans.BLOCKS)
            .build();

    /** No known width: right-align spans (::text::) render inline, with no tab/alignment. */
    public static void render(StyledDocument doc, String rawText) {
        render(doc, rawText, -1);
    }

    /**
     * @param contentWidthPx usable pixel width of the text view (pane width minus its insets),
     *                       used as the right-align tab-stop position. <= 0 means "unknown".
     */
    public static void render(StyledDocument doc, String rawText, int contentWidthPx) {
        MarkdownDebug.log("[MarkdownRenderer] render() called. rawText length = "
                + (rawText != null ? rawText.length() : 0) + ", contentWidthPx = " + contentWidthPx);

        if (rawText == null) rawText = "";

        clearDocument(doc);
        // doc.remove() leaves one surviving empty paragraph that keeps its old attributes
        // (including a stale right-align TabSet from a previous width) -- reset it.
        doc.setParagraphAttributes(0, 1, SimpleAttributeSet.EMPTY, true);

        // Rewrite user-facing |v|...|v| color-tag syntax into the internal ^1...^1 syntax
        // right before parsing -- a local variable only. The caller's original rawText (what
        // gets persisted and what reopens in the edit dialog) is never touched.
        String parseText = ColorTagPreprocessor.preprocess(rawText);

        Node astRoot = PARSER.parse(parseText);
        MarkdownDebug.log("[MarkdownRenderer] Parsed AST root: " + astRoot.getClass().getSimpleName());
        long startNanos = System.nanoTime();

        MarkdownVisitor visitor = new MarkdownVisitor(doc, contentWidthPx);
        astRoot.accept(visitor);

        appendTrailingBlankLines(doc, rawText);

        long elapsedMicros = (System.nanoTime() - startNanos) / 1000;
        MarkdownDebug.summary("[Markdown] rendered rawLen=" + rawText.length()
                + " -> docLen=" + doc.getLength() + " (" + elapsedMicros + "us)");
    }

    // commonmark's parser discards trailing blank lines entirely -- they never
    // appear as nodes in the AST, so the Visitor has no way to know about them.
    // We recover them here by inspecting the raw string directly.
    private static void appendTrailingBlankLines(StyledDocument doc, String rawText) {
        int trailingNewlines = 0;
        int i = rawText.length() - 1;
        while (i >= 0 && rawText.charAt(i) == '\n') {
            trailingNewlines++;
            i--;
        }
        int blankLines = Math.max(0, trailingNewlines - 1);

        if (blankLines > 0) {
            try {
                for (int b = 0; b < blankLines; b++) {
                    doc.insertString(doc.getLength(), "\n", MarkdownStyles.getPlainAttributes());
                }
                MarkdownDebug.log("[MarkdownRenderer] Appended " + blankLines + " trailing blank line(s) from raw text.");
            } catch (BadLocationException e) {
                System.err.println("[MarkdownRenderer] Failed to append trailing blank lines: " + e.getMessage());
            }
        }
    }

    private static void clearDocument(StyledDocument doc) {
        int length = doc.getLength();
        if (length > 0) {
            try {
                doc.remove(0, length);
                MarkdownDebug.log("[MarkdownRenderer] Cleared " + length + " existing char(s) from document.");
            } catch (BadLocationException e) {
                System.err.println("[MarkdownRenderer] Failed to clear document: " + e.getMessage());
            }
        }
    }
}