package Registers;

import utilities.UniversalThemes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

public class RegisterItem extends JPanel {

    private final RegisterManager.RegisterEntry entry; // null for unrecognized rows
    private final JLabel nameLabel;
    private boolean isSelected;
    private final boolean isUnrecognized;

    // Recognized register row
    public RegisterItem(RegisterManager.RegisterEntry entry, Runnable onLeftClick,
                        BiConsumer<RegisterItem, MouseEvent> onRightClick) {
        this.entry = entry;
        this.isUnrecognized = false;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        setOpaque(true);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        nameLabel = new JLabel(entry.name);
        nameLabel.setFont(UniversalThemes.FONT_R_18);
        add(nameLabel, BorderLayout.WEST);

        setSelected(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    onLeftClick.run();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) { maybeShowPopup(e); }

            @Override
            public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    onRightClick.accept(RegisterItem.this, e);
                }
            }
        });
    }

    // Unrecognized register row (read-only, no left-click switching yet)
    public RegisterItem(String displayName, Runnable onLeftClickBlocked,
                        BiConsumer<RegisterItem, MouseEvent> onRightClick) {
        this.entry = null;
        this.isUnrecognized = true;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        setOpaque(true);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBackground(UniversalThemes.BG_COMPONENT);

        nameLabel = new JLabel(displayName);
        nameLabel.setFont(UniversalThemes.FONT_R_18);
        nameLabel.setForeground(UniversalThemes.DISABLED_TEXT);
        add(nameLabel, BorderLayout.WEST);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    onLeftClickBlocked.run();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) { maybeShowPopup(e); }

            @Override
            public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    onRightClick.accept(RegisterItem.this, e);
                }
            }
        });
    }

    public RegisterManager.RegisterEntry getEntry() {
        return entry;
    }

    public void setSelected(boolean selected) {
        if (isUnrecognized) return; // unrecognized rows never show selected styling
        this.isSelected = selected;
        if (selected) {
            setBackground(UniversalThemes.ACCENT_COLOR);
            nameLabel.setForeground(UniversalThemes.TXT_SELECTED);
        } else {
            setBackground(UniversalThemes.BORDER_COLOR1);
            nameLabel.setForeground(UniversalThemes.TXT_PRIMARY);
        }
        repaint();
    }

    public boolean isSelected() {
        return isSelected;
    }
}