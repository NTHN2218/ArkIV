package utilities;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.geom.RoundRectangle2D;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;

public class UniversalThemes {

    ///==============================================================================================================
    ///== Theme Colors
    ///==============================================================================================================
    public static final Color BG_MAIN        = new Color(0x0F0F12);
    public static final Color TITLE_BAR      = new Color(0x191621);
    public static final Color BG_PANEL       = new Color(0x1A1A1E);
    public static final Color BG_COMPONENT   = new Color(0x222226);
    public static final Color BG_SIDEBAR     = new Color(0x1e1f22);
    public static final Color TXT_PRIMARY    = new Color(0xE5E5E5);
    public static final Color TXT_SECONDARY  = new Color(0xBEBEBE);
    public static final Color BORDER_COLOR1   = new Color(0x303036);
    public static final Color BORDER_COLOR2   = new Color(0x6A6A6A);


    //public static final Color ACCENT_COLOR    = new Color(0x2fafbc);
    public static final Color ACCENT_COLOR    = new Color(0x1EABCA);
    public static final Color ACCENT_COLOR_DARK = new Color(0x2b929d);  //0xC66A1A
    public static final Color SEARCH_HIGHLIGHT_COLOR = new Color(0x2b929d); // distinct from ACCENT_COLOR


    //Delete Pop-up Colors
    public static final Color BG_CANCEL_BTN = new Color(0x3f3f3f);  //0xE67E22
    public static final Color BG_DELETE_BTN = new Color(0xfb464c); // destructive actions (delete)

    public static final Color BG_HOVERED = new Color(0x2A2B2F);
    public static final Color BG_LINENUM = new Color(0x858585);
    
    public static final Color DISABLED_TEXT  = new Color(0x6B6B6B);
    public static final Color DIMMED_TEXT  = new Color(0x1E1E22);
    public static final Color TXT_SELECTED   = Color.BLACK;

    //Markdown Features Colors
    public static final Color MD_COLOR_HEADING = new Color(0x0fb6d6);
    //public static final Color MD_COLOR_BOLD = new Color(0x94A5F2);
    public static final Color MD_COLOR_BOLD = new Color(0xE5E5E5);
    // Inline code (Obsidian-style chip)
    public static final Color MD_COLOR_CODE_FG = new Color(0x0fbfe2);     // 0x0fb6d6
    public static final Color MD_COLOR_CODE_BG = new Color(0x1C2D35);    // blended with ACCENT_COLOR, BG_PANEL and BG_COMPONENT


    // VIBGYOR Color Tag palette (^1..^7) -- tuned for readability on BG_MAIN/BG_PANEL,
    // deliberately not raw textbook VIBGYOR values since some wash out or clash on dark bg.
    public static final Color MD_COLOR_VIOLET = new Color(0xC084FC);
    public static final Color MD_COLOR_INDIGO = new Color(0x818CF8);
    public static final Color MD_COLOR_BLUE   = new Color(0x60A5FA);
    public static final Color MD_COLOR_GREEN  = new Color(0x4ADE80);
    public static final Color MD_COLOR_YELLOW = new Color(0xFACC15);
    public static final Color MD_COLOR_ORANGE = new Color(0xFB923C);
    public static final Color MD_COLOR_RED    = new Color(0xF87171);







    ///==============================================================================================================
    ///== Fonts
    ///==============================================================================================================
    private static final Font BASE_REGULAR = PathResolver.getRegularBaseFont();
    private static final Font BASE_BOLD    = PathResolver.getBoldBaseFont();
    private static final Font BASE_ITALIC  = PathResolver.getItalicBaseFont();
    private static final Font BASE_NERD_ICON = PathResolver.getNerdIconBaseFont();

