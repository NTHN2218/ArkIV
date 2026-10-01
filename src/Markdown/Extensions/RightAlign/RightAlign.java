package Markdown.Extensions.RightAlign;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;

/**
 * AST node for a right-align span: ::text::
 *
 * Only produced by RightAlignDelimiterProcessor when the closing "::" is the last thing on its
 * line (followed by end-of-paragraph, SoftLineBreak or HardLineBreak). Children are ordinary
 * already-parsed inline nodes, so nesting with bold/italic/color tags works for free.
 */
public class RightAlign extends CustomNode implements Delimited {

    public static final String DELIMITER = "::";

    @Override
    public String getOpeningDelimiter() {
        return DELIMITER;
    }

    @Override
    public String getClosingDelimiter() {
        return DELIMITER;
    }
}
