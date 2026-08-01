package utilities;

import javax.swing.*;
import java.awt.*;

public class UniversalFactory {

    public static JMenu createMenuBar(String label) {
        JMenu menu = new JMenu(label);
        menu.setFont(UniversalThemes.FONT_R_14);
        menu.setForeground(UniversalThemes.TXT_PRIMARY);
        menu.setBackground(UniversalThemes.BG_SIDEBAR);
        menu.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        UniversalThemes.applyMenuTheme(menu);
        return menu;
    }

    public static JMenuItem createMenuBarItem(String label) {
        JMenuItem item = new JMenuItem(label);
        item.setFont(UniversalThemes.FONT_R_14);
        item.setForeground(UniversalThemes.TXT_PRIMARY);
        item.setBackground(UniversalThemes.BG_PANEL);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 1, 1, UniversalThemes.BORDER_COLOR1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        UniversalThemes.applyMenuItemTheme(item);
        return item;
    }

    public static JTextField createTextField(int x, int y, int width, int height, Color bgColor, Color fgColor, Color caretColor)
    {
        JTextField textfield = new JTextField();
        textfield.setBounds(x, y, width, height);
        textfield.setBackground(bgColor);
        textfield.setForeground(fgColor);
        textfield.setCaretColor(caretColor);
        return textfield;
    }

}
