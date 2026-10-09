package utilities;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Obsidian-settings-style Help: left nav (4 sections) + right content area (CardLayout).
 * Lives inside a UniversalThemes.RoundedDialog (JLayeredPane overlay), same as every other dialog.
 *
 * To fill a section in, replace its stub in buildMarkdownPanel() / buildKeybindsPanel() /
 * buildHotstringsPanel() / buildGeneralPanel(). Each returns the section BODY only --
 * the heading, padding and scrolling are added by buildSection().
 *
 * Keys while open: Esc closes, Up/Down switch sections.
 */
public class HelpDialog {

    private static final int NAV_WIDTH = 200;
    private static final int NAV_ROW_HEIGHT = 38;
    private static final int ACCENT_BAR_WIDTH = 3;

    private static final String[] SECTIONS = {"General Use", "Key Binds", "Mark Down", "Hot Strings"};

    private static final Color ACCENT_OVERLAY = withAlpha(UniversalThemes.ACCENT_COLOR, 45);
    //private static final Color HOVER_OVERLAY  = withAlpha(UniversalThemes.ACCENT_COLOR, 22);

    private static boolean isOpen = false; // stops double-click on the menu opening two dialogs

    private int bodyWidth;

    private final JFrame frame;
    private final UniversalThemes.RoundedDialog rd;
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final List<NavRow> navRows = new ArrayList<>();
    private int selectedIndex = 0;

    private final List<JScrollPane> scrollPanes = new ArrayList<>();

    // ── Entry point ─────────────────────────────────────────────────────────
    public static void show(JFrame frame) {
        if (isOpen) return;
        isOpen = true;
        new HelpDialog(frame).open();
    }

    private HelpDialog(JFrame frame) {
        this.frame = frame;
        this.rd = UniversalThemes.createRoundedDialogShell(frame, "Help");
    }

