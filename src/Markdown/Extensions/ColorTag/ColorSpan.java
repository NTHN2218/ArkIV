package Markdown.Extensions.ColorTag;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;

/**
 * AST node for a VIBGYOR color tag span: ^1 text ^1 ... ^7 text ^7.
 * colorNumber is 1 (violet) through 7 (red), matching MarkdownStyles' palette index.
 *
 * Children are ordinary already-parsed inline nodes (Text, Emphasis, StrongEmphasis, etc.) --
 * nesting with bold/italic works for free because ColorTagDelimiterProcessor only wraps
 * content that has ALREADY been parsed as inline nodes by the time process() runs (this is
 * the DelimiterProcessor mechanism, same one built-in emphasis and Strikethrough use --
 * not the same mechanism as the task-checkbox InlineContentParser, which captures raw text).
 */
public class ColorSpan extends CustomNode implements Delimited {

    private final int colorNumber;
    private final String delimiter; // e.g. "^1" -- rendered dimmed at both open and close by MarkdownVisitor

    public ColorSpan(int colorNumber, String delimiter) {
        this.colorNumber = colorNumber;
        this.delimiter = delimiter;
    }

    public int getColorNumber() {
        return colorNumber;
    }

    @Override
    public String getOpeningDelimiter() {
        return delimiter;
    }

    @Override
    public String getClosingDelimiter() {
        return delimiter;
    }
}