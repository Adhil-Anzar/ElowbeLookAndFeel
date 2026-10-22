package com.elowbe.laf.ui;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.ElowbeBorder;
import com.elowbe.laf.util.ElowbeCard;
import com.elowbe.laf.util.PaintUtils;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicListUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.util.Locale;

/**
 * List UI that renders {@link ElowbeCard} values as rounded cards while
 * delegating every other value to the list's regular cell renderer.
 */
public class ElowbeListUI extends BasicListUI {

    public static ComponentUI createUI(JComponent component) {
        return new ElowbeListUI();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void installDefaults() {
        super.installDefaults();
        ListCellRenderer renderer = list.getCellRenderer();
        if (!(renderer instanceof CardCellRenderer)) {
            list.setCellRenderer(new CardCellRenderer(renderer));
        }
    }

    private static final class CardCellRenderer implements ListCellRenderer<Object> {
        private final ListCellRenderer<Object> delegate;

        @SuppressWarnings("unchecked")
        private CardCellRenderer(ListCellRenderer<?> delegate) {
            this.delegate = (ListCellRenderer<Object>) (delegate == null
                    ? new DefaultListCellRenderer()
                    : delegate);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean selected, boolean focused) {
            if (!(value instanceof ElowbeCard card)) {
                return delegate.getListCellRendererComponent(list, value, index, selected, focused);
            }
            return cardComponent(list, card, selected);
        }

        private Component cardComponent(JList<?> list, ElowbeCard card, boolean selected) {
            ElowbePalette palette = PaintUtils.palette();
            Icon icon = card.getIcon();

            Font baseFont = list.getFont() != null ? list.getFont() : UIManager.getFont("List.font");
            if (baseFont == null) {
                baseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
            }
            Font smallFont = UIManager.getFont("Elowbe.font.small");
            if (smallFont == null) {
                smallFont = baseFont.deriveFont(Math.max(9f, baseFont.getSize() - 1f));
            }

            int wrapWidth = wrapWidth(list, icon);
            CardLabel label = new CardLabel(
                    cardHtml(card, wrapWidth, palette.mutedForeground, smallFont.getSize()),
                    selected ? palette.accent : palette.card,
                    selected ? palette.primary : palette.border,
                    wrapWidth + 42 + CardLabel.iconSpace(icon),
                    icon);
            label.setFont(baseFont);
            label.setForeground(palette.foreground);
            return label;
        }

        private static int wrapWidth(JList<?> list, Icon icon) {
            int width = list.getFixedCellWidth();
            if (width <= 0) {
                width = list.getWidth();
            }
            if (width <= 0) {
                width = 220;
            }
            // Cell gap (6) + card padding (26) + a little breathing room.
            return Math.max(80, width - 42 - CardLabel.iconSpace(icon));
        }

        private static String cardHtml(ElowbeCard card, int wrapWidth, Color mutedColor, int smallSize) {
            String muted = toHex(mutedColor);
            StringBuilder html = new StringBuilder("<html><body style='width:")
                    .append(wrapWidth).append("px'>");

            boolean hasTitle = hasText(card.getTitle());
            boolean hasBody = hasText(card.getBody());
            boolean hasCaption = hasText(card.getCaption());

            if (hasTitle) {
                html.append("<b>").append(escape(card.getTitle())).append("</b>");
            }
            if (hasBody) {
                if (hasTitle) {
                    html.append("<br/>");
                }
                html.append("<span style='color:").append(muted)
                        .append(";font-size:").append(smallSize).append("px'>")
                        .append(card.isHtml() ? stripHtmlWrapper(card.getBody()) : escape(card.getBody()))
                        .append("</span>");
            }
            if (hasCaption) {
                if (hasTitle || hasBody) {
                    html.append("<br/>");
                }
                html.append("<span style='color:").append(muted)
                        .append(";font-size:").append(Math.max(9, smallSize - 1)).append("px'>")
                        .append(escape(card.getCaption()))
                        .append("</span>");
            }

            return html.append("</body></html>").toString();
        }

        private static String stripHtmlWrapper(String html) {
            String text = html.trim();
            if (text.regionMatches(true, 0, "<html", 0, 5)) {
                int end = text.indexOf('>');
                text = end >= 0 ? text.substring(end + 1).trim() : text;
            }
            if (text.regionMatches(true, 0, "<body", 0, 5)) {
                int end = text.indexOf('>');
                text = end >= 0 ? text.substring(end + 1).trim() : text;
            }
            if (text.regionMatches(true, text.length() - 14, "</body></html>", 0, 14)) {
                text = text.substring(0, text.length() - 14);
            } else if (text.regionMatches(true, text.length() - 7, "</html>", 0, 7)) {
                text = text.substring(0, text.length() - 7);
            }
            return text;
        }

        private static String toHex(Color color) {
            return String.format(Locale.ROOT, "#%02x%02x%02x",
                    color.getRed(), color.getGreen(), color.getBlue());
        }

        private static boolean hasText(String text) {
            return text != null && !text.isBlank();
        }
    }

    /**
     * A label that paints its own rounded card background. Keeping the renderer
     * a single label also means Swing's {@code CellRendererPane} can lay it out
     * without nested containers.
     */
    private static final class CardLabel extends JLabel {
        private static final long serialVersionUID = 1L;
        private static final int GAP_LEFT = 3;
        private static final int GAP_BOTTOM = 6;
        private static final int PADDING_X = 13;
        private static final int PADDING_Y = 11;
        static final int ICON_GAP = 10;

        private final Color fill;
        private final int maxWidth;
        private final Icon icon;

        private CardLabel(String text, Color fill, Color borderColor, int maxWidth, Icon icon) {
            super(text);
            this.fill = fill;
            this.maxWidth = maxWidth;
            this.icon = icon;
            setOpaque(false);
            setVerticalAlignment(SwingConstants.TOP);
            int textLeft = PADDING_X + iconSpace(icon);
            setBorder(BorderFactory.createCompoundBorder(
                    new EmptyBorder(0, GAP_LEFT, GAP_BOTTOM, GAP_LEFT),
                    new ElowbeBorder(borderColor, ElowbeDefaults.RADIUS_SM,
                            new Insets(PADDING_Y, textLeft, PADDING_Y, PADDING_X))));
        }

        private static int iconSpace(Icon icon) {
            return icon == null ? 0 : icon.getIconWidth() + ICON_GAP;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            return new Dimension(Math.min(size.width, maxWidth), size.height);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = PaintUtils.prepare(graphics);
            try {
                int width = Math.max(0, getWidth() - GAP_LEFT * 2);
                int height = Math.max(0, getHeight() - GAP_BOTTOM);
                PaintUtils.fillRound(g2, GAP_LEFT, 0, width, height,
                        ElowbeDefaults.RADIUS_SM, fill);
                if (icon != null) {
                    g2.setColor(getForeground());
                    icon.paintIcon(this, g2, GAP_LEFT + PADDING_X, PADDING_Y);
                }
            } finally {
                g2.dispose();
            }
            super.paintComponent(graphics);
        }
    }

    /**
     * Escapes text for use in Swing HTML labels.
     */
    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
