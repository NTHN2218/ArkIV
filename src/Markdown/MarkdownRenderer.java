package Markdown;

import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;

import java.util.List;

import javax.swing.text.BadLocationException;
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
            .extensions(List.of(TaskListItemsExtension.create()))
            .includeSourceSpans(IncludeSourceSpans.BLOCKS)
            .build();

    public static void render(StyledDocument doc, String rawText) {
        MarkdownDebug.log("[MarkdownRenderer] render() called. rawText length = "
                + (rawText != null ? rawText.length() : 0));

        if (rawText == null) rawText = "";

        clearDocument(doc);

        Node astRoot = PARSER.parse(rawText);
        MarkdownDebug.log("[MarkdownRenderer] Parsed AST root: " + astRoot.getClass().getSimpleName());
        long startNanos = System.nanoTime();

        MarkdownVisitor visitor = new MarkdownVisitor(doc);
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
                System.out.println("[MarkdownRenderer] Appended " + blankLines + " trailing blank line(s) from raw text.");
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