    private void open() {
        // ── Content area (fixed size so the dialog doesn't resize between sections) ──
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(UniversalThemes.BG_PANEL);
        content.setBorder(new LineBorder(UniversalThemes.BORDER_COLOR1, 1));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension size = new Dimension(
                clamp((int) (frame.getWidth() * 0.7), 640, 960),
                clamp((int) (frame.getHeight() * 0.7), 400, 640));
        content.setPreferredSize(size);
        content.setMaximumSize(size);

        content.add(buildNav(), BorderLayout.WEST);

        // content width - nav - borders - page padding - scrollbar
        bodyWidth = size.width - NAV_WIDTH - 3 - 48 - 12;

        cardPanel.setOpaque(false);
        cardPanel.add(buildSection(SECTIONS[0],buildGeneralPanel() ), SECTIONS[0]);
        cardPanel.add(buildSection(SECTIONS[1], buildKeybindsPanel()), SECTIONS[1]);
        cardPanel.add(buildSection(SECTIONS[2], buildMarkdownPanel()), SECTIONS[2]);
        cardPanel.add(buildSection(SECTIONS[3], buildGeneralPanel()), SECTIONS[3]);
        content.add(cardPanel, BorderLayout.CENTER);

        rd.body.add(content);

        // ── Keyboard: Esc closes, Up/Down switch section ──
        content.setFocusable(true);
        InputMap im = content.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = content.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "helpPrev");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "helpNext");

        am.put("helpPrev", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { select(selectedIndex - 1); }
        });
        am.put("helpNext", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { select(selectedIndex + 1); }
        });

        rd.setOnCloseRequest(this::close);
        select(0);
        rd.show();
        SwingUtilities.invokeLater(() -> { content.requestFocusInWindow(); select(0); });
    }

    private void close() {
        isOpen = false;
        rd.close();
    }

    // ── Left nav ────────────────────────────────────────────────────────────
    private JPanel buildNav() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(UniversalThemes.BG_SIDEBAR);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UniversalThemes.BORDER_COLOR1),
                BorderFactory.createEmptyBorder(8, 0, 8, 0)));
        nav.setPreferredSize(new Dimension(NAV_WIDTH, 0));

        for (int i = 0; i < SECTIONS.length; i++) {
            NavRow row = new NavRow(SECTIONS[i], i);
            navRows.add(row);
            nav.add(row);
        }
        nav.add(Box.createVerticalGlue());
        return nav;
    }

    private void select(int index) {
        // wrap around both directions
        selectedIndex = (index + SECTIONS.length) % SECTIONS.length;
        cards.show(cardPanel, SECTIONS[selectedIndex]);
        JScrollPane sp = scrollPanes.get(selectedIndex);
        SwingUtilities.invokeLater(() -> sp.getViewport().setViewPosition(new Point(0, 0)));
        for (int i = 0; i < navRows.size(); i++) {
            navRows.get(i).setSelected(i == selectedIndex);
        }
    }

    private class NavRow extends JPanel {
        private final JLabel label;
        private boolean selected = false;
        private boolean hovered = false;

        NavRow(String text, int index) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(NAV_WIDTH, NAV_ROW_HEIGHT));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, NAV_ROW_HEIGHT));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            label = new JLabel(text);
            label.setFont(UniversalThemes.FONT_R_16);
            label.setForeground(UniversalThemes.TXT_PRIMARY);
            label.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
            add(label, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) {
                    select(index);
                    // keep keyboard focus on the content so Esc / Up / Down keep working
                    Container c = NavRow.this.getParent();
                    while (c != null && !c.isFocusable()) c = c.getParent();
                    if (c != null) c.requestFocusInWindow();
                }
            });
        }

        void setSelected(boolean selected) {
            this.selected = selected;
            label.setForeground(selected ? UniversalThemes.MD_COLOR_HEADING : UniversalThemes.TXT_PRIMARY);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (selected) {
                g.setColor(ACCENT_OVERLAY);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(UniversalThemes.ACCENT_COLOR);
                g.fillRect(0, 0, ACCENT_BAR_WIDTH, getHeight());
            } else if (hovered) {

            }
            super.paintComponent(g);
        }
    }

    // ── Right side: heading + body, wrapped in a themed scroll pane ──────────
    private JComponent buildSection(String title, JComponent body) {
        JPanel page = new WidthTrackingPanel(new BorderLayout(0, 14));
        page.setBackground(UniversalThemes.BG_PANEL);
        page.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel heading = new JLabel(title);
        heading.setFont(UniversalThemes.FONT_B_20.deriveFont(26f));
        heading.setForeground(UniversalThemes.MD_COLOR_HEADING);
        page.add(heading, BorderLayout.NORTH);
        page.add(body, BorderLayout.CENTER);

        JScrollPane sp = new JScrollPane(page);
        scrollPanes.add(sp);
        sp.getVerticalScrollBar().setValue(0);
        sp.setBorder(null);
        sp.getViewport().setBackground(UniversalThemes.BG_PANEL);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        UniversalThemes.applyScrollbarTheme(sp);
        return sp;
    }

    // ── Section bodies -- FILL THESE IN ──────────────────────────────────────
    private JComponent buildMarkdownPanel() {
        return HelpTable.build(HelpContent.MARKDOWN, bodyWidth);
    }
    private JComponent buildKeybindsPanel()   { return placeholder(); }
    private JComponent buildHotstringsPanel() { return placeholder(); }
    private JComponent buildGeneralPanel()    { return placeholder(); }

    private JComponent placeholder() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel("Coming soon.");
        l.setFont(UniversalThemes.FONT_R_16);
        l.setForeground(UniversalThemes.TXT_SECONDARY);
        p.add(l, BorderLayout.NORTH);
        return p;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────
    /** Panel that always matches the scroll pane's width (so content wraps, never scrolls sideways). */
    public static class WidthTrackingPanel extends JPanel implements Scrollable {
        public WidthTrackingPanel(LayoutManager lm) { super(lm); }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 100; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}