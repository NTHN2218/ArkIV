package utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

/** Middle-click autoscroll: click, move mouse up/down from the anchor, click/Esc/wheel to stop. */
public class AutoScroller {

    private static final int DEAD_ZONE = 12;      // px around anchor with no scrolling
    private static final double SPEED = 0.35;     // scroll px per tick, per px beyond dead zone
    private static final int TICK_MS = 16;
    private static final int ICON_SIZE = 32;

    private final JScrollPane pane;
    private final Timer timer;
    private final BufferedImage icon = createIcon(ICON_SIZE);
    private Cursor autoScrollCursor;
    private JComponent overlay;
    private Point anchorScreen;
    private double remainder;
    private boolean active;

    public static void attach(JScrollPane pane) {
        new AutoScroller(pane);
    }

    private AutoScroller(JScrollPane pane) {
        this.pane = pane;
        this.timer = new Timer(TICK_MS, e -> tick());
        Toolkit.getDefaultToolkit().addAWTEventListener(this::onEvent,
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }

    private void onEvent(AWTEvent ev) {
        if (ev instanceof KeyEvent k) {
            if (active && k.getID() == KeyEvent.KEY_PRESSED && k.getKeyCode() == KeyEvent.VK_ESCAPE) stop();
            return;
        }
        if (!(ev instanceof MouseEvent m)) return;
        if (!(m.getSource() instanceof Component c)) return;
        // Viewport only -- excludes the scrollbar itself
        if (!SwingUtilities.isDescendingFrom(c, pane.getViewport())) return;

        if (active) {
            if (m instanceof MouseWheelEvent || m.getID() == MouseEvent.MOUSE_PRESSED) stop();
        } else if (m.getID() == MouseEvent.MOUSE_PRESSED && SwingUtilities.isMiddleMouseButton(m)) {
            start();
        }
    }

    private void start() {
        JRootPane root = SwingUtilities.getRootPane(pane);
        if (root == null) return;

        anchorScreen = MouseInfo.getPointerInfo().getLocation();
        remainder = 0;
        active = true;

        if (autoScrollCursor == null) {
            autoScrollCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    icon, new Point(ICON_SIZE / 2, ICON_SIZE / 2), "autoscroll");
        }

        // Transparent full-window overlay: no mouse listeners, so clicks fall through to
        // whatever is underneath, but Swing still uses its cursor for the whole window.
        JLayeredPane layered = root.getLayeredPane();
        overlay = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Point p = new Point(anchorScreen);
                SwingUtilities.convertPointFromScreen(p, this);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2.drawImage(icon, p.x - ICON_SIZE / 2, p.y - ICON_SIZE / 2, null);
                g2.dispose();
            }
        };
        overlay.setOpaque(false);
        overlay.setCursor(autoScrollCursor);
        overlay.setBounds(0, 0, layered.getWidth(), layered.getHeight());
        layered.add(overlay, JLayeredPane.DRAG_LAYER);
        layered.repaint();

        timer.start();
    }

    private void stop() {
        active = false;
        timer.stop();
        if (overlay != null) {
            Container parent = overlay.getParent();
            if (parent != null) {
                parent.remove(overlay);
                parent.repaint();
            }
            overlay = null;
        }
    }

    private void tick() {
        Window w = SwingUtilities.getWindowAncestor(pane);
        if (w == null || !w.isActive()) { stop(); return; }

        int dy = MouseInfo.getPointerInfo().getLocation().y - anchorScreen.y;
        int beyond = Math.abs(dy) - DEAD_ZONE;
        if (beyond <= 0) return;

        double delta = Math.signum(dy) * beyond * SPEED + remainder;
        int step = (int) delta;
        remainder = delta - step;

        JScrollBar bar = pane.getVerticalScrollBar();
        bar.setValue(bar.getValue() + step);
    }

    /** Circle with up/down arrows and a center dot. */
    private static BufferedImage createIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int c = size / 2;
        int r = size / 2 - 2;

        g.setColor(new Color(UniversalThemes.BG_PANEL.getRed(), UniversalThemes.BG_PANEL.getGreen(),
                UniversalThemes.BG_PANEL.getBlue(), 235));
        g.fillOval(c - r, c - r, r * 2, r * 2);

        g.setColor(UniversalThemes.TXT_PRIMARY);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(c - r, c - r, r * 2, r * 2);

        // center dot
        g.fillOval(c - 2, c - 2, 4, 4);

        // up arrow
        g.fillPolygon(new int[]{c - 4, c + 4, c}, new int[]{c - 5, c - 5, c - 10}, 3);
        // down arrow
        g.fillPolygon(new int[]{c - 4, c + 4, c}, new int[]{c + 5, c + 5, c + 10}, 3);

        g.dispose();
        return img;
    }
}