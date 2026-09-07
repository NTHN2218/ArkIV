package AutoHotkey;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.BreakIterator;

/**
 * JetBrains/IntelliJ-style caret navigation. Word-jump (this feature) and
 * line-jump (added next) both live here since they're one conceptual family,
 * wired via a single attach(JTextArea) call per field.
 */
public class CaretNavigation {

    public static void attach(JTextArea area) {
        InputMap im = area.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = area.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK), "wordJumpRight");
        am.put("wordJumpRight", new WordJumpAction(area, true, false));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK), "wordJumpLeft");
        am.put("wordJumpLeft", new WordJumpAction(area, false, false));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "wordJumpRightSelect");
        am.put("wordJumpRightSelect", new WordJumpAction(area, true, true));

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "wordJumpLeftSelect");
        am.put("wordJumpLeftSelect", new WordJumpAction(area, false, true));
    }

    private static class WordJumpAction extends AbstractAction {
        private final JTextArea area;
        private final boolean forward;
        private final boolean extendSelection;

        WordJumpAction(JTextArea area, boolean forward, boolean extendSelection) {
            this.area = area;
            this.forward = forward;
            this.extendSelection = extendSelection;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String text = area.getText();
            if (text.isEmpty()) return;

            int caretPos = area.getCaretPosition();

            BreakIterator wordIterator = BreakIterator.getWordInstance();
            wordIterator.setText(text);

            int newPos = forward
                    ? nextWordBoundary(wordIterator, text, caretPos)
                    : previousWordBoundary(wordIterator, text, caretPos);

            if (extendSelection) {
                area.getCaret().moveDot(newPos);
            } else {
                area.setCaretPosition(newPos);
            }
        }

        // End of current/next word -- skips any boundary that doesn't sit at
        // the edge of an alphanumeric run (whitespace AND markdown punctuation
        // like * # - > ` [ ] ( ) etc. are all treated as noise to jump over).
        private int nextWordBoundary(BreakIterator it, String text, int pos) {
            int boundary = it.following(pos);
            while (boundary != BreakIterator.DONE && boundary < text.length()
                    && !endsAlphanumericRun(text, boundary)) {
                int next = it.next();
                if (next == BreakIterator.DONE) break;
                boundary = next;
            }
            return boundary == BreakIterator.DONE ? text.length() : boundary;
        }

        // Start of current/previous word -- same skip rule, mirrored.
        private int previousWordBoundary(BreakIterator it, String text, int pos) {
            int boundary = it.preceding(pos);
            while (boundary != BreakIterator.DONE && boundary > 0
                    && !startsAlphanumericRun(text, boundary)) {
                int prev = it.previous();
                if (prev == BreakIterator.DONE) break;
                boundary = prev;
            }
            return boundary == BreakIterator.DONE ? 0 : boundary;
        }

        // True if the character just before `boundary` is alphanumeric --
        // meaning this boundary is genuinely the end of a word, not a
        // whitespace/punctuation transition we should hop over.
        private boolean endsAlphanumericRun(String text, int boundary) {
            if (boundary <= 0) return false;
            return Character.isLetterOrDigit(text.charAt(boundary - 1));
        }

        // True if the character at `boundary` (start of the next segment)
        // is alphanumeric -- meaning this boundary is genuinely the start
        // of a word.
        private boolean startsAlphanumericRun(String text, int boundary) {
            if (boundary >= text.length()) return false;
            return Character.isLetterOrDigit(text.charAt(boundary));
        }

        private boolean isWhitespaceBoundary(String text, int boundary) {
            if (boundary <= 0 || boundary >= text.length()) return false;
            return Character.isWhitespace(text.charAt(boundary - 1))
                    && Character.isWhitespace(text.charAt(boundary));
        }
    }
}