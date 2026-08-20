package Registers;

import utilities.UniversalThemes;

import javax.swing.*;
import javax.swing.plaf.basic.BasicMenuItemUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

public class RegisterContextMenu {

    private static final int CORNER_RADIUS = 12;

    public interface Handler {
        void onRename();
        void onMoveUp();
        void onMoveDown();
        void onSetDefault();
        void onCopyPath();
        void onOpenInExplorer();
        void onDelete();
    }

    public static void showForBranch(Component invoker, int x, int y, Runnable onNewRegister) {
        JPopupMenu menu = createRoundedPopup();
        menu.add(buildItem("New Register", true, e -> onNewRegister.run(), UniversalThemes.TXT_PRIMARY));
        menu.show(invoker, x, y);
    }

    public static void show(Component invoker, int x, int y,
                            boolean isFirst, boolean isLast, boolean isDefault, boolean canDelete,
                            Handler handler) {

        JPopupMenu menu = createRoundedPopup();


        menu.add(buildItem("Move Up", !isFirst, e -> handler.onMoveUp(), UniversalThemes.TXT_PRIMARY));
        menu.add(buildItem("Move Down", !isLast, e -> handler.onMoveDown(), UniversalThemes.TXT_PRIMARY));
        menu.add(buildSeparator());


        menu.add(buildItem("Copy Path", true, e -> handler.onCopyPath(), UniversalThemes.TXT_PRIMARY));
        menu.add(buildItem("Show in System Explorer", true, e -> handler.onOpenInExplorer(), UniversalThemes.TXT_PRIMARY));

        menu.add(buildSeparator());


        menu.add(buildItem(isDefault ? "Default" : "Make Default", !isDefault, e -> handler.onSetDefault(), UniversalThemes.TXT_PRIMARY));

        menu.add(buildSeparator());
        menu.add(buildItem("Rename", true, e -> handler.onRename(), UniversalThemes.TXT_PRIMARY));
        menu.add(buildItem("Remove", canDelete, e -> handler.onDelete(), UniversalThemes.BG_DELETE_BTN));

        menu.show(invoker, x, y);
    }

    public interface UnrecognizedHandler {
        void onRecognize();
        void onCopyPath();
        void onOpenInExplorer();
        void onDelete();
    }

    public static void showForUnrecognized(Component invoker, int x, int y, UnrecognizedHandler handler) {
        JPopupMenu menu = createRoundedPopup();

        menu.add(buildItem("Recognize", true, e -> handler.onRecognize(), UniversalThemes.TXT_PRIMARY));
        menu.add(buildSeparator());
        menu.add(buildItem("Copy Path", true, e -> handler.onCopyPath(), UniversalThemes.TXT_PRIMARY));
        menu.add(buildItem("Open in System Explorer", true, e -> handler.onOpenInExplorer(), UniversalThemes.TXT_PRIMARY));
        menu.add(buildSeparator());
        menu.add(buildItem("Delete", true, e -> handler.onDelete(), UniversalThemes.BG_DELETE_BTN));

        menu.show(invoker, x, y);
    }

    // ── Rounded popup shell ─────────────────────────────────────────
    private static JPopupMenu createRoundedPopup() {
        JPopupMenu menu = new JPopupMenu() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(UniversalThemes.BG_COMPONENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);

                float strokeWidth = 1.2f;
                g2.setColor(UniversalThemes.BORDER_COLOR2);
                g2.setStroke(new BasicStroke(strokeWidth));
                float inset = strokeWidth / 2f;
                g2.draw(new RoundRectangle2D.Float(
                        inset, inset,
                        getWidth() - strokeWidth, getHeight() - strokeWidth,
                        CORNER_RADIUS, CORNER_RADIUS
                ));

                g2.dispose();
            }
        };
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        return menu;
    }

    private static JSeparator buildSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UniversalThemes.BORDER_COLOR2);
        sep.setBackground(UniversalThemes.BG_COMPONENT);
        sep.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return sep;
    }

    private static JMenuItem buildItem(String label, boolean enabled, ActionListener action, Color activeTextColor) {
        JMenuItem item = new JMenuItem(label);
        item.setFont(UniversalThemes.FONT_R_16);
        item.setEnabled(enabled);
        item.setBackground(UniversalThemes.BG_COMPONENT);
        item.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 7));
        item.setOpaque(false);
        item.addActionListener(action);

        Color idleColor = (label.equals("Delete") || label.equals("Remove")) ? UniversalThemes.BG_DELETE_BTN : UniversalThemes.TXT_PRIMARY;
        applyMenuItemTheme(item, activeTextColor, idleColor);
        return item;
    }

    private static void applyMenuItemTheme(JMenuItem item, Color activeTextColor, Color idleTextColor) {
        item.setUI(new BasicMenuItemUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                JMenuItem menuItem = (JMenuItem) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                @SuppressWarnings("unchecked")
                Map<RenderingHints.Key, Object> desktopHints =
                        (Map<RenderingHints.Key, Object>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
                if (desktopHints != null) {
                    g2.addRenderingHints(desktopHints);
                } else {
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }

                boolean hovered = menuItem.getModel().isArmed() && menuItem.isEnabled();

                // 1. Background fill first
                if (hovered) {
                    g2.setColor(UniversalThemes.BG_HOVERED);
                    g2.fillRoundRect(4, 0, menuItem.getWidth() - 8, menuItem.getHeight(), 6, 6);
                }

                // 2. Text drawn explicitly afterward, guaranteed on top
                Color textColor = !menuItem.isEnabled() ? UniversalThemes.DISABLED_TEXT
                        : hovered ? activeTextColor : idleTextColor;
                g2.setColor(textColor);
                g2.setFont(menuItem.getFont());

                FontMetrics fm = g2.getFontMetrics();
                Insets insets = menuItem.getInsets();
                int textX = insets.left;
                int textY = (menuItem.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(menuItem.getText(), textX, textY);

                g2.dispose();
            }
        });
    }


}