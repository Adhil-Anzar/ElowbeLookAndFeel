package com.elowbe.laf.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

import com.elowbe.laf.util.ElowbeInputs;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeTextFieldUI extends BasicTextFieldUI {

    public static ComponentUI createUI(JComponent component) {
        return new ElowbeTextFieldUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        getComponent().setOpaque(false);
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        ElowbeInputs.installFocusRepaint(getComponent());
    }

    @Override
    protected void uninstallListeners() {
        ElowbeInputs.uninstallFocusRepaint(getComponent());
        super.uninstallListeners();
    }

    @Override
    protected void paintSafely(Graphics graphics) {
        JTextComponent text = getComponent();

        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            paintChrome(g2, text);
        } finally {
            g2.dispose();
        }

        super.paintSafely(graphics);
        paintPlaceholder(graphics, text);
    }

    /**
     * Paints the rounded input background and border. Text fields hosted by a
     * {@link JSpinner} are painted by the spinner shell instead.
     */
    protected void paintChrome(Graphics2D g2, JTextComponent text) {
        if (SwingUtilities.getAncestorOfClass(JSpinner.class, text) != null) {
            return;
        }
        ElowbeInputs.paintChrome(g2, text);
    }

    protected void paintPlaceholder(Graphics graphics, JTextComponent text) {
        ElowbeInputs.paintPlaceholder(graphics, text);
    }
}
