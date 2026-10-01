package Markdown.Extensions.RightAlign;

import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Node;
import org.commonmark.node.Nodes;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.SourceSpans;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

/**
 * Handles ::text:: -- right-aligned span that must end its line.
 *
 * Delimiter char is ':' with a minimum run length of 2. CommonMark's delimiter-stack does the
 * open/close pairing and flanking checks (same mechanism as * / ** / ~~ / ^).
 *
 * Reject rules (return 0 -> both runs stay as literal "::" text, same graceful degrade as an
 * unmatched "**"):
 *  - either run is not exactly 2 long (":::" etc. is not a supported variant)
 *  - the closer is followed by anything other than end-of-paragraph, SoftLineBreak or
 *    HardLineBreak. This also naturally rejects the first of two spans on one line, and spans
 *    wrapped inside another delimiter (e.g. **::x::**) since the outer delimiter is still a
 *    Text sibling at this point.
 */
public class RightAlignDelimiterProcessor implements DelimiterProcessor {

    @Override
    public char getOpeningCharacter() {
        return ':';
    }

    @Override
    public char getClosingCharacter() {
        return ':';
    }

    @Override
    public int getMinLength() {
        return 2;
    }

    @Override
    public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
        if (openingRun.length() != 2 || closingRun.length() != 2) {
            return 0;
        }

        Text opener = openingRun.getOpener();
        Text closer = closingRun.getCloser();

        // Closer must end the line. A delimiter run is stored as ONE Text node PER CHARACTER,
        // so closer.getNext() is the run's own second ':' -- step past the whole run first.
        Node lastCloserChar = closer;
        for (Text t : closingRun.getClosers(2)) {
            lastCloserChar = t;
        }
        Node after = lastCloserChar.getNext();
        // Trailing spaces before a line break get trimmed out of their Text node but the (now
        // empty) node stays in the tree -- skip blank Text so "::x::  \n" still counts as EOL.
        while (after instanceof Text t && t.getLiteral().isBlank()) {
            after = after.getNext();
        }
        if (after != null && !(after instanceof SoftLineBreak) && !(after instanceof HardLineBreak)) {
            return 0;
        }

        RightAlign span = new RightAlign();

        SourceSpans sourceSpans = new SourceSpans();
        sourceSpans.addAllFrom(openingRun.getOpeners(2));

        for (Node node : Nodes.between(opener, closer)) {
            span.appendChild(node);
            sourceSpans.addAll(node.getSourceSpans());
        }

        sourceSpans.addAllFrom(closingRun.getClosers(2));
        span.setSourceSpans(sourceSpans.getSourceSpans());

        opener.insertAfter(span);

        return 2;
    }
}