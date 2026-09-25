package utilities;

import AutoHotkey.Hotstring;
import AutoHotkey.SelectionWrapper;
import AutoHotkey.CaretNavigation;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Shared "equipping" for text-entry fields used inside a UniversalThemes.RoundedDialog --
 * theming, Hotstring/SelectionWrapper/CaretNavigation wiring, auto-grow, Enter-to-submit,
 * and the standard Primary/Cancel button row. Pulled out of ArkIV so every dialog method
 * (New Entry, Edit, Create Sub-Entry) shares one copy of this setup instead of repeating
 * it, keeping future tweaks and merges to a single file.
 */
public class DialogFieldKit {

    private DialogFieldKit() {} // static-utility class -- never instantiated

    public static JTextArea createThemedTextEntryField(String initialText, int rows, int cols) {
        JTextArea field = (initialText != null) ? new JTextArea(initialText, rows, cols) : new JTextArea(rows, cols);
        field.setBackground(UniversalThemes.BG_COMPONENT);
        field.setForeground(UniversalThemes.TXT_PRIMARY);
        field.setCaretColor(UniversalThemes.ACCENT_COLOR);
        field.setFont(PathResolver.getRegularBaseFont().deriveFont(17f));
        field.setLineWrap(true);
        field.setWrapStyleWord(true);
        field.setMargin(new Insets(10, 10, 10, 10));
        field.setBorder(null);
        Hotstring.attach(field);
        SelectionWrapper.attach(field);
        CaretNavigation.attach(field);
        UniversalThemes.applySelectionTheme(field);
        UniversalThemes.applyCollapseSelectionNavigation(field);
        UniversalThemes.freeCtrlTabFromTraversal(field);
        return field;
    }

    public static JScrollPane wrapInThemedScrollPane(JTextArea field, int maxHeight) {
        JScrollPane scrollPane = new JScrollPane(field);
        scrollPane.setBorder(BorderFactory.createLineBorder(UniversalThemes.BORDER_COLOR1, 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        UniversalThemes.applyScrollbarTheme(scrollPane);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
        return scrollPane;
    }

    // Auto-grow: expands the field's row count (capped at maxRows) as the user
    // types past the visible area, and tells the dialog to re-measure/re-center.
    public static void attachAutoGrow(JTextArea field, UniversalThemes.RoundedDialog rd, int maxRows, boolean scrollToCaretOnType) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { adjust(); }
            public void removeUpdate(DocumentEvent e) { adjust(); }
            public void changedUpdate(DocumentEvent e) { adjust(); }
            private void adjust() {
                int rows = field.getLineCount();
                if (rows > field.getRows()) {
                    field.setRows(Math.min(rows, maxRows));
                    rd.refresh();
                }
                if (scrollToCaretOnType && field.getCaretPosition() == field.getDocument().getLength()) {
                    SwingUtilities.invokeLater(() -> field.setCaretPosition(field.getDocument().getLength()));
                }
            }
        });
    }

    // Enter submits, Shift+Enter inserts a literal newline -- the same binding
    // every entry/edit/sub-entry dialog uses.
    public static void bindEnterToSubmit(JTextArea field, Runnable submit) {
        InputMap im = field.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = field.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), "insert-newline");
        am.put("insert-newline", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int caretPos = field.getCaretPosition();
                String text = field.getText();
                field.setText(text.substring(0, caretPos) + "\n" + text.substring(caretPos));
                field.setCaretPosition(caretPos + 1);
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit-entry");
        am.put("submit-entry", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { submit.run(); }
        });
    }

    // Standard Primary/Cancel button row, wired and added to rd.body in one call.
    public static void addPrimaryCancelRow(UniversalThemes.RoundedDialog rd, String primaryLabel, Runnable onPrimary, Runnable onCancel) {
        JButton primaryButton = UniversalThemes.createRoundedDialogButton(primaryLabel, UniversalThemes.ACCENT_COLOR,
                UniversalThemes.TXT_SELECTED, UniversalThemes.ACCENT_COLOR_DARK);
        JButton cancelButton = UniversalThemes.createRoundedDialogButton("Cancel", UniversalThemes.BG_COMPONENT,
                UniversalThemes.TXT_PRIMARY, UniversalThemes.BORDER_COLOR1);
        primaryButton.addActionListener(e -> onPrimary.run());
        cancelButton.addActionListener(e -> onCancel.run());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRow.add(primaryButton);
        UniversalThemes.wireDialogButtonNavigation(primaryButton, cancelButton);
        rd.body.add(buttonRow);
    }
}