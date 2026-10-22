package com.elowbe.laf.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPasswordFieldUI;
import javax.swing.text.JTextComponent;

import com.elowbe.laf.util.ElowbeInputs;
import com.elowbe.laf.util.PaintUtils;

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

    protected void paintChrome(Graphics2D g2, JTextComponent text) {
        ElowbeInputs.paintChrome(g2, text);
    }

    protected void paintPlaceholder(Graphics graphics, JTextComponent text) {
        ElowbeInputs.paintPlaceholder(graphics, text);
    }
}
