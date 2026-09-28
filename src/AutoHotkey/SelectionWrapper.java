package AutoHotkey;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Obsidian-style selection wrapping: select text, press a trigger shortcut,
 * and the selection gets wrapped in that shortcut's associated delimiter.
 * No selection = delimiter is inserted normally.
 * Nesting is allowed (no toggle/unwrap).
 * Not logged to ActionUndoManager -- treated as plain in-field text editing.
 */
public class SelectionWrapper {

    // Ctrl+Alt+number -> wrap string
    private static final Map<KeyStroke, String> WRAP_REGISTRY = new HashMap<>();

    static {
        /*
        VIBGYOR color tags
         */
        WRAP_REGISTRY.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
                "^1"
        );
        WRAP_REGISTRY.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
                "^2"
        );
        WRAP_REGISTRY.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
                "^3"
        );
        WRAP_REGISTRY.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
                "^4"
        );
        WRAP_REGISTRY.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
                "^5"
        );
        WRAP_REGISTRY.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
                "^6"
        );
        WRAP_REGISTRY.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
                "^7"
        );

        /*
        Emphasis
         */
        //Italic
        WRAP_REGISTRY.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
                "*"
        );
        //Bold
        WRAP_REGISTRY.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
                "**"
        );
    }

    public static void attach(JTextArea area) {
        InputMap im = area.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = area.getActionMap();

        for (Map.Entry<KeyStroke, String> entry : WRAP_REGISTRY.entrySet()) {
            KeyStroke keyStroke = entry.getKey();
            String wrap = entry.getValue();

            String actionKey = "wrap-" + wrap;
            im.put(keyStroke, actionKey);
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
                area.replaceSelection(wrap);
                return;
            }

            String selected = area.getSelectedText();
            String replacement = wrap + selected + wrap;

            area.replaceRange(replacement, start, end);

            int newInnerStart = start + wrap.length();
            int newInnerEnd = newInnerStart + selected.length();

            SwingUtilities.invokeLater(() ->
                    area.select(newInnerStart, newInnerEnd)
            );
        }
    }
}

