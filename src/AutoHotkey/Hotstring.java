package AutoHotkey;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Line-start (or anywhere) text shortcuts: typing a trigger string followed
 * by space expands it into a longer snippet, e.g. "-[" + space -> "- [ ] ".
 *
 * New shortcuts are added as new RULES entries -- nothing else needs to change.
 */
public class Hotstring {

    private Hotstring() {}

    public record HotstringRule(String trigger, String expansion, boolean requireStartOfLine) {}

    // Add new shortcuts here -- each is a standalone rule, order doesn't matter
    // unless two triggers could overlap (first match wins in that case).
    private static final List<HotstringRule> RULES = new ArrayList<>();
    static {
        RULES.add(new HotstringRule("-[", "- [ ] ", true));
        RULES.add(new HotstringRule("-->", "—⟶ ", false));
    }

    public static void attach(JTextComponent textComponent) {
        textComponent.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { handle(e); }
            @Override public void removeUpdate(DocumentEvent e) {}
            @Override public void changedUpdate(DocumentEvent e) {}

            private void handle(DocumentEvent e) {
                if (e.getLength() != 1) return; // only react to single-character typing
                int offset = e.getOffset();
                SwingUtilities.invokeLater(() -> tryExpand(textComponent, offset));
            }
        });
    }

    private static void tryExpand(JTextComponent comp, int typedOffset) {
        try {
            Document doc = comp.getDocument();
            if (typedOffset >= doc.getLength()) return;
            if (!" ".equals(doc.getText(typedOffset, 1))) return;

            for (HotstringRule rule : RULES) {
                int triggerStart = typedOffset - rule.trigger().length();
                if (triggerStart < 0) continue;
                if (rule.requireStartOfLine() && !isStartOfLine(doc, triggerStart)) continue;
                if (!doc.getText(triggerStart, rule.trigger().length()).equals(rule.trigger())) continue;

                doc.remove(triggerStart, rule.trigger().length() + 1); // trigger + the space that triggered it
                doc.insertString(triggerStart, rule.expansion(), null);
                return;
            }
        } catch (BadLocationException ex) {
            System.err.println("[Hotstring] expansion failed: " + ex.getMessage());
        }
    }

    private static boolean isStartOfLine(Document doc, int offset) throws BadLocationException {
        return offset == 0 || doc.getText(offset - 1, 1).equals("\n");
    }
}