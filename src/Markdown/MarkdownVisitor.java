package Markdown;

import org.commonmark.node.*;
import org.commonmark.ext.task.list.items.TaskListItemMarker;
import org.commonmark.node.SourceSpan;
import org.commonmark.node.OrderedList;

import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

import java.util.ArrayDeque;
import java.util.Deque;

public class MarkdownVisitor extends AbstractVisitor {

    private final StyledDocument doc;
    private final Deque<SimpleAttributeSet> attributeStack = new ArrayDeque<>();
    private int insertOffset;

    public MarkdownVisitor(StyledDocument doc) {
        this.doc = doc;
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
        } else {
            visitChildren(customNode);
        }
    }



}