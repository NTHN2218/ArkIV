package Markdown;

import org.commonmark.node.*;
import org.commonmark.ext.task.list.items.TaskListItemMarker;

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
        Node item = bulletList.getFirstChild();
        while (item != null) {
            item.accept(this);
            Node next = item.getNext();
            if (next != null && !bulletList.isTight()) {
                insertText("\n");
            }
            item = next;
        }
        if (bulletList.getNext() != null) {
            insertText("\n");
        }
    }

    //Checklist
    @Override
    public void visit(ListItem listItem) {
        boolean checkedTaskItem = isCheckedTaskItem(listItem);
        MarkdownDebug.log("[MD] ListItem checkedTaskItem=" + checkedTaskItem);
        if (checkedTaskItem) {
            attributeStack.push(MarkdownStyles.getCheckedTaskTextAttributes());
        }

        visitChildren(listItem);

        if (checkedTaskItem) {
            attributeStack.pop();
        }
    }

    private boolean isCheckedTaskItem(ListItem listItem) {
        Node first = listItem.getFirstChild();
        return first instanceof TaskListItemMarker marker && marker.isChecked();
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