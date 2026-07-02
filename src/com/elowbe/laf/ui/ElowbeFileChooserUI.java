package com.elowbe.laf.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.ItemEvent;
import java.io.File;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalFileChooserUI;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.ElowbeBorder;
import com.elowbe.laf.util.ElowbeIcons;
import com.elowbe.laf.util.ElowbeThumbnails;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeFileChooserUI extends MetalFileChooserUI {
    private static final Dimension DEFAULT_SIZE = new Dimension(880, 620);
    private static final int NAME_COLUMN_WIDTH = 560;
    private static final int SIZE_COLUMN_WIDTH = 82;
    private static final int TYPE_COLUMN_WIDTH = 110;
    private static final int MODIFIED_COLUMN_WIDTH = 170;
    private static final int ATTRIBUTES_COLUMN_WIDTH = 96;
    private static final int ROW_HEIGHT = 30;
    private static final int EDITOR_RADIUS = 12;
    private static final String EDITOR_STYLER_KEY = "Elowbe.fileChooser.editorStyler";
    private static final String GALLERY_TOOLTIP = "Gallery";
    private static final String GALLERY_KEY = "Elowbe.fileChooser.galleryMode";
    private static final String ORIGINAL_RENDERER_KEY = "Elowbe.fileChooser.listRenderer";
    private static final String GALLERY_SELECTED_KEY = "Elowbe.fileChooser.gallerySelected";
    private static final int GALLERY_CELL_WIDTH = 172;
    private static final int GALLERY_CELL_HEIGHT = 150;
    private static final int THUMBNAIL_WIDTH = 148;
    private static final int THUMBNAIL_HEIGHT = 96;

    public static ComponentUI createUI(JComponent component) {
        return new ElowbeFileChooserUI((JFileChooser) component);
    }

    private boolean galleryTransition;

    public ElowbeFileChooserUI(JFileChooser fileChooser) {
        super(fileChooser);
    }

    @Override
    public void installComponents(JFileChooser fileChooser) {
        super.installComponents(fileChooser);
        styleFileChooser(fileChooser);
    }

    @Override
    protected void installDefaults(JFileChooser fileChooser) {
        super.installDefaults(fileChooser);
        ElowbePalette palette = PaintUtils.palette();
        fileChooser.setBackground(palette.background);
        fileChooser.setForeground(palette.foreground);
        fileChooser.setBorder(new EmptyBorder(18, 18, 18, 18));
        fileChooser.setPreferredSize(DEFAULT_SIZE);
        fileChooser.setMinimumSize(new Dimension(720, 480));
    }

    private void styleFileChooser(JFileChooser fileChooser) {
        installGalleryButton(fileChooser);
        style(fileChooser);
        styleApproveButton(fileChooser);
        SwingUtilities.invokeLater(() -> {
            showDetailsView(fileChooser);
            style(fileChooser);
            styleApproveButton(fileChooser);
        });
    }

    private void showDetailsView(JFileChooser fileChooser) {
        String detailsToolTip = UIManager.getString("FileChooser.detailsViewButtonToolTipText");
        AbstractButton detailsButton = findButtonByToolTip(fileChooser, detailsToolTip);
        if (detailsButton != null && !detailsButton.isSelected()) {
            detailsButton.doClick(0);
        }
    }

    private AbstractButton findButtonByToolTip(Component component, String toolTip) {
        if (toolTip != null && component instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) component;
            if (toolTip.equals(button.getToolTipText())) {
                return button;
            }
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                AbstractButton button = findButtonByToolTip(child, toolTip);
                if (button != null) {
                    return button;
                }
            }
        }
        return null;
    }

    private void style(Component component) {
        ElowbePalette palette = PaintUtils.palette();
        if (component instanceof JComponent) {
            JComponent jComponent = (JComponent) component;
            jComponent.setFont(UIManager.getFont("FileChooser.font"));
        }

        if (component instanceof JPanel) {
            component.setBackground(palette.background);
        } else if (component instanceof JLabel) {
            component.setForeground(palette.foreground);
            component.setBackground(palette.background);
        } else if (component instanceof JTextField) {
            styleTextField((JTextField) component, palette);
        } else if (component instanceof JComboBox) {
            styleComboBox((JComboBox<?>) component, palette);
        } else if (component instanceof JList) {
            styleList((JList<?>) component, palette);
        } else if (component instanceof JTable) {
            styleTable((JTable) component, palette);
        } else if (component instanceof JTableHeader) {
            styleTableHeader((JTableHeader) component, palette);
        } else if (component instanceof JToolBar) {
            styleToolBar((JToolBar) component, palette);
        } else if (component instanceof AbstractButton) {
            styleButton((AbstractButton) component);
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                style(child);
            }
        }
    }

    private void styleTextField(JTextField field, ElowbePalette palette) {
        Container parent = field.getParent();
        if (parent instanceof JList || parent instanceof JTable) {
            styleInlineEditor(field);
            return;
        }
        field.setBackground(palette.card);
        field.setForeground(palette.foreground);
        field.setCaretColor(palette.foreground);
        field.setSelectionColor(palette.selection);
        field.setSelectedTextColor(palette.selectionForeground);
        field.setBorder(new ElowbeBorder(palette.input, ElowbeDefaults.RADIUS_MD, new Insets(7, 10, 7, 10)));
        field.putClientProperty(ElowbeDefaults.PLACEHOLDER_KEY, "File name");
    }

    private void styleComboBox(JComboBox<?> comboBox, ElowbePalette palette) {
        comboBox.setBackground(palette.card);
        comboBox.setForeground(palette.foreground);
        comboBox.setBorder(new ElowbeBorder(palette.input, ElowbeDefaults.RADIUS_MD, new Insets(6, 10, 6, 34)));
        Dimension preferred = comboBox.getPreferredSize();
        Dimension size = new Dimension(Math.max(preferred.width, 320), Math.max(preferred.height, 44));
        comboBox.setMinimumSize(size);
        comboBox.setPreferredSize(size);
    }

    private void styleList(JList<?> list, ElowbePalette palette) {
        list.setBackground(palette.background);
        list.setForeground(palette.foreground);
        list.setSelectionBackground(palette.selection);
        list.setSelectionForeground(palette.selectionForeground);
        list.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        if (!Boolean.TRUE.equals(list.getClientProperty(GALLERY_KEY))) {
            list.setFixedCellHeight(ROW_HEIGHT);
        }
        installEditorStyling(list);
    }

    private void styleTable(JTable table, ElowbePalette palette) {
        table.setBackground(palette.background);
        table.setForeground(palette.foreground);
        table.setSelectionBackground(palette.selection);
        table.setSelectionForeground(palette.selectionForeground);
        table.setGridColor(new Color(0, 0, 0, 0));
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setRowHeight(ROW_HEIGHT);
        TableCellRenderer defaultRenderer = table.getDefaultRenderer(Object.class);
        if (defaultRenderer != null && !(defaultRenderer instanceof GlyphSafeRenderer)) {
            table.setDefaultRenderer(Object.class, new GlyphSafeRenderer(defaultRenderer));
        }
        installEditorStyling(table);
        configureDetailsColumns(table);
        if (table.getTableHeader() != null) {
            styleTableHeader(table.getTableHeader(), palette);
        }
    }

    /**
     * FilePane drops a bare JTextField into the list/table when the user renames a
     * file or creates a folder. Restyle it on arrival: the default text-field
     * insets are taller than a row, which clips the text vertically.
     */
    private void installEditorStyling(JComponent container) {
        if (Boolean.TRUE.equals(container.getClientProperty(EDITOR_STYLER_KEY))) {
            return;
        }
        container.putClientProperty(EDITOR_STYLER_KEY, Boolean.TRUE);
        container.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent event) {
                if (event.getChild() instanceof JTextField) {
                    styleInlineEditor((JTextField) event.getChild());
                }
            }
        });
    }

    private void styleInlineEditor(JTextField editor) {
        ElowbePalette palette = PaintUtils.palette();
        editor.setFont(UIManager.getFont("FileChooser.font"));
        editor.setOpaque(false);
        editor.setBackground(palette.card);
        editor.setForeground(palette.foreground);
        editor.setCaretColor(palette.foreground);
        editor.setSelectionColor(palette.selection);
        editor.setSelectedTextColor(palette.selectionForeground);
        editor.putClientProperty(ElowbeDefaults.ROUNDED_KEY, Integer.valueOf(EDITOR_RADIUS));
        editor.setBorder(new ElowbeBorder(palette.ring, EDITOR_RADIUS, new Insets(2, 8, 2, 8)));
    }

    private void configureDetailsColumns(JTable table) {
        TableColumnModel columns = table.getColumnModel();
        for (int i = 0; i < columns.getColumnCount(); i++) {
            TableColumn column = columns.getColumn(i);
            String header = String.valueOf(column.getHeaderValue());
            int width = detailColumnWidth(header);
            column.setMinWidth(width == NAME_COLUMN_WIDTH ? 240 : width - 16);
            column.setPreferredWidth(width);
            column.setMaxWidth(width == NAME_COLUMN_WIDTH ? Integer.MAX_VALUE : width + 42);
        }
    }

    private int detailColumnWidth(String header) {
        if (matchesHeader(header, "FileChooser.fileSizeHeaderText")) {
            return SIZE_COLUMN_WIDTH;
        }
        if (matchesHeader(header, "FileChooser.fileTypeHeaderText")) {
            return TYPE_COLUMN_WIDTH;
        }
        if (matchesHeader(header, "FileChooser.fileDateHeaderText")) {
            return MODIFIED_COLUMN_WIDTH;
        }
        if (matchesHeader(header, "FileChooser.fileAttrHeaderText")) {
            return ATTRIBUTES_COLUMN_WIDTH;
        }
        return NAME_COLUMN_WIDTH;
    }

    private boolean matchesHeader(String header, String defaultsKey) {
        String expected = UIManager.getString(defaultsKey);
        return expected != null && expected.equals(header);
    }

    private void styleTableHeader(JTableHeader header, ElowbePalette palette) {
        header.setOpaque(false);
        header.setBackground(palette.background);
        header.setForeground(palette.mutedForeground);
        Font font = UIManager.getFont("Elowbe.font.small");
        header.setFont(font != null ? font : UIManager.getFont("FileChooser.font"));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, palette.border));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new FlatHeaderRenderer());
    }

    private void styleToolBar(JToolBar toolBar, ElowbePalette palette) {
        toolBar.setOpaque(false);
        toolBar.setFloatable(false);
        toolBar.setBackground(palette.background);
        toolBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
    }

    private void styleButton(AbstractButton button) {
        String text = button.getText();
        boolean textButton = text != null && !text.isBlank();
        button.putClientProperty(ElowbeDefaults.BUTTON_VARIANT_KEY, textButton ? "outline" : "ghost");
        button.setMargin(textButton ? new Insets(7, 13, 7, 13) : new Insets(7, 8, 7, 8));
        button.setFocusPainted(false);
    }

    private void styleApproveButton(JFileChooser fileChooser) {
        AbstractButton approve = getApproveButton(fileChooser);
        if (approve != null) {
            approve.putClientProperty(ElowbeDefaults.BUTTON_VARIANT_KEY, "primary");
        }
    }

    /**
     * Adds a third view-toggle next to List/Details that shows the files as a
     * wrapped grid of large thumbnails. It reuses FilePane's list view: on
     * toggle the list switches to HORIZONTAL_WRAP with a thumbnail renderer.
     */
    private void installGalleryButton(JFileChooser fileChooser) {
        if (findButtonByToolTip(fileChooser, GALLERY_TOOLTIP) != null) {
            return;
        }
        AbstractButton listButton = findButtonByToolTip(fileChooser,
                UIManager.getString("FileChooser.listViewButtonToolTipText"));
        AbstractButton detailsButton = findButtonByToolTip(fileChooser,
                UIManager.getString("FileChooser.detailsViewButtonToolTipText"));
        if (listButton == null || detailsButton == null || detailsButton.getParent() == null) {
            return;
        }

        JToggleButton galleryButton = new JToggleButton(ElowbeIcons.gallery(14));
        galleryButton.setToolTipText(GALLERY_TOOLTIP);
        galleryButton.putClientProperty(ElowbeDefaults.BUTTON_VARIANT_KEY, "ghost");
        galleryButton.setMargin(new Insets(7, 8, 7, 8));
        galleryButton.setFocusPainted(false);
        if (listButton.getModel() instanceof DefaultButtonModel) {
            ButtonGroup group = ((DefaultButtonModel) listButton.getModel()).getGroup();
            if (group != null) {
                group.add(galleryButton);
            }
        }
        galleryButton.addItemListener(event -> {
            if (galleryTransition) {
                return;
            }
            if (event.getStateChange() == ItemEvent.SELECTED) {
                enterGalleryMode(fileChooser, listButton, galleryButton);
            } else {
                exitGalleryMode(fileChooser);
            }
        });

        Container parent = detailsButton.getParent();
        int detailsIndex = 0;
        for (int i = 0; i < parent.getComponentCount(); i++) {
            if (parent.getComponent(i) == detailsButton) {
                detailsIndex = i;
                break;
            }
        }
        parent.add(galleryButton, detailsIndex + 1);
        parent.revalidate();
        parent.repaint();
    }

    private void enterGalleryMode(JFileChooser fileChooser, AbstractButton listButton, AbstractButton galleryButton) {
        // Switching FilePane to list view makes MetalFileChooserUI select the
        // List toggle, which would bounce the gallery toggle out of the group.
        // Guard the transition and reclaim the selection afterwards.
        galleryTransition = true;
        try {
            for (ActionListener listener : listButton.getActionListeners()) {
                listener.actionPerformed(new ActionEvent(listButton, ActionEvent.ACTION_PERFORMED, "viewTypeList"));
            }
            galleryButton.setSelected(true);
        } finally {
            galleryTransition = false;
        }
        style(fileChooser);
        styleApproveButton(fileChooser);

        JList<?> list = findFileList(fileChooser);
        if (list == null) {
            return;
        }
        if (list.getClientProperty(ORIGINAL_RENDERER_KEY) == null) {
            list.putClientProperty(ORIGINAL_RENDERER_KEY, list.getCellRenderer());
        }
        list.putClientProperty(GALLERY_KEY, Boolean.TRUE);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(-1);
        list.setFixedCellWidth(GALLERY_CELL_WIDTH);
        list.setFixedCellHeight(GALLERY_CELL_HEIGHT);
        setRenderer(list, new GalleryRenderer(list));
        list.revalidate();
        list.repaint();
    }

    private void exitGalleryMode(JFileChooser fileChooser) {
        JList<?> list = findFileList(fileChooser);
        if (list == null || !Boolean.TRUE.equals(list.getClientProperty(GALLERY_KEY))) {
            return;
        }
        list.putClientProperty(GALLERY_KEY, null);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(8);
        list.setFixedCellWidth(-1);
        list.setFixedCellHeight(ROW_HEIGHT);
        Object original = list.getClientProperty(ORIGINAL_RENDERER_KEY);
        if (original instanceof ListCellRenderer) {
            setRenderer(list, (ListCellRenderer<?>) original);
        }
        list.revalidate();
        list.repaint();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void setRenderer(JList<?> list, ListCellRenderer<?> renderer) {
        ((JList) list).setCellRenderer((ListCellRenderer) renderer);
    }

    private JList<?> findFileList(Component component) {
        if (component instanceof JList) {
            return (JList<?>) component;
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                JList<?> list = findFileList(child);
                if (list != null) {
                    return list;
                }
            }
        }
        return null;
    }

    private final class GalleryRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        private final JList<?> fileList;

        private GalleryRenderer(JList<?> fileList) {
            this.fileList = fileList;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            File file = value instanceof File ? (File) value : null;
            String name = file == null ? String.valueOf(value) : getFileChooser().getName(file);
            super.getListCellRendererComponent(list, name, index, false, false);
            ElowbePalette palette = PaintUtils.palette();
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setHorizontalTextPosition(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.BOTTOM);
            setIconTextGap(8);
            setBorder(new EmptyBorder(10, 8, 10, 8));
            setFont(UIManager.getFont("FileChooser.font"));
            setForeground(isSelected ? palette.selectionForeground : palette.foreground);
            putClientProperty(GALLERY_SELECTED_KEY, Boolean.valueOf(isSelected));
            setIcon(galleryIcon(file));
            return this;
        }

        private Icon galleryIcon(File file) {
            Image thumbnail = ElowbeThumbnails.thumbnail(file, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT,
                    fileList::repaint);
            if (thumbnail != null) {
                return new ThumbnailIcon(thumbnail);
            }
            if (file != null && file.isDirectory()) {
                return new CenteredIcon(ElowbeIcons.folder(40));
            }
            return new CenteredIcon(ElowbeIcons.file(40));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            if (Boolean.TRUE.equals(getClientProperty(GALLERY_SELECTED_KEY))) {
                Graphics2D g2 = PaintUtils.prepare(graphics);
                try {
                    PaintUtils.fillRound(g2, 2, 2, getWidth() - 4, getHeight() - 4, ElowbeDefaults.RADIUS_SM,
                            PaintUtils.palette().selection);
                } finally {
                    g2.dispose();
                }
            }
            super.paintComponent(graphics);
        }
    }

    private static final class ThumbnailIcon implements Icon {
        private final Image image;

        private ThumbnailIcon(Image image) {
            this.image = image;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = PaintUtils.prepare(graphics);
            try {
                int width = image.getWidth(null);
                int height = image.getHeight(null);
                int dx = x + (THUMBNAIL_WIDTH - width) / 2;
                int dy = y + (THUMBNAIL_HEIGHT - height) / 2;
                g2.clip(PaintUtils.roundedRect(dx, dy, width, height, 12));
                g2.drawImage(image, dx, dy, null);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return THUMBNAIL_WIDTH;
        }

        @Override
        public int getIconHeight() {
            return THUMBNAIL_HEIGHT;
        }
    }

    private static final class CenteredIcon implements Icon {
        private final Icon delegate;

        private CenteredIcon(Icon delegate) {
            this.delegate = delegate;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            delegate.paintIcon(component, graphics,
                    x + (THUMBNAIL_WIDTH - delegate.getIconWidth()) / 2,
                    y + (THUMBNAIL_HEIGHT - delegate.getIconHeight()) / 2);
        }

        @Override
        public int getIconWidth() {
            return THUMBNAIL_WIDTH;
        }

        @Override
        public int getIconHeight() {
            return THUMBNAIL_HEIGHT;
        }
    }

    /**
     * The JDK formats dates with narrow no-break spaces (U+202F) that the bundled
     * Geist Mono font has no glyph for, which renders as tofu boxes.
     */
    private static final class GlyphSafeRenderer implements TableCellRenderer {
        private final TableCellRenderer delegate;

        private GlyphSafeRenderer(TableCellRenderer delegate) {
            this.delegate = delegate;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component rendered = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (rendered instanceof JLabel) {
                JLabel label = (JLabel) rendered;
                String text = label.getText();
                if (text != null && (text.indexOf('\u202F') >= 0 || text.indexOf('\u00A0') >= 0)) {
                    label.setText(text.replace('\u202F', ' ').replace('\u00A0', ' '));
                }
            }
            return rendered;
        }
    }

    private static final class FlatHeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            ElowbePalette palette = PaintUtils.palette();
            setOpaque(false);
            setForeground(palette.mutedForeground);
            Font font = UIManager.getFont("Elowbe.font.small");
            setFont(font != null ? font : table.getFont());
            setBorder(new EmptyBorder(8, 10, 8, 10));
            setHorizontalAlignment(SwingConstants.LEFT);
            return this;
        }
    }
}
