package com.elowbe.laf.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;

/**
 * Shared painting and focus helpers for text-like input components.
 *
 * <p>Elowbe paints the input chrome itself instead of relying on a border, which
 * keeps the focus ring, disabled state and theme colors consistent across text
 * fields, password fields, text areas, combo boxes and spinners.</p>
 */
public final class ElowbeInputs {
    private static final FocusListener REPAINT_ON_FOCUS = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent event) {
            event.getComponent().repaint();
        }

        @Override
        public void focusLost(FocusEvent event) {
            event.getComponent().repaint();
        }
    };

    private ElowbeInputs() {
    }

    /**
     * A stateless listener that repaints its component when focus changes.
     * The same instance can safely be shared by any number of components.
     */
    public static FocusListener repaintOnFocus() {
        return REPAINT_ON_FOCUS;
    }

    public static void installFocusRepaint(JComponent component) {
        component.addFocusListener(REPAINT_ON_FOCUS);
    }

    public static void uninstallFocusRepaint(JComponent component) {
        component.removeFocusListener(REPAINT_ON_FOCUS);
    }

    /**
     * Border color for an input. Focused inputs use the foreground color so the
     * state stays visible in both light and dark themes.
     */
    public static Color borderColor(JComponent component) {
        ElowbePalette palette = PaintUtils.palette();
        return component.hasFocus() ? palette.foreground : palette.input;
    }

    public static Color backgroundColor(JComponent component) {
        ElowbePalette palette = PaintUtils.palette();
        return component.isEnabled() ? palette.card : palette.disabled;
    }

    public static void paintBackground(Graphics2D g2, JComponent component) {
        paintBackground(g2, component, 0, 0, component.getWidth(), component.getHeight());
    }

    public static void paintBackground(Graphics2D g2, JComponent component, int x, int y, int width, int height) {
        int radius = PaintUtils.radius(component, ElowbeDefaults.RADIUS_MD);
        PaintUtils.fillRound(g2, x, y, width, height, radius, backgroundColor(component));
    }

    public static void paintBorder(Graphics2D g2, JComponent component) {
        paintBorder(g2, component, 0, 0, component.getWidth(), component.getHeight());
    }

    public static void paintBorder(Graphics2D g2, JComponent component, int x, int y, int width, int height) {
        int radius = PaintUtils.radius(component, ElowbeDefaults.RADIUS_MD);
        PaintUtils.drawRound(g2, x, y, width, height, radius, borderColor(component));
    }

    public static void paintChrome(Graphics2D g2, JComponent component) {
        paintChrome(g2, component, 0, 0, component.getWidth(), component.getHeight());
    }

    public static void paintChrome(Graphics2D g2, JComponent component, int x, int y, int width, int height) {
        paintBackground(g2, component, x, y, width, height);
        paintBorder(g2, component, x, y, width, height);
    }

    /**
     * Paints the placeholder stored under {@link ElowbeDefaults#PLACEHOLDER_KEY}
     * when the component is empty.
     */
    public static void paintPlaceholder(Graphics graphics, JTextComponent text) {
        Object placeholder = text.getClientProperty(ElowbeDefaults.PLACEHOLDER_KEY);
        if (placeholder == null || text.getDocument().getLength() > 0) {
            return;
        }

        Font font = text.getFont();
        if (font == null) {
            font = UIManager.getFont("Label.font");
        }
        if (font == null) {
            font = graphics.getFont();
        }

        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            g2.setColor(PaintUtils.palette().mutedForeground);
            g2.setFont(font);

            FontMetrics metrics = text.getFontMetrics(font);
            Insets insets = text.getInsets();
            Rectangle visible = text.getVisibleRect();

            int baseline = visible.y + insets.top + metrics.getAscent();
            if (text instanceof JTextField) {
                baseline = (text.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            }

            g2.drawString(String.valueOf(placeholder), insets.left, baseline);
        } finally {
            g2.dispose();
        }
    }
}
