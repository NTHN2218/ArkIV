package AutoHotkey;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Obsidian-style selection wrapping: select text, press a trigger character,
 * and the selection gets wrapped in that character's associated delimiter.
 * No selection = character types normally. Nesting is allowed (no toggle/unwrap).
 * Not logged to ActionUndoManager -- treated as plain in-field text editing.
 */
public class SelectionWrapper {

    // Trigger character -> wrap string. v1 ships with '*' only; extend by adding entries here.
    private static final Map<Character, String> WRAP_REGISTRY = new HashMap<>();
    static {
        WRAP_REGISTRY.put('*', "*");
    }

    public static void attach(JTextArea area) {
        InputMap im = area.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = area.getActionMap();

        for (Map.Entry<Character, String> entry : WRAP_REGISTRY.entrySet()) {
            char trigger = entry.getKey();
            String wrap = entry.getValue();

            String actionKey = "wrap-" + trigger;
            im.put(KeyStroke.getKeyStroke(trigger), actionKey);
            am.put(actionKey, new WrapAction(area, wrap));
        }
    }

    private static class WrapAction extends AbstractAction {
        private final JTextArea area;
        private final String wrap;

        WrapAction(JTextArea area, String wrap) {
            this.area = area;
            this.wrap = wrap;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int start = area.getSelectionStart();
            int end = area.getSelectionEnd();

            if (start == end) {
                area.replaceSelection(String.valueOf(getTriggerChar()));
                return;
            }

            String selected = area.getSelectedText();
            String replacement = wrap + selected + wrap;

            area.replaceRange(replacement, start, end);

            int newInnerStart = start + wrap.length();
            int newInnerEnd = newInnerStart + selected.length();

            SwingUtilities.invokeLater(() -> area.select(newInnerStart, newInnerEnd));
        }

        private char getTriggerChar() {
            // wrap string and trigger char are the same for v1's single-char wraps;
            // safe simplification since WRAP_REGISTRY only maps single chars to themselves for now.
            return wrap.charAt(0);
        }
    }
}