package Markdown;

import Markdown.Extensions.ColorTag.ColorSpan;
import Markdown.Extensions.RightAlign.RightAlign;
import org.commonmark.node.*;
import org.commonmark.ext.task.list.items.TaskListItemMarker;
import org.commonmark.node.SourceSpan;
import org.commonmark.node.OrderedList;

import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import java.util.ArrayDeque;
import java.util.Deque;

public class MarkdownVisitor extends AbstractVisitor {

    // Swing sizes the right-tab gap from integer-rounded widths but lays the row out with float
    // widths, so a stop exactly at the view width overflows by a fraction of a pixel and the
    // last word wraps. Pull the stop in a few px so the row always fits. Raise (more negative)
    // if wrapping still happens, lower toward 0 if the gap to the edge looks too big.
    private static final int RIGHT_EDGE_ADJUST_PX = -4;

    private final StyledDocument doc;
    private final Deque<SimpleAttributeSet> attributeStack = new ArrayDeque<>();
    private final int rightEdgePx; // <= 0 means unknown -> right-align spans render inline
    private int insertOffset;

    public MarkdownVisitor(StyledDocument doc) {
        this(doc, -1);
    }

    public MarkdownVisitor(StyledDocument doc, int contentWidthPx) {
        this.doc = doc;
        this.rightEdgePx = contentWidthPx;
        this.insertOffset = 0;
        attributeStack.push(MarkdownStyles.getPlainAttributes());
    }

    private SimpleAttributeSet currentAttributes() {
        return attributeStack.peek();
    }

    private void insertText(String text) {
        insertText(text, currentAttributes(), "txt");
    }

    private void insertText(String text, AttributeSet attrs) {
        insertText(text, attrs, "mark");
    }

    private void insertText(String text, AttributeSet attrs, String tag) {
        if (text == null || text.isEmpty()) return;
        try {
            doc.insertString(insertOffset, text, attrs);
            MarkdownDebug.log("[MD] " + insertOffset + "+" + text.length() + " " + tag + " " + preview(text));
            insertOffset += text.length();
        } catch (BadLocationException e) {
            System.err.println("[MarkdownVisitor] insertString failed at offset " + insertOffset + ": " + e.getMessage());
        }
    }

    private String preview(String text) {
        String escaped = text.replace("\n", "\\n");
        return escaped.length() > 20 ? "\"" + escaped.substring(0, 20) + "...\"" : "\"" + escaped + "\"";
    }

    @Override
    public void visit(Heading heading) {
        String marker = "#".repeat(heading.getLevel()) + " ";
        insertText(marker, MarkdownStyles.getMutedAttributes());

        attributeStack.push(MarkdownStyles.getHeadingAttributes(heading.getLevel()));
        visitChildren(heading);
        attributeStack.pop();

        insertText("\n");
        if (heading.getNext() != null) insertText("\n");
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        String delimiter = strongEmphasis.getOpeningDelimiter() != null ? strongEmphasis.getOpeningDelimiter() : "**";
        insertText(delimiter, MarkdownStyles.getMutedAttributes());

        SimpleAttributeSet boldAttrs = MarkdownStyles.copyOf(currentAttributes());
        MarkdownStyles.applyBold(boldAttrs);
        attributeStack.push(boldAttrs);
        visitChildren(strongEmphasis);
        attributeStack.pop();

        String closing = strongEmphasis.getClosingDelimiter() != null ? strongEmphasis.getClosingDelimiter() : "**";
        insertText(closing, MarkdownStyles.getMutedAttributes());
    }

    @Override
    public void visit(Emphasis emphasis) {
        String delimiter = emphasis.getOpeningDelimiter() != null ? emphasis.getOpeningDelimiter() : "*";
        insertText(delimiter, MarkdownStyles.getMutedAttributes());

        SimpleAttributeSet italicAttrs = MarkdownStyles.copyOf(currentAttributes());
        MarkdownStyles.applyItalic(italicAttrs);
        attributeStack.push(italicAttrs);
        visitChildren(emphasis);
        attributeStack.pop();

        String closing = emphasis.getClosingDelimiter() != null ? emphasis.getClosingDelimiter() : "*";
        insertText(closing, MarkdownStyles.getMutedAttributes());
    }

    @Override
    public void visit(Paragraph paragraph) {
        visitChildren(paragraph);
        insertText("\n");
        if (paragraph.getNext() != null) insertText("\n");
    }

    @Override
    public void visit(Text text) {
        insertText(text.getLiteral());
    }

    @Override
    public void visit(BulletList bulletList) {
        visitListItems(bulletList);
    }

    @Override
    public void visit(OrderedList orderedList) {
        visitListItems(orderedList);
    }

    private void visitListItems(ListBlock list) {
        Node item = list.getFirstChild();
        while (item != null) {
            item.accept(this);
            Node next = item.getNext();
            if (next != null && hasBlankLineBetween(item, next)) {
                insertText("\n");
            }
            item = next;
        }
        if (list.getNext() != null) {
            insertText("\n");
        }
    }

    // checks the actual raw source line gap between two specific items, rather than
    // commonmark's isTight() which reports looseness for the WHOLE list if a blank
    // line appears anywhere in it -- we want per-gap accuracy, not a list-wide flag.
    private boolean hasBlankLineBetween(Node a, Node b) {
        List<SourceSpan> aSpans = a.getSourceSpans();
        List<SourceSpan> bSpans = b.getSourceSpans();
        if (aSpans.isEmpty() || bSpans.isEmpty()) return false;

        int aEndLine = aSpans.get(aSpans.size() - 1).getLineIndex();
        int bStartLine = bSpans.get(0).getLineIndex();
        boolean hasBlank = (bStartLine - aEndLine) > 1;

        MarkdownDebug.log("[MD] gap check: itemA endLine=" + aEndLine
                + " itemB startLine=" + bStartLine + " -> blankLine=" + hasBlank);
        return hasBlank;
    }

    //Checklist
    @Override
    public void visit(ListItem listItem) {
        boolean taskItem = isTaskItem(listItem);
        boolean checkedTaskItem = taskItem && ((TaskListItemMarker) listItem.getFirstChild()).isChecked();

        if (checkedTaskItem) {
            attributeStack.push(MarkdownStyles.getCheckedTaskTextAttributes());
        }

        if (!taskItem) {
            if (listItem.getParent() instanceof OrderedList orderedList) {
                int number = computeOrderedNumber(orderedList, listItem);
                insertText(number + ". ", MarkdownStyles.getOrderedMarkerAttributes());
            } else {
                insertText("\u2022 ", MarkdownStyles.getBulletAttributes());
            }
        }

        visitChildren(listItem);

        if (checkedTaskItem) {
            attributeStack.pop();
        }
    }

    private int computeOrderedNumber(OrderedList list, ListItem item) {
        int number = list.getMarkerStartNumber() != null ? list.getMarkerStartNumber() : 1;
        Node sibling = list.getFirstChild();
        while (sibling != null && sibling != item) {
            number++;
            sibling = sibling.getNext();
        }
        return number;
    }

    private boolean isTaskItem(ListItem listItem) {
        return listItem.getFirstChild() instanceof TaskListItemMarker;
    }

    @Override
    public void visit(CustomNode customNode) {
        if (customNode instanceof TaskListItemMarker marker) {
            insertText("[", MarkdownStyles.getCheckboxBracketAttributes());
            if (marker.isChecked()) {
                insertText("x", MarkdownStyles.getCheckboxCheckedMarkAttributes());
            } else {
                insertText(" ", MarkdownStyles.getCheckboxBracketAttributes());
            }
            insertText("]", MarkdownStyles.getCheckboxBracketAttributes());
            insertText(" ");
        } else if (customNode instanceof ColorSpan colorSpan) {
            insertText(colorSpan.getOpeningDelimiter(), MarkdownStyles.getMutedAttributes());
            SimpleAttributeSet colorAttrs = MarkdownStyles.copyOf(currentAttributes());
            MarkdownStyles.applyColorTag(colorAttrs, colorSpan.getColorNumber());
            attributeStack.push(colorAttrs);
            visitChildren(colorSpan);
            attributeStack.pop();

            insertText(colorSpan.getClosingDelimiter(), MarkdownStyles.getMutedAttributes());
        } else if (customNode instanceof RightAlign rightAlign) {
            visitRightAlign(rightAlign);
        } else {
            visitChildren(customNode);
        }
    }

    @Override
    public void visit(Code code) {
        String literal = code.getLiteral();
        String ticks = "`".repeat(longestBacktickRun(literal) + 1);

        SimpleAttributeSet chip = MarkdownStyles.copyOf(currentAttributes());
        MarkdownStyles.applyInlineCode(chip);

        insertText(ticks, MarkdownStyles.getMutedAttributes());
        insertText(" ", chip);          // left padding, carries the background
        insertText(literal, chip);
        insertText(" ", chip);          // right padding
        insertText(ticks, MarkdownStyles.getMutedAttributes());
    }

    private int longestBacktickRun(String s) {
        int max = 0, run = 0;
        for (int i = 0; i < s.length(); i++) {
            run = (s.charAt(i) == '`') ? run + 1 : 0;
            max = Math.max(max, run);
        }
        return max;
    }

    // ::text:: -- a "\t" before the span plus a single right-aligned TabStop on the paragraph
    // pushes everything after the tab flush against the right edge. The parser guarantees the
    // span is the last thing on its line, so "everything after the tab" is exactly the span.
    private void visitRightAlign(RightAlign rightAlign) {
        if (rightEdgePx > 0) {
            int tabOffset = insertOffset;
            insertText("\t", currentAttributes(), "tab");
            applyRightTabStop(tabOffset);
        }

        insertText(rightAlign.getOpeningDelimiter(), MarkdownStyles.getMutedAttributes());
        visitChildren(rightAlign);
        insertText(rightAlign.getClosingDelimiter(), MarkdownStyles.getMutedAttributes());
    }

    private void applyRightTabStop(int offsetInParagraph) {
        SimpleAttributeSet paragraphAttrs = new SimpleAttributeSet();
        TabStop stop = new TabStop(rightEdgePx + RIGHT_EDGE_ADJUST_PX, TabStop.ALIGN_RIGHT, TabStop.LEAD_NONE);
        StyleConstants.setTabSet(paragraphAttrs, new TabSet(new TabStop[]{stop}));
        doc.setParagraphAttributes(offsetInParagraph, 1, paragraphAttrs, false);
    }
}