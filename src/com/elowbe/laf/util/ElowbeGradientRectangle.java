package com.elowbe.laf.util;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;

/**
 * A panel that paints a rounded linear gradient background.
 *
 * <p>Colors are optional. When they are not set, the panel uses theme colors,
 * so it automatically follows light and dark theme changes. The default
 * gradient is subtle: {@code accent} to {@code background}.</p>
 */
public class ElowbeGradientRectangle extends JPanel {

    /**
     * Direction of the gradient.
     */
    public enum Direction {
        VERTICAL,
        HORIZONTAL
    }

    private Color topColor;
    private Color bottomColor;
    private int radius = ElowbeDefaults.RADIUS_LG;
    private Direction direction = Direction.VERTICAL;

    public ElowbeGradientRectangle() {
        setOpaque(false);
    }

    public ElowbeGradientRectangle(Color topColor, Color bottomColor) {
        this();
        this.topColor = topColor;
        this.bottomColor = bottomColor;
    }

    public ElowbeGradientRectangle(Color topColor, Color bottomColor, int radius) {
        this(topColor, bottomColor);
        this.radius = Math.max(0, radius);
    }

    /**
     * Sets both gradient colors. Either color may be {@code null} to fall back
     * to the current theme color.
     */
    public void setColors(Color topColor, Color bottomColor) {
        this.topColor = topColor;
        this.bottomColor = bottomColor;
        repaint();
    }

    public Color getTopColor() {
        return topColor;
    }

    /**
     * Sets the start color, or {@code null} to use the theme accent color.
     */
    public void setTopColor(Color color) {
        topColor = color;
        repaint();
    }

    public Color getBottomColor() {
        return bottomColor;
    }

    /**
     * Sets the end color, or {@code null} to use the theme background color.
     */
    public void setBottomColor(Color color) {
        bottomColor = color;
        repaint();
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = Math.max(0, radius);
        repaint();
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction == null ? Direction.VERTICAL : direction;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        ElowbePalette palette = PaintUtils.palette();
        Color start = topColor != null ? topColor : palette.accent;
        Color end = bottomColor != null ? bottomColor : palette.background;

        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            GradientPaint paint = direction == Direction.HORIZONTAL
                    ? new GradientPaint(0, 0, start, getWidth(), 0, end)
                    : new GradientPaint(0, 0, start, 0, getHeight(), end);
            g2.setPaint(paint);
            g2.fill(PaintUtils.roundedRect(0, 0, getWidth(), getHeight(), radius));
        } finally {
            g2.dispose();
        }
    }
}
