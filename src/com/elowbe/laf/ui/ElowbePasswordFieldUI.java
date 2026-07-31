package com.elowbe.laf.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPasswordFieldUI;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;
import javax.swing.text.JTextComponent;

public class ElowbePasswordFieldUI extends BasicPasswordFieldUI {

    public static ComponentUI createUI(JComponent component) {
        return new ElowbePasswordFieldUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        getComponent().setOpaque(false);
    }

    @Override
    protected void paintSafely(Graphics graphics) {
        JTextComponent text = getComponent();
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            //ElowbeTextFieldUI painter = new ElowbeTextFieldUI();
            paintChrome(g2, text);
        } finally {
            g2.dispose();
        }
        super.paintSafely(graphics);
        paintPlaceholder(graphics, text);
    }
    protected void paintChrome(Graphics2D g2, JTextComponent text) {
        ElowbePalette palette = PaintUtils.palette();
        int radius = PaintUtils.radius(text, ElowbeDefaults.RADIUS_MD);
        Color fill = text.isEnabled() ? palette.card : palette.disabled;
        Color border = palette.input;
        PaintUtils.fillRound(g2, 0, 0, text.getWidth(), text.getHeight(), radius, fill);
        PaintUtils.drawRound(g2, 0, 0, text.getWidth(), text.getHeight(), radius, border);
    }
    protected void paintPlaceholder(Graphics graphics, JTextComponent text) {
        Object placeholder = text.getClientProperty(ElowbeDefaults.PLACEHOLDER_KEY);
        if (placeholder == null || text.getDocument().getLength() > 0) {
            return;
        }
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            g2.setColor(PaintUtils.palette().mutedForeground);
            g2.setFont(text.getFont());
            Insets insets = text.getInsets();
            Rectangle visible = text.getVisibleRect();
            int baseline = visible.y + insets.top + text.getFontMetrics(text.getFont()).getAscent();
            if (text instanceof JTextField) {
                baseline = (text.getHeight() - text.getFontMetrics(text.getFont()).getHeight()) / 2
                        + text.getFontMetrics(text.getFont()).getAscent();
            }
            g2.drawString(placeholder.toString(), insets.left, baseline);
        } finally {
            g2.dispose();
        }
    }
}