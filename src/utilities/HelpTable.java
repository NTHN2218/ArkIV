package utilities;

import Markdown.MarkdownRenderer;
import utilities.HelpContent.Group;
import utilities.HelpContent.Kind;
import utilities.HelpContent.Row;
import utilities.HelpContent.Table;

import Markdown.WrapEditorKit;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import java.awt.*;

/**
 * Turns a HelpContent.Table into a themed panel.
 *
 * Column widths are computed up front from totalWidth (pixels) and every cell is sized to that
 * exact width before layout -- same trick TaskItem uses -- so wrapped text gets the right height
 * and the right-align (::text::) tab stop gets a real width to work with.
 */
public final class HelpTable {

    private static final int GAP = 14;        // horizontal gap between columns
    private static final int CELL_V_PAD = 6;  // top/bottom padding inside every cell

    private HelpTable() {}

    /** @param totalWidth pixel width available for the whole table */
    public static JComponent build(Table table, int totalWidth) {
        int cols = table.kinds().length;
        int usable = totalWidth - GAP * (cols - 1);
        int[] widths = new int[cols];
        int used = 0;
        for (int c = 0; c < cols - 1; c++) {
            widths[c] = (int) (usable * table.weights()[c]);
            used += widths[c];
        }
        widths[cols - 1] = usable - used;

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int y = 0;

        // Header row
        for (int c = 0; c < cols; c++) {
            addCell(grid, textCell(table.headers()[c], UniversalThemes.FONT_B_14,
                    UniversalThemes.TXT_SECONDARY, widths[c]), c, y, cols);
        }
        addRule(grid, ++y, cols);
        y++;

        for (Group group : table.groups()) {
            // Group heading
            JLabel title = new JLabel(group.title());
            title.setFont(UniversalThemes.FONT_B_17);
            title.setForeground(UniversalThemes.MD_COLOR_HEADING);
            GridBagConstraints tg = new GridBagConstraints();
            tg.gridx = 0; tg.gridy = y++; tg.gridwidth = cols;
            tg.anchor = GridBagConstraints.WEST;
            tg.insets = new Insets(18, 0, 4, 0);
            grid.add(title, tg);

            for (Row r : group.rows()) {
                String[] cells = r.cells();
                int src = 0; // next unread entry in cells[]
                for (int c = 0; c < cols; c++) {
                    JComponent cell;
                    switch (table.kinds()[c]) {
                        case TEXT -> cell = textCell(cells[src++], UniversalThemes.FONT_R_16,
                                UniversalThemes.TXT_PRIMARY, widths[c]);
                        case CODE -> cell = codeCell(cells[src++], widths[c]);
                        case RENDERED -> cell = renderedCell(cells[src - 1], widths[c]);
                        default -> throw new IllegalStateException();
                    }
                    addCell(grid, cell, c, y, cols);
                }
                y++;
                addRule(grid, y++, cols);
            }
        }

        // GridBagLayout centres its content when the container is taller than it needs --
        // pin it to the top instead.
        JPanel pinned = new JPanel(new BorderLayout());
        pinned.setOpaque(false);
        pinned.add(grid, BorderLayout.NORTH);
        return pinned;
    }

    // ── Cell builders ───────────────────────────────────────────────────────
    private static JTextArea textCell(String text, Font font, Color fg, int w) {
        JTextArea a = new JTextArea(text);
        noCaretScroll(a);
        a.setFont(font);
        a.setForeground(fg);
        a.setOpaque(false);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createEmptyBorder(CELL_V_PAD, 0, CELL_V_PAD, 0));
        UniversalThemes.applySelectionTheme(a);
        return sized(a, w);
    }

    private static JTextArea codeCell(String text, int w) {
        JTextArea a = new JTextArea(text);
        noCaretScroll(a);
        a.setFont(PathResolver.getRegularBaseFont().deriveFont(15f));
        a.setForeground(UniversalThemes.TXT_PRIMARY);
        a.setBackground(UniversalThemes.BG_COMPONENT);
        a.setOpaque(true);
        a.setEditable(false);
        a.setLineWrap(true); // char-wrap (no word-wrap) so long syntax strings never overflow
        a.setBorder(BorderFactory.createEmptyBorder(CELL_V_PAD, 8, CELL_V_PAD, 8));
        UniversalThemes.applySelectionTheme(a);
        return sized(a, w);
    }

    private static JTextPane renderedCell(String syntax, int w) {
        JTextPane pane = new JTextPane();
        noCaretScroll(pane);
        pane.setEditorKit(new WrapEditorKit());
        pane.setOpaque(false);
        pane.setEditable(false);
        pane.setBorder(BorderFactory.createEmptyBorder(CELL_V_PAD, 0, CELL_V_PAD, 0));
        pane.setCaretColor(UniversalThemes.BG_PANEL); // hide caret
        UniversalThemes.applySelectionTheme(pane);

        Insets in = pane.getInsets();
        int contentWidth = w - in.left - in.right;

        StyledDocument doc = new DefaultStyledDocument();
        MarkdownRenderer.render(doc, syntax, contentWidth);
        trimTrailingNewlines(doc); // renderer ends blocks with "\n" -> would add an empty last line
        pane.setDocument(doc);
        return sized(pane, w);
    }

    private static void trimTrailingNewlines(StyledDocument doc) {
        try {
            int len = doc.getLength();
            while (len > 0 && doc.getText(len - 1, 1).equals("\n")) {
                doc.remove(len - 1, 1);
                len--;
            }
        } catch (BadLocationException ignored) {}
    }

    /** Fix width to w, then measure the height the text needs at that width. */
    private static <T extends JComponent> T sized(T c, int w) {
        c.setSize(w, Short.MAX_VALUE);
        Dimension d = c.getPreferredSize();
        c.setPreferredSize(new Dimension(w, d.height));
        return c;
    }

    // ── Grid helpers ────────────────────────────────────────────────────────
    private static void addCell(JPanel grid, JComponent cell, int col, int row, int cols) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = col;
        g.gridy = row;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.fill = GridBagConstraints.VERTICAL;
        g.insets = new Insets(0, 0, 0, col < cols - 1 ? GAP : 0);
        grid.add(cell, g);
    }

    private static void addRule(JPanel grid, int row, int cols) {
        JPanel rule = new JPanel();
        rule.setBackground(UniversalThemes.BORDER_COLOR1);
        rule.setPreferredSize(new Dimension(1, 1));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = cols;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        grid.add(rule, g);
    }

    private static void noCaretScroll(javax.swing.text.JTextComponent t) {
        if (t.getCaret() instanceof javax.swing.text.DefaultCaret c) {
            c.setUpdatePolicy(javax.swing.text.DefaultCaret.NEVER_UPDATE);
        }
    }
}