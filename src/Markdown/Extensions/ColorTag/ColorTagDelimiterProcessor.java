package Markdown.Extensions.ColorTag;

import org.commonmark.node.Node;
import org.commonmark.node.Nodes;
import org.commonmark.node.SourceSpans;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

/**
 * Handles ^1 text ^1 ... ^7 text ^7.
 *
 * "^" is the actual delimiter character CommonMark's delimiter-stack algorithm tracks and pairs
 * up (same mechanism as * / _ / Strikethrough's ~). The digit is ordinary adjacent text that
 * this processor inspects itself and strips once a valid same-digit pair is confirmed -- the
 * digit isn't part of what CommonMark considers "the delimiter," so we have to police it here.
 *
 * IMPORTANT: the tag is "^" followed by a digit for BOTH the opener and the closer (^1 ... ^1,
 * not a mirrored ^1 ... 1^). That means the digit for the CLOSER also sits right AFTER its
 * caret (closer.getNext()), not before it -- easy to get backwards, which is exactly what
 * happened in the first pass of this file.
 *
 * Any run that doesn't resolve to a valid pair (missing closer, mismatched digit, no digit at
 * all, wrong run length) is rejected by returning 0, which leaves the "^" as plain literal text
 * -- identical graceful-degrade behavior to an unmatched "*" in standard CommonMark.
 */
public class ColorTagDelimiterProcessor implements DelimiterProcessor {

    @Override
    public char getOpeningCharacter() {
        return '^';
    }

    @Override
    public char getClosingCharacter() {
        return '^';
    }

    @Override
    public int getMinLength() {
        return 1;
    }

    @Override
    public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
        // Only a single "^" is meaningful for this tag -- "^^1 text^^1" is not a supported variant,
        // unlike ** vs * for bold/italic. Reject anything else and let it fall through as text.
        if (openingRun.length() != 1 || closingRun.length() != 1) {
            return 0;
        }

        Text opener = openingRun.getOpener();
        Text closer = closingRun.getCloser();

        // Digit for BOTH opener and closer trails its own caret -- "^1 ... ^1", never "1^".
        Node afterOpener = opener.getNext();
        Node afterCloser = closer.getNext();
        if (!(afterOpener instanceof Text) || !(afterCloser instanceof Text)) {
            return 0; // no adjacent plain text -- no digit possible, not our tag
        }

        Text openDigitNode = (Text) afterOpener;
        Text closeDigitNode = (Text) afterCloser;
        String openLiteral = openDigitNode.getLiteral();
        String closeLiteral = closeDigitNode.getLiteral();
        if (openLiteral.isEmpty() || closeLiteral.isEmpty()) {
            return 0;
        }

        char openDigit = openLiteral.charAt(0);
        char closeDigit = closeLiteral.charAt(0);
        if (!isValidDigit(openDigit) || !isValidDigit(closeDigit) || openDigit != closeDigit) {
            return 0; // no digit, out of 1-7 range, or opener/closer digits don't match
        }

        // Strip the digit out of its host Text node now, before collecting children -- it's
        // part of the 2-char marker ("^" + digit), not real content. Both sides strip the
        // LEADING char, since the digit trails the caret on both ends.
        stripLeadingChar(openDigitNode);
        stripLeadingChar(closeDigitNode);

        int colorNumber = openDigit - '0';
        String delimiterLabel = "^" + colorNumber;
        Node colorSpan = new ColorSpan(colorNumber, delimiterLabel);

        SourceSpans sourceSpans = new SourceSpans();
        sourceSpans.addAllFrom(openingRun.getOpeners(1));

        for (Node node : Nodes.between(opener, closer)) {
            colorSpan.appendChild(node);
            sourceSpans.addAll(node.getSourceSpans());
        }

        sourceSpans.addAllFrom(closingRun.getClosers(1));
        colorSpan.setSourceSpans(sourceSpans.getSourceSpans());

        opener.insertAfter(colorSpan);

        return 1;
    }

    private boolean isValidDigit(char c) {
        return c >= '1' && c <= '7';
    }

    private void stripLeadingChar(Text node) {
        String literal = node.getLiteral();
        if (literal.length() == 1) {
            node.unlink();
        } else {
            node.setLiteral(literal.substring(1));
        }
    }
}