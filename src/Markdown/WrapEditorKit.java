package Markdown;

import utilities.UniversalThemes;

import javax.swing.text.*;
import java.awt.*;

/**
 * StyledEditorKit used by every JTextPane that shows rendered Markdown (entries AND the Help tables).
 * Adds: mid-word wrapping for long tokens, and the rounded inline-code chip background.
 * Moved out of ArkIV so other packages can use it.
 */
public class WrapEditorKit extends StyledEditorKit {

    private final ViewFactory factory = new WrapColumnFactory();

    @Override
    public ViewFactory getViewFactory() {
        return factory;
    }

    private static class WrapColumnFactory implements ViewFactory {
        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                switch (kind) {
                    case AbstractDocument.ContentElementName:
                        return new WrapLabelView(elem);
                    case AbstractDocument.ParagraphElementName:
                        return new ParagraphView(elem);
                    case AbstractDocument.SectionElementName:
                        return new BoxView(elem, View.Y_AXIS);
                    case StyleConstants.ComponentElementName:
                        return new ComponentView(elem);
                    case StyleConstants.IconElementName:
                        return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }
    }

    private static class WrapLabelView extends LabelView {

        // Used to curve the BG around inline codes in MD
        private final int curveFactor = 10;

        WrapLabelView(Element elem) { super(elem); }

        @Override
        public void paint(Graphics g, Shape a) {
            if (getAttributes().containsAttribute(MarkdownStyles.INLINE_CODE, Boolean.TRUE)) {
                Rectangle r = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UniversalThemes.MD_COLOR_CODE_BG);
                g2.fillRoundRect(r.x, r.y + 1, r.width, r.height - 2, curveFactor, curveFactor);
                g2.dispose();
            }
            super.paint(g, a); // text on top
        }

        @Override
        public float getMinimumSpan(int axis) {
            switch (axis) {
                case View.X_AXIS:
                    return 0;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalStateException("Invalid axis: " + axis);
            }
        }
    }
}