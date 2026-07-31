package com.elowbe.laf.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.ElowbeBorder;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeTableUI extends BasicTableUI {

    public static ComponentUI createUI(javax.swing.JComponent c) {
        return new ElowbeTableUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();

        ElowbePalette palette = PaintUtils.palette();

        table.setOpaque(false);
        table.setBackground(palette.card);
        table.setForeground(palette.foreground);

        table.setSelectionBackground(palette.primary);
        table.setSelectionForeground(palette.selectionForeground);

        table.setGridColor(palette.border);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));

        table.setRowHeight(34);
        table.setRowMargin(0);
        table.setFont(UIManager.getFont("Table.font"));
        table.setBorder(new EmptyBorder(12,0,0,0));

        TableCellRenderer renderer = new CellRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Number.class, renderer);
        table.setDefaultRenderer(Boolean.class, renderer);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (e.getButton() == MouseEvent.BUTTON3) {
                    table.clearSelection();
                }
            }
        });
    }

    @Override
    public void paint(Graphics g, javax.swing.JComponent c) {
        Graphics2D g2 = PaintUtils.prepare(g);
        try {
            g2.setColor(PaintUtils.palette().background);
            g2.fillRect(0, 0, c.getWidth(), c.getHeight());
        } finally {
            g2.dispose();
        }

        super.paint(g, c);
    }

    public static class CellRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        public CellRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setOpaque(true);
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            super.getTableCellRendererComponent(
                    table,
                    value,
                    isSelected,
                    hasFocus,
                    row,
                    column);

            ElowbePalette palette = PaintUtils.palette();
            if (isSelected) {
                setBackground(palette.primary);
                setForeground(palette.selectionForeground);
            } else {
                setBackground((row & 1) == 0
                        ? palette.card
                        : PaintUtils.mix(palette.card, palette.primary, 0.12f));
                setForeground(palette.foreground);
            }
            setBorder(new ElowbeBorder(getBackground(), 0, new Insets(0,10,0,10)));
            Font font = UIManager.getFont("Table.font");
            if (font != null) {
                setFont(font);
            }

            return this;
        }
    }

}