    public static final Font FONT_R_10  = BASE_REGULAR.deriveFont(Font.PLAIN, 10f);
    public static final Font FONT_R_11  = BASE_REGULAR.deriveFont(Font.PLAIN, 11f);
    public static final Font FONT_R_12  = BASE_REGULAR.deriveFont(Font.PLAIN, 12f);
    public static final Font FONT_R_13  = BASE_REGULAR.deriveFont(Font.PLAIN, 13f);
    public static final Font FONT_R_14  = BASE_REGULAR.deriveFont(Font.PLAIN, 14f);
    public static final Font FONT_R_15  = BASE_REGULAR.deriveFont(Font.PLAIN, 15f);
    public static final Font FONT_R_16  = BASE_REGULAR.deriveFont(Font.PLAIN, 16f);
    public static final Font FONT_R_17  = BASE_REGULAR.deriveFont(Font.PLAIN, 17f);
    public static final Font FONT_R_18  = BASE_REGULAR.deriveFont(Font.PLAIN, 18f);
    public static final Font FONT_R_19  = BASE_REGULAR.deriveFont(Font.PLAIN, 19f);
    public static final Font FONT_R_20  = BASE_REGULAR.deriveFont(Font.PLAIN, 20f);

    public static final Font FONT_B_10   = BASE_BOLD.deriveFont(Font.BOLD, 10f);
    public static final Font FONT_B_11   = BASE_BOLD.deriveFont(Font.BOLD, 11f);
    public static final Font FONT_B_12   = BASE_BOLD.deriveFont(Font.BOLD, 12f);
    public static final Font FONT_B_13   = BASE_BOLD.deriveFont(Font.BOLD, 13f);
    public static final Font FONT_B_14   = BASE_BOLD.deriveFont(Font.BOLD, 14f);
    public static final Font FONT_B_15   = BASE_BOLD.deriveFont(Font.BOLD, 15f);
    public static final Font FONT_B_16   = BASE_BOLD.deriveFont(Font.BOLD, 16f);
    public static final Font FONT_B_17   = BASE_BOLD.deriveFont(Font.BOLD, 17f);
    public static final Font FONT_B_18   = BASE_BOLD.deriveFont(Font.BOLD, 18f);
    public static final Font FONT_B_19   = BASE_BOLD.deriveFont(Font.BOLD, 19f);
    public static final Font FONT_B_20   = BASE_BOLD.deriveFont(Font.BOLD, 20f);

    public static final Font FONT_NERD_ICON_SMALL = BASE_NERD_ICON.deriveFont(Font.PLAIN, 25f);


    public static final Font FONT_EMOJI       = new Font("Segoe UI Emoji", Font.PLAIN, 18);
    public static final Font FONT_EMOJI1       = new Font("Segoe UI Emoji", Font.PLAIN, 16);
    public static final Font FONT_EMOJI2       = new Font("Segoe UI Emoji", Font.PLAIN, 20);
    public static final Font FONT_EMOJI3       = new Font("Segoe UI Emoji", Font.PLAIN, 22);


    public static Font getCompositeFont(int size) {
        // JetBrains Mono for regular text; Java's own per-glyph font
        // substitution automatically falls back to an OS emoji font
        // for any character JetBrains Mono can't render (e.g. emoji).
        return PathResolver.getRegularBaseFont().deriveFont((float) size);
    }

    ///==============================================================================================================
    ///== Dialog Shell & Helpers
    ///==============================================================================================================
    ///==============================================================================================================
    ///== Dialog Shell & Helpers  (JLayeredPane overlay -- lives inside the frame,
    ///== not a second OS window, so there's nothing for Windows to give its own
    ///== taskbar identity or raise above other apps.)
    ///==============================================================================================================
    public static final int DIALOG_CORNER_RADIUS = 16;

    public static class RoundedDialog {
        public final JPanel body; // caller adds content here

        private final JFrame ownerFrame;
        private final JLayeredPane layeredPane;
        private final JPanel dimOverlay;
        private final JPanel shellPanel;
        private final ComponentAdapter resizeListener;
        private Runnable onCloseRequest;
        private boolean showing = false;
        private Component previousFocus;

        RoundedDialog(JFrame ownerFrame, JLayeredPane layeredPane, JPanel dimOverlay, JPanel shellPanel, JPanel body) {
            this.ownerFrame = ownerFrame;
            this.layeredPane = layeredPane;
            this.dimOverlay = dimOverlay;
            this.shellPanel = shellPanel;
            this.body = body;
            this.onCloseRequest = this::close; // default: X button just closes
            this.resizeListener = new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) { refresh(); }
            };
        }

        // Recompute size/position -- call after adding content that changes
        // preferred size (e.g. an auto-growing text area).
        public void show() {
            previousFocus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (showing) return;
            showing = true;
            layeredPane.add(dimOverlay, Integer.valueOf(JLayeredPane.MODAL_LAYER));
            layeredPane.add(shellPanel, Integer.valueOf(JLayeredPane.MODAL_LAYER.intValue() + 1));
            layeredPane.moveToFront(shellPanel); // guarantee z-order regardless of add-order
            ownerFrame.addComponentListener(resizeListener);
            refresh();
            layeredPane.revalidate();
            layeredPane.repaint();
        }

        public void refresh() {
            Dimension frameSize = ownerFrame.getRootPane().getSize();
            if (frameSize.width <= 0 || frameSize.height <= 0) {
                frameSize = ownerFrame.getSize(); // fallback if rootPane hasn't reported yet
            }
            dimOverlay.setBounds(0, 0, frameSize.width, frameSize.height);

            // Force header/body to actually lay out before trusting their preferred size --
            // an unvalidated BorderLayout container can report an unreliable size otherwise.
            shellPanel.doLayout();
            Dimension pref = shellPanel.getPreferredSize();

            // Floor values so the dialog can never come out effectively invisible.
            int w = Math.max(320, Math.min(pref.width, frameSize.width - 40));
            int h = Math.max(160, Math.min(pref.height, frameSize.height - 40));
            int x = (frameSize.width - w) / 2;
            int y = (frameSize.height - h) / 2;
            shellPanel.setBounds(x, y, w, h);

            shellPanel.revalidate();
            shellPanel.repaint();
            dimOverlay.repaint();
        }

        public void close() {
            if (!showing) return;
            showing = false;
            ownerFrame.removeComponentListener(resizeListener);
            layeredPane.remove(shellPanel);
            layeredPane.remove(dimOverlay);
            layeredPane.revalidate();
            layeredPane.repaint();
            ownerFrame.repaint(); // force a full repaint in case the RepaintManager's
            // dirty-region tracking still missed the vacated area
            SwingUtilities.invokeLater(() -> {
                if (previousFocus != null && previousFocus.isShowing()) {
                    previousFocus.requestFocusInWindow();
                }
            });
        }

        // Overrides what the header's X button does. Default is close(); pass
        // your own handler when closing needs extra logic (treat X as Cancel, etc.)
        public void setOnCloseRequest(Runnable r) { this.onCloseRequest = r; }

        void requestClose() { onCloseRequest.run(); }
    }

    public static RoundedDialog createRoundedDialogShell(Component parent, String titleText) {
        Window ownerWindow = (parent instanceof Window)
                ? (Window) parent
                : SwingUtilities.getWindowAncestor(parent);
        if (!(ownerWindow instanceof JFrame ownerFrame)) {
            throw new IllegalStateException("RoundedDialog requires a JFrame ancestor");
        }
        JLayeredPane layeredPane = ownerFrame.getLayeredPane();

        // Blocks clicks to the app behind it, same job OS modality used to do --
        // just a real opaque component now, not a system-level enforcement.
        JPanel dimOverlay = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                // Non-opaque, so we paint the tint manually -- opaque(true) + an
                // alpha color is the actual bug: it lies to the RepaintManager about
                // fully covering this region, which corrupts its dirty-region
                // tracking once this panel is later removed.
                g.setColor(new Color(0, 0, 0, 120));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        dimOverlay.setOpaque(false);

        JPanel shellPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DIALOG_CORNER_RADIUS, DIALOG_CORNER_RADIUS);

                float strokeWidth = 1.5f;
                g2.setColor(BORDER_COLOR1);
                g2.setStroke(new BasicStroke(strokeWidth));
                float inset = strokeWidth / 2f;
                g2.draw(new RoundRectangle2D.Float(
                        inset, inset,
                        getWidth() - strokeWidth, getHeight() - strokeWidth,
                        DIALOG_CORNER_RADIUS, DIALOG_CORNER_RADIUS
                ));

                g2.dispose();
            }
        };
        shellPanel.setOpaque(false);
        shellPanel.setBackground(BG_PANEL);
        shellPanel.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(FONT_B_18);
        titleLabel.setForeground(TXT_PRIMARY);
        header.add(titleLabel, BorderLayout.WEST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(14, 2, 0, 2));

        RoundedDialog rd = new RoundedDialog(ownerFrame, layeredPane, dimOverlay, shellPanel, body);

        JButton closeButton = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TXT_SECONDARY);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2, arm = 5;
                g2.drawLine(cx - arm, cy - arm, cx + arm, cy + arm);
                g2.drawLine(cx - arm, cy + arm, cx + arm, cy - arm);
                g2.dispose();
            }
        };
        closeButton.setPreferredSize(new Dimension(24, 24));
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusable(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> rd.requestClose());
        header.add(closeButton, BorderLayout.EAST);

        shellPanel.add(header, BorderLayout.NORTH);
        shellPanel.add(body, BorderLayout.CENTER);

        return rd;
    }

    public static void showToast(Component parent, String message) {
        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        if (!(owner instanceof JFrame)) return;
        JFrame frame = (JFrame) owner;

        JLabel label = new JLabel(message);
        label.setFont(FONT_R_16);
        label.setForeground(TXT_PRIMARY);

        JPanel toastPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_COMPONENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_COLOR2);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        toastPanel.setOpaque(false);
        toastPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        toastPanel.add(label, BorderLayout.CENTER);

        Dimension pref = toastPanel.getPreferredSize();
        int margin = 24;
        int x = frame.getWidth() - pref.width - margin;
        int y = margin;
        toastPanel.setBounds(x, y, pref.width, pref.height);

        JPanel glass = new JPanel(null);
        glass.setOpaque(false);
        glass.add(toastPanel);

        frame.setGlassPane(glass);
        glass.setVisible(true);
        glass.revalidate();
        glass.repaint();

        Timer dismissTimer = new Timer(1600, e -> glass.setVisible(false));
        dismissTimer.setRepeats(false);
        dismissTimer.start();
    }



    public static JButton createRoundedDialogButton(String text, Color bg, Color fg, Color hoverBg) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override protected void paintBorder(Graphics g) {
                if (isFocusOwner()) {
                    Color btnColor = bg==BG_DELETE_BTN ? Color.BLACK : Color.GRAY ;

                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(BG_COMPONENT);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
                    g2.dispose();
                }
            }
        };
        button.setFont(FONT_R_16);
        button.setForeground(fg);
        button.setBackground(bg);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setFocusable(true);
        button.setOpaque(false);
        button.setUI(new NoPressedButtonUI());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(hoverBg); }
            @Override public void mouseExited(MouseEvent e)  { button.setBackground(bg); }
        });
        button.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { button.repaint(); }
            @Override public void focusLost(FocusEvent e)   { button.repaint(); }
        });
        return button;
    }

    ///==============================================================================================================
    ///== Dialog Button Keyboard Navigation
    ///==============================================================================================================
    public static void wireDialogButtonNavigation(JButton... buttons) {
        if (buttons.length == 0) return;

        for (int i = 0; i < buttons.length; i++) {
            final JButton current = buttons[i];
            final int index = i;

            InputMap im = current.getInputMap(JComponent.WHEN_FOCUSED);
            ActionMap am = current.getActionMap();

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "navNext");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "navPrev");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "navActivate");

            am.put("navNext", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    buttons[(index + 1) % buttons.length].requestFocusInWindow();
                }
            });
            am.put("navPrev", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    buttons[(index - 1 + buttons.length) % buttons.length].requestFocusInWindow();
                }
            });
            am.put("navActivate", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    current.doClick();
                }
            });
        }
    }


    ///==============================================================================================================
    ///== Button UI Helpers
    ///==============================================================================================================
    public static class NoPressedButtonUI extends BasicButtonUI {
        @Override
        protected void paintButtonPressed(Graphics g, AbstractButton b) {
            // Disable default pressed effect
        }
    }

    public static void ClickEffect(JButton button) {

        Color normalBg = ACCENT_COLOR;
        Color hoverBg = ACCENT_COLOR_DARK;
        Color disabledBg = BG_COMPONENT;          // or darker shade if you want
        Color disabledFg = ACCENT_COLOR;

        // Initial paint
        button.setBackground(button.isEnabled() ? normalBg : disabledBg);
        button.setForeground(button.isEnabled() ? TXT_SELECTED : disabledFg);

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (!button.isEnabled()) return;
                button.setBackground(hoverBg);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!button.isEnabled()) return;
                button.setBackground(normalBg);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.isEnabled()) return;
                button.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!button.isEnabled()) return;
                button.setBackground(normalBg);
            }
        });

        // Cursor should reflect disabled state
        button.addPropertyChangeListener("enabled", evt -> {
            if (button.isEnabled()) {
                button.setBackground(normalBg);
                button.setForeground(TXT_SELECTED);
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                button.setBackground(disabledBg);
                button.setForeground(disabledFg);
                button.setCursor(Cursor.getDefaultCursor());
            }
            button.repaint();
        });

        // Remove Swing focus glow (important for tab-like buttons)
        button.setFocusable(false);
    }


    public static void removeFocusFromAllButtons(Container c) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JButton btn) {
                btn.setFocusPainted(false);
                btn.setFocusable(false);
                btn.setUI(new NoPressedButtonUI());
            }
            if (comp instanceof Container cont) {
                removeFocusFromAllButtons(cont);
            }
        }
    }



    ///==============================================================================================================
    ///== Scrollbar Theme
    ///==============================================================================================================
    public static void applyScrollbarTheme(JScrollPane scrollPane) {

        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {

            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = UniversalThemes.ACCENT_COLOR; // Orange
                this.trackColor = UniversalThemes.BG_COMPONENT;     // Dark background
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill thumb
                g2.setColor(thumbColor);
                g2.fillRect(thumbBounds.x+3, thumbBounds.y, thumbBounds.width-3, thumbBounds.height);

// Draw inner black rectangle with a 1px margin on all sides
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1f));
//                g2.drawRect(
//                        thumbBounds.x + 1,                // move 1px right
//                        thumbBounds.y + 1,                // little top margin (optional)
//                        thumbBounds.width - 3,            // shrink width so right side isn't clipped
//                        thumbBounds.height - 3            // shrink height to match style
//                );
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize(JComponent c) {
                return new Dimension(12, super.getPreferredSize(c).height);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });

        scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {

            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = UniversalThemes.ACCENT_COLOR; // Orange
                this.trackColor = UniversalThemes.BG_SIDEBAR;     // Dark background
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill thumb
                g2.setColor(thumbColor);
                g2.fillRect(thumbBounds.x, thumbBounds.y+3, thumbBounds.width, thumbBounds.height+3);

// Draw inner black rectangle with a 1px margin on all sides
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1f));
//                g2.drawRect(
//                        thumbBounds.x + 1,                // move 1px right
//                        thumbBounds.y + 1,                // little top margin (optional)
//                        thumbBounds.width - 3,            // shrink width so right side isn't clipped
//                        thumbBounds.height - 3            // shrink height to match style
//                );
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize(JComponent c) {
                return new Dimension( super.getPreferredSize(c).width,12);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });

        scrollPane.setCorner(
                JScrollPane.LOWER_RIGHT_CORNER,
                new JPanel() {{
                    setBackground(UniversalThemes.BG_PANEL); // match your UI
                }}
        );

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    }

    ///==============================================================================================================
    ///== CheckBox Theme
    ///==============================================================================================================
    public static void applyCheckBoxTheme(JCheckBox checkBox) {

        boolean[] isHovered = {false};

        checkBox.setUI(new javax.swing.plaf.basic.BasicCheckBoxUI() {
            @Override
            public synchronized void paint(Graphics g, JComponent c) {
                JCheckBox cb = (JCheckBox) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int boxSize = 12;
                int x = (c.getWidth() - boxSize) / 2;
                x=x-1;
                int y = (c.getHeight() - boxSize) / 2;

                // Draw background box
                g2.setColor(cb.isEnabled() ? BORDER_COLOR1 : new Color(0x1A1A1E));
                g2.fillRect(x, y, boxSize, boxSize);

                // Draw border
                g2.setColor(cb.isEnabled() ? BORDER_COLOR1 : new Color(0x3A3A40));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRect(x, y, boxSize, boxSize);

                // Draw checkmark if selected
                if (cb.isSelected()) {
                    // Fill with orange
                    g2.setColor(cb.isEnabled() ? ACCENT_COLOR : DISABLED_TEXT);
                    g2.fillRect(x, y, boxSize, boxSize);
                    g2.setColor(cb.isEnabled() ? BORDER_COLOR1 : new Color(0x3A3A40));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRect(x, y, boxSize, boxSize);


                    // Draw checkmark (white tick)
                    g2.setColor(cb.isEnabled() ? TXT_SELECTED : new Color(0x9A9A9A));
                    g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 3, y + 6,  x + 4,  y + 9);
                    g2.drawLine(x + 4, y + 9, x + 10, y + 3);
                }

                // Hover highlight overlay
                if (isHovered[0]) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                    g2.setColor(cb.isEnabled() ? Color.WHITE : ACCENT_COLOR);
                    g2.fillRect(x, y, boxSize, boxSize);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }

                // Draw label text
                FontMetrics fm = g2.getFontMetrics(cb.getFont());
                g2.setFont(cb.getFont());
                g2.setColor(cb.isEnabled() ? TXT_PRIMARY : DISABLED_TEXT);
                String text = cb.getText();
                if (text != null && !text.isEmpty()) {
                    int textX = x + boxSize + 6;
                    int textY = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(text, textX, textY);
                }

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize(JComponent c) {
                JCheckBox cb = (JCheckBox) c;
                FontMetrics fm = cb.getFontMetrics(cb.getFont());
                int textWidth = (cb.getText() != null) ? fm.stringWidth(cb.getText()) : 0;
                return new Dimension(textWidth + 28, 20);
            }
        });

        // Remove default focus painting
        checkBox.setFocusPainted(false);
        checkBox.setFocusable(false);
        checkBox.setOpaque(true);
        checkBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        checkBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered[0] = true;
                checkBox.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered[0] = false;
                checkBox.repaint();
            }
        });

        // Repaint on enabled state change
        checkBox.addPropertyChangeListener("enabled", evt -> checkBox.repaint());

        checkBox.setForeground(TXT_PRIMARY);
        checkBox.setFont(FONT_R_18);
    }

    ///==============================================================================================================
    ///== Selection Theme
    ///==============================================================================================================
    public static final Color SELECTION_COLOR = new Color(255, 255, 255, 40); // translucent grey

    public static void applySelectionTheme(javax.swing.text.JTextComponent comp) {
        applySelectionTheme(comp, TXT_PRIMARY);
    }

    public static void applySelectionTheme(javax.swing.text.JTextComponent comp, Color selectedFg) {
        comp.setSelectionColor(SELECTION_COLOR);
        comp.setSelectedTextColor(selectedFg);
    }

    ///==============================================================================================================
    ///== Selection Collapse Navigation (arrow keys collapse to selection edge instead of default caret step)
    ///==============================================================================================================
    public static void applyCollapseSelectionNavigation(javax.swing.text.JTextComponent comp) {
        InputMap im = comp.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = comp.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "collapseToStartOrStepBack");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "collapseToEndOrStepForward");

        am.put("collapseToStartOrStepBack", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int selStart = comp.getSelectionStart();
                int selEnd = comp.getSelectionEnd();
                if (selStart != selEnd) {
                    comp.setCaretPosition(selStart);
                } else {
                    int pos = Math.max(0, comp.getCaretPosition() - 1);
                    comp.setCaretPosition(pos);
                }
            }
        });

        am.put("collapseToEndOrStepForward", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int selStart = comp.getSelectionStart();
                int selEnd = comp.getSelectionEnd();
                if (selStart != selEnd) {
                    comp.setCaretPosition(selEnd);
                } else {
                    int pos = Math.min(comp.getDocument().getLength(), comp.getCaretPosition() + 1);
                    comp.setCaretPosition(pos);
                }
            }
        });
    }

    ///==============================================================================================================
    ///== Free Up Ctrl+Tab (text components claim it as focus-traversal by default)
    ///==============================================================================================================
    public static void freeCtrlTabFromTraversal(JComponent comp) {
        Set<AWTKeyStroke> forwardKeys = new HashSet<>(comp.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.remove(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_DOWN_MASK));
        comp.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);

        Set<AWTKeyStroke> backwardKeys = new HashSet<>(comp.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backwardKeys.remove(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        comp.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
    }

    ///==============================================================================================================
    ///== Label Wrapping Helpers
    ///==============================================================================================================
    private static int computeWrapWidth(Font font, String text, int minWidth, int maxWidth) {
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
        int widest = 0;
        for (String line : text.split("\n")) {
            widest = Math.max(widest, fm.stringWidth(line));
        }
        return Math.max(minWidth, Math.min(widest + 4, maxWidth));
    }

    private static JLabel createWrappingLabel(String text, Font font, Color color, int minWidth, int maxWidth) {
        int width = computeWrapWidth(font, text, minWidth, maxWidth);
        JLabel label = new JLabel("<html><div style='width:" + width + "px'>" + text.replace("\n", "<br>") + "</div></html>");
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }



    ///==============================================================================================================
    ///== Popups & Confirm Dialogs
    ///==============================================================================================================

    public static void showPopup(Component parent, String message, String title) {
        RoundedDialog rd = createRoundedDialogShell(parent, title);

        JLabel messageLabel = createWrappingLabel(message, FONT_R_16, TXT_PRIMARY, 200, 340);
        rd.body.add(messageLabel);
        rd.body.add(Box.createVerticalStrut(18));

        JButton okButton = createRoundedDialogButton("OK", ACCENT_COLOR, TXT_SELECTED, ACCENT_COLOR_DARK);

        okButton.addActionListener(e -> rd.close());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRow.add(okButton);
        wireDialogButtonNavigation(okButton);
        rd.body.add(buttonRow);

        rd.show();
        SwingUtilities.invokeLater(okButton::requestFocusInWindow);
    }

    // No more OS modal blocking to fake a synchronous return -- callers get
    // the answer via onResult instead of a boolean return value.
    public static void showConfirmPopup(Component parent, String message, String title, Consumer<Boolean> onResult) {
        RoundedDialog rd = createRoundedDialogShell(parent, title);

        JLabel messageLabel = createWrappingLabel(message, FONT_R_18, TXT_PRIMARY, 200, 340);
        rd.body.add(messageLabel);
        rd.body.add(Box.createVerticalStrut(18));

        JButton yesButton = createRoundedDialogButton("Yes", ACCENT_COLOR, TXT_SELECTED, ACCENT_COLOR_DARK);
        JButton noButton  = createRoundedDialogButton("No", BG_COMPONENT, TXT_PRIMARY, BORDER_COLOR1);
        yesButton.addActionListener(e -> { rd.close(); onResult.accept(true); });
        noButton.addActionListener(e -> { rd.close(); onResult.accept(false); });
        rd.setOnCloseRequest(() -> { rd.close(); onResult.accept(false); });

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRow.add(yesButton);
        buttonRow.add(noButton);
        wireDialogButtonNavigation(yesButton, noButton);
        rd.body.add(buttonRow);

        rd.show();
        SwingUtilities.invokeLater(noButton::requestFocusInWindow);
    }

    public static void showDeleteConfirmPopup(Component parent, String dialogTitle, String targetName, String subMessage, Consumer<Boolean> onResult) {
        showDeleteConfirmPopup(parent, dialogTitle, targetName, subMessage, "Delete", onResult);
    }

    public static void showDeleteConfirmPopup(Component parent, String dialogTitle, String targetName, String subMessage, String actionVerb, Consumer<Boolean> onResult) {
        RoundedDialog rd = createRoundedDialogShell(parent, dialogTitle);

        JLabel messageLabel = createWrappingLabel(
                "Are you sure you want to " + actionVerb.toLowerCase() + " \u201C" + targetName + "\u201D?",
                FONT_R_16, TXT_PRIMARY, 220, 340
        );
        rd.body.add(messageLabel);

        if (subMessage != null && !subMessage.isEmpty()) {
            rd.body.add(Box.createVerticalStrut(10));
            JLabel subLabel = createWrappingLabel(subMessage, FONT_R_16, TXT_SECONDARY, 220, 340);
            rd.body.add(subLabel);
        }

        rd.body.add(Box.createVerticalStrut(18));

        JButton actionButton = createRoundedDialogButton(actionVerb, BG_DELETE_BTN, Color.BLACK, BG_DELETE_BTN.darker());
        JButton cancelButton = createRoundedDialogButton("Cancel", BG_CANCEL_BTN, TXT_PRIMARY, BORDER_COLOR1);
        actionButton.addActionListener(e -> { rd.close(); onResult.accept(true); });
        cancelButton.addActionListener(e -> { rd.close(); onResult.accept(false); });
        rd.setOnCloseRequest(() -> { rd.close(); onResult.accept(false); });

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRow.add(actionButton);
        buttonRow.add(cancelButton);
        wireDialogButtonNavigation(actionButton, cancelButton);
        rd.body.add(buttonRow);

        rd.show();
        SwingUtilities.invokeLater(actionButton::requestFocusInWindow);
    }

    ///==============================================================================================================
    ///== Menu Theming
    ///==============================================================================================================
    private static boolean menuUiManagerApplied = false;

    public static void applyMenuTheme(JMenu menu) {
        if (!menuUiManagerApplied) {
            UIManager.put("PopupMenu.border", BorderFactory.createEmptyBorder());
            UIManager.put("PopupMenu.background", BG_COMPONENT);
            UIManager.put("Menu.selectionBackground", BG_SIDEBAR);   // ← kills the dark box
            UIManager.put("Menu.selectionForeground", ACCENT_COLOR); // ← text color when open
            menuUiManagerApplied = true;
        }

        menu.setUI(new javax.swing.plaf.basic.BasicMenuUI() {
            @Override
            protected void paintBackground(Graphics g, JMenuItem item, Color bgColor) {
                // always keep sidebar color, no highlight box on the label
                g.setColor(BG_SIDEBAR);
                g.fillRect(0, 0, item.getWidth(), item.getHeight());
            }

            @Override
            protected void paintText(Graphics g, JMenuItem item,
                                     Rectangle textRect, String text) {
                ButtonModel model = item.getModel();
                g.setColor((model.isSelected() || model.isArmed())
                        ? ACCENT_COLOR
                        : TXT_PRIMARY);
                super.paintText(g, item, textRect, text);
            }
        });
    }

    public static void applyMenuItemTheme(JMenuItem item) {
        item.setUI(new javax.swing.plaf.basic.BasicMenuItemUI() {
            @Override
            protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                ButtonModel model = menuItem.getModel();
                Color bg = model.isArmed() ? ACCENT_COLOR : BG_COMPONENT;
                g.setColor(bg);
                g.fillRect(0, 0, menuItem.getWidth(), menuItem.getHeight());
            }

            @Override
            protected void paintText(Graphics g, JMenuItem menuItem,
                                     Rectangle textRect, String text) {
                ButtonModel model = menuItem.getModel();
                g.setColor(model.isArmed() ? BG_MAIN : TXT_PRIMARY);
                super.paintText(g, menuItem, textRect, text);
            }
        });
    }

    ///==============================================================================================================
    ///== Misc
    ///==============================================================================================================
    public static void flashBorder(JComponent component, Color flashColor, Color normalColor, int borderWidth) {
        final int[] count = {0};
        Timer timer = new Timer(100, null);
        timer.addActionListener(e -> {
            if (count[0] % 2 == 0) {
                component.setBorder(BorderFactory.createMatteBorder(borderWidth, borderWidth, borderWidth, borderWidth, flashColor));
            } else {
                component.setBorder(BorderFactory.createMatteBorder(borderWidth, borderWidth, borderWidth, borderWidth, normalColor));
            }
            count[0]++;
            if (count[0] >= 6) { // 3 flashes
                ((Timer) e.getSource()).stop();
                component.setBorder(BorderFactory.createMatteBorder(borderWidth, borderWidth, borderWidth, borderWidth, normalColor));
            }
        });
        timer.start();
    }



}