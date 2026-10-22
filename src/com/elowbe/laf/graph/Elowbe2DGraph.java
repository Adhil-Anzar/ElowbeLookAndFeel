package com.elowbe.laf.graph;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleFunction;

/**
 * A scrollable, theme-aware line chart for Swing.
 *
 * <p>Points are drawn from left to right using a fixed horizontal spacing. The
 * vertical position of each point comes from {@link ElowbeGraphPoint#getValue()}.
 * The component is intentionally domain neutral: values, axis titles, tick
 * formatting, colors and spacing are all configurable.</p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * Elowbe2DGraph graph = new Elowbe2DGraph();
 * graph.setXAxisTitle("Month");
 * graph.setYAxisTitle("Revenue");
 * graph.addPoint(new ElowbeGraphPoint(120, "Jan"));
 * graph.addPoint(new ElowbeGraphPoint(180, "Feb"));
 * }</pre>
 */
public class Elowbe2DGraph extends JComponent implements Scrollable {

    private static final int DEFAULT_GRID_SIZE = 24;
    private static final int DEFAULT_POINT_SPACING = 96;
    private static final int DEFAULT_HIT_RADIUS = 16;
    private static final int DEFAULT_POINT_RADIUS = 4;
    private static final int DEFAULT_PADDING = 24;
    private static final int DEFAULT_Y_HEADROOM = 6;
    private static final int LABEL_GAP = 6;
    private static final float DEFAULT_LINE_WIDTH = 1.5f;

    private final List<ElowbeGraphPoint> points = new ArrayList<>();
    private final List<PointSelectionListener> listeners = new ArrayList<>();

    private int selectedPointIndex = -1;

    // Base outer gutter; it grows automatically to fit axis titles and labels.
    private int padding = DEFAULT_PADDING;

    // Value represented by a single point-to-point step on the X axis.
    private double xValuePerPoint = 1.0;

    // Numbers are drawn along the X axis every xTickInterval points.
    private int xTickInterval = 4;

    private String xAxisTitle = "X Axis";
    private String yAxisTitle = "Y Axis";

    private int gridSize = DEFAULT_GRID_SIZE;
    private int pointSpacing = DEFAULT_POINT_SPACING;
    private int hitRadius = DEFAULT_HIT_RADIUS;
    private int pointRadius = DEFAULT_POINT_RADIUS;
    private int yHeadroom = DEFAULT_Y_HEADROOM;
    private float lineWidth = DEFAULT_LINE_WIDTH;

    // null means "use the current theme color".
    private Color lineColor;
    private Color pointColor;
    private Color labelColor;

    private boolean gridVisible = true;
    private boolean lineVisible = true;
    private boolean pointsVisible = true;
    private boolean labelsVisible = true;

    private DoubleFunction<String> valueFormatter = Elowbe2DGraph::formatNumber;

    public Elowbe2DGraph() {
        setOpaque(false);
        setFocusable(true);
        setPreferredSize(new Dimension(640, 360));
        Font font = getFont();
        setFont(font != null ? font.deriveFont(11f) : new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        ToolTipManager.sharedInstance().registerComponent(this);
        installMouseBehavior();
        installKeyBindings();
    }

    // ---------------------------------------------------------------------
    // Data
    // ---------------------------------------------------------------------

    public void addPoint(ElowbeGraphPoint point) {
        if (point != null) {
            points.add(point);
            dataChanged();
        }
    }

    public void removePoint(ElowbeGraphPoint point) {
        int index = points.indexOf(point);
        if (index < 0) {
            return;
        }
        points.remove(index);
        if (selectedPointIndex == index) {
            selectedPointIndex = -1;
            notifySelectionCleared();
        } else if (selectedPointIndex > index) {
            selectedPointIndex--;
        }
        dataChanged();
    }

    /**
     * Replaces all points with a defensive copy of the supplied list.
     */
    public void setPoints(List<ElowbeGraphPoint> points) {
        ElowbeGraphPoint previousSelection = getSelectedPoint();
        this.points.clear();
        if (points != null) {
            for (ElowbeGraphPoint point : points) {
                if (point != null) {
                    this.points.add(point);
                }
            }
        }
        if (selectedPointIndex >= this.points.size()) {
            selectedPointIndex = -1;
            notifySelectionCleared();
        } else if (selectedPointIndex >= 0 && this.points.get(selectedPointIndex) != previousSelection) {
            notifyPointSelected(this.points.get(selectedPointIndex));
        }
        dataChanged();
    }

    public List<ElowbeGraphPoint> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public void setPoint(int index, ElowbeGraphPoint point) {
        if (index >= 0 && index < points.size() && point != null) {
            points.set(index, point);
            if (index == selectedPointIndex) {
                notifyPointSelected(point);
            }
            dataChanged();
        }
    }

    public ElowbeGraphPoint getPointForIndex(int index) {
        return index >= 0 && index < points.size() ? points.get(index) : null;
    }

    public int getIndexForPoint(ElowbeGraphPoint point) {
        return points.indexOf(point);
    }

    public int getPointCount() {
        return points.size();
    }

    public void clearPoints() {
        if (points.isEmpty() && selectedPointIndex < 0) {
            return;
        }
        points.clear();
        if (selectedPointIndex >= 0) {
            selectedPointIndex = -1;
            notifySelectionCleared();
        }
        dataChanged();
    }

    // ---------------------------------------------------------------------
    // Selection
    // ---------------------------------------------------------------------

    public ElowbeGraphPoint getSelectedPoint() {
        return getPointForIndex(selectedPointIndex);
    }

    public int getSelectedPointIndex() {
        return selectedPointIndex;
    }

    /**
     * Selects the point at {@code index}, or clears the selection when the
     * index is outside the data range.
     */
    public void setSelectedPointIndex(int index) {
        int normalized = index >= 0 && index < points.size() ? index : -1;
        if (normalized == selectedPointIndex) {
            return;
        }
        selectedPointIndex = normalized;
        repaint();
        if (normalized >= 0) {
            notifyPointSelected(points.get(normalized));
        } else {
            notifySelectionCleared();
        }
    }

    public void setSelectedPoint(ElowbeGraphPoint point) {
        setSelectedPointIndex(point == null ? -1 : points.indexOf(point));
    }

    public void addPointSelectionListener(PointSelectionListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removePointSelectionListener(PointSelectionListener listener) {
        listeners.remove(listener);
    }

    private void notifyPointSelected(ElowbeGraphPoint point) {
        for (PointSelectionListener listener : new ArrayList<>(listeners)) {
            listener.pointSelected(point);
        }
    }

    private void notifySelectionCleared() {
        for (PointSelectionListener listener : new ArrayList<>(listeners)) {
            listener.pointSelectionCleared();
        }
    }

    // ---------------------------------------------------------------------
    // Configuration
    // ---------------------------------------------------------------------

    public double getXValuePerPoint() {
        return xValuePerPoint;
    }

    /**
     * Sets the numeric distance between two consecutive points on the X axis.
     * This only affects tick labels, not the pixel spacing.
     */
    public void setXValuePerPoint(double xValuePerPoint) {
        this.xValuePerPoint = xValuePerPoint;
        repaint();
    }

    public int getXTickInterval() {
        return xTickInterval;
    }

    /**
     * Sets how many points lie between two numbered ticks. Use {@code 0} to
     * hide the X axis numbers.
     */
    public void setXTickInterval(int xTickInterval) {
        this.xTickInterval = Math.max(0, xTickInterval);
        repaint();
    }

    public String getXAxisTitle() {
        return xAxisTitle;
    }

    /**
     * Sets the horizontal axis title. {@code null} or an empty string hides it.
     */
    public void setXAxisTitle(String xAxisTitle) {
        this.xAxisTitle = xAxisTitle;
        revalidate();
        repaint();
    }

    public String getYAxisTitle() {
        return yAxisTitle;
    }

    /**
     * Sets the vertical axis title. {@code null} or an empty string hides it.
     */
    public void setYAxisTitle(String yAxisTitle) {
        this.yAxisTitle = yAxisTitle;
        revalidate();
        repaint();
    }

    /**
     * Formats values used for Y axis labels and X axis ticks. Pass {@code null}
     * to restore the default number formatting.
     */
    public void setValueFormatter(DoubleFunction<String> valueFormatter) {
        this.valueFormatter = valueFormatter == null ? Elowbe2DGraph::formatNumber : valueFormatter;
        revalidate();
        repaint();
    }

    public DoubleFunction<String> getValueFormatter() {
        return valueFormatter;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = Math.max(1, gridSize);
        repaint();
    }

    public int getPointSpacing() {
        return pointSpacing;
    }

    /**
     * Sets the horizontal distance, in pixels, between two consecutive points.
     */
    public void setPointSpacing(int pointSpacing) {
        this.pointSpacing = Math.max(1, pointSpacing);
        revalidate();
        repaint();
    }

    public int getHitRadius() {
        return hitRadius;
    }

    public void setHitRadius(int hitRadius) {
        this.hitRadius = Math.max(0, hitRadius);
    }

    public int getPointRadius() {
        return pointRadius;
    }

    public void setPointRadius(int pointRadius) {
        this.pointRadius = Math.max(0, pointRadius);
        repaint();
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = Math.max(0, padding);
        revalidate();
        repaint();
    }

    public int getYHeadroom() {
        return yHeadroom;
    }

    /**
     * Sets the empty value range added above the highest and below the lowest
     * point so the line does not touch the plot edges.
     */
    public void setYHeadroom(int yHeadroom) {
        this.yHeadroom = Math.max(0, yHeadroom);
        revalidate();
        repaint();
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = Math.max(0.1f, lineWidth);
        repaint();
    }

    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Sets the line color. {@code null} restores the theme primary color.
     */
    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
        repaint();
    }

    public Color getPointColor() {
        return pointColor;
    }

    /**
     * Sets the default point color. {@code null} uses the line color. A color
     * on an individual {@link ElowbeGraphPoint} takes precedence.
     */
    public void setPointColor(Color pointColor) {
        this.pointColor = pointColor;
        repaint();
    }

    public Color getLabelColor() {
        return labelColor;
    }

    /**
     * Sets the point label color. {@code null} restores the theme muted color.
     */
    public void setLabelColor(Color labelColor) {
        this.labelColor = labelColor;
        repaint();
    }

    public boolean isGridVisible() {
        return gridVisible;
    }

    public void setGridVisible(boolean gridVisible) {
        this.gridVisible = gridVisible;
        repaint();
    }

    public boolean isLineVisible() {
        return lineVisible;
    }

    public void setLineVisible(boolean lineVisible) {
        this.lineVisible = lineVisible;
        repaint();
    }

    public boolean isPointsVisible() {
        return pointsVisible;
    }

    public void setPointsVisible(boolean pointsVisible) {
        this.pointsVisible = pointsVisible;
        repaint();
    }

    public boolean isLabelsVisible() {
        return labelsVisible;
    }

    public void setLabelsVisible(boolean labelsVisible) {
        this.labelsVisible = labelsVisible;
        repaint();
    }

    // Backwards-compatible aliases for the original API.
    public double getXSlice() {
        return getXValuePerPoint();
    }

    public void setXSlice(double xSlice) {
        setXValuePerPoint(xSlice);
    }

    public int getXSliceSpacing() {
        return getXTickInterval();
    }

    public void setXSliceSpacing(int xSliceSpacing) {
        setXTickInterval(xSliceSpacing);
    }

    public String getXAxisText() {
        return getXAxisTitle();
    }

    public void setXAxisText(String xAxisText) {
        setXAxisTitle(xAxisText);
    }

    public String getYAxisText() {
        return getYAxisTitle();
    }

    public void setYAxisText(String yAxisText) {
        setYAxisTitle(yAxisText);
    }

    // ---------------------------------------------------------------------
    // Scrolling
    // ---------------------------------------------------------------------

    private void dataChanged() {
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension base = super.getPreferredSize();
        int width = contentWidth();
        int height = base == null || base.height <= 0 ? 360 : base.height;
        JViewport viewport = enclosingViewport();
        if (viewport != null) {
            width = Math.max(width, viewport.getWidth());
            height = Math.max(height, viewport.getHeight());
        }
        return new Dimension(width, height);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.HORIZONTAL ? pointSpacing : gridSize;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.HORIZONTAL ? visibleRect.width : visibleRect.height;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        JViewport viewport = enclosingViewport();
        return viewport == null || contentWidth() <= viewport.getWidth();
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return true;
    }

    private JViewport enclosingViewport() {
        return getParent() instanceof JViewport ? (JViewport) getParent() : null;
    }

    private int contentWidth() {
        ChartLayout layout = computeLayout();
        return layout.left + layout.right + Math.max(0, points.size() - 1) * pointSpacing;
    }

    // ---------------------------------------------------------------------
    // Painting
    // ---------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            ChartLayout layout = computeLayout();
            paintSurface(g2);
            if (gridVisible) {
                paintGrid(g2, layout);
            }
            paintAxis(g2, layout);
            if (!points.isEmpty()) {
                paintYAxisNumbers(g2, layout);
                paintXAxisTicks(g2, layout);
            }
            paintAxisTitles(g2, layout);
            if (!points.isEmpty()) {
                if (lineVisible) {
                    paintLines(g2, layout);
                }
                if (pointsVisible) {
                    paintPoints(g2, layout);
                }
            }
        } finally {
            g2.dispose();
        }
    }

    private void paintSurface(Graphics2D g2) {
        ElowbePalette palette = PaintUtils.palette();
        int radius = PaintUtils.radius(this, ElowbeDefaults.RADIUS_LG);
        PaintUtils.fillRound(g2, 0, 0, getWidth(), getHeight(), radius, palette.background);
        PaintUtils.drawRound(g2, 0, 0, getWidth(), getHeight(), radius, palette.border);
    }

    private void paintGrid(Graphics2D g2, ChartLayout layout) {
        ElowbePalette palette = PaintUtils.palette();
        g2.setColor(PaintUtils.withAlpha(palette.border, ElowbeDefaults.theme().isDark() ? 74 : 86));
        g2.setStroke(new BasicStroke(1f));
        int left = layout.left;
        int right = getWidth() - layout.right;
        int top = layout.top;
        int bottom = getHeight() - layout.bottom;
        for (int x = left; x <= right; x += gridSize) {
            g2.drawLine(x, top, x, bottom);
        }
        for (int y = top; y <= bottom; y += gridSize) {
            g2.drawLine(left, y, right, y);
        }
    }

    private void paintAxis(Graphics2D g2, ChartLayout layout) {
        ElowbePalette palette = PaintUtils.palette();
        g2.setColor(palette.border);
        g2.setStroke(new BasicStroke(3f));
        int left = layout.left;
        int right = getWidth() - layout.right;
        int top = layout.top;
        int bottom = getHeight() - layout.bottom;
        g2.drawLine(left, bottom, right, bottom);
        g2.drawLine(left, top, left, bottom);
    }

    private void paintYAxisNumbers(Graphics2D g2, ChartLayout layout) {
        ValueStats stats = computeValueStats();

        int plotTop = layout.top;
        int plotBottom = getHeight() - layout.bottom;
        int plotHeight = Math.max(1, plotBottom - plotTop);
        int plotLeft = layout.left;

        g2.setFont(axisNumberFont());
        FontMetrics metrics = g2.getFontMetrics();
        g2.setColor(axisNumberColor());

        drawYAxisLabel(g2, metrics, plotLeft, formatValue(stats.min - yHeadroom), plotBottom);
        drawYAxisLabel(g2, metrics, plotLeft, formatValue(stats.avg),
                yFor(stats.avg, stats, plotTop, plotHeight));
        drawYAxisLabel(g2, metrics, plotLeft, formatValue(stats.max + yHeadroom), plotTop);
    }

    private void drawYAxisLabel(Graphics2D g2, FontMetrics metrics, int plotLeft, String text, int y) {
        int labelX = plotLeft - metrics.stringWidth(text) - LABEL_GAP;
        int labelY = y + metrics.getAscent() / 2;
        g2.setFont(axisNumberFont());
        g2.setColor(axisNumberColor());
        g2.drawString(text, Math.max(2, labelX), labelY);
    }

    private void paintXAxisTicks(Graphics2D g2, ChartLayout layout) {
        if (xTickInterval <= 0 || points.size() < 2) {
            return;
        }
        int plotLeft = layout.left;
        int plotBottom = getHeight() - layout.bottom;
        int lastPointX = xFor(points.size() - 1, plotLeft);

        g2.setFont(axisNumberFont());
        FontMetrics metrics = g2.getFontMetrics();
        g2.setColor(axisNumberColor());

        int tickY = plotBottom + LABEL_GAP + metrics.getAscent();
        int tickSpacingPx = xTickInterval * pointSpacing;
        for (int n = 1; plotLeft + n * tickSpacingPx <= lastPointX; n++) {
            int tickX = plotLeft + n * tickSpacingPx;
            double value = (double) n * xTickInterval * xValuePerPoint;
            String text = formatValue(value);
            int textX = tickX - metrics.stringWidth(text) / 2;
            g2.drawString(text, textX, tickY);
        }
    }

    private void paintAxisTitles(Graphics2D g2, ChartLayout layout) {
        ElowbePalette palette = PaintUtils.palette();
        Font titleFont = axisTitleFont();
        FontMetrics titleMetrics = getFontMetrics(titleFont);
        FontMetrics axisMetrics = getFontMetrics(axisNumberFont());

        int plotTop = layout.top;
        int plotBottom = getHeight() - layout.bottom;
        int plotHeight = Math.max(1, plotBottom - plotTop);
        int plotLeft = layout.left;
        int plotRight = getWidth() - layout.right;

        if (hasText(xAxisTitle)) {
            int numbersRowBottom = points.isEmpty()
                    ? plotBottom
                    : plotBottom + LABEL_GAP + axisMetrics.getAscent() + axisMetrics.getDescent();
            int textY = numbersRowBottom + LABEL_GAP + titleMetrics.getAscent();
            int textX = plotLeft + (plotRight - plotLeft - titleMetrics.stringWidth(xAxisTitle)) / 2;
            g2.setFont(titleFont);
            g2.setColor(palette.mutedForeground);
            g2.drawString(xAxisTitle, Math.max(plotLeft, textX), textY);
        }

        if (hasText(yAxisTitle)) {
            Graphics2D rotated = (Graphics2D) g2.create();
            try {
                rotated.setFont(titleFont);
                FontMetrics rotatedMetrics = rotated.getFontMetrics();
                int textLength = rotatedMetrics.stringWidth(yAxisTitle);

                int numbersBlockLeft = plotLeft - maxYAxisLabelWidth(axisMetrics) - LABEL_GAP;
                int pivotX = numbersBlockLeft - LABEL_GAP - rotatedMetrics.getDescent();
                int pivotY = plotTop + (plotHeight + textLength) / 2;

                rotated.translate(pivotX, pivotY);
                rotated.rotate(-Math.PI / 2);
                rotated.setColor(palette.mutedForeground);
                rotated.drawString(yAxisTitle, 0, 0);
            } finally {
                rotated.dispose();
            }
        }
    }

    private void paintLines(Graphics2D g2, ChartLayout layout) {
        if (points.size() < 2) {
            return;
        }
        g2.setStroke(new BasicStroke(lineWidth));

        ValueStats stats = computeValueStats();
        int plotTop = layout.top;
        int plotBottom = getHeight() - layout.bottom;
        int plotHeight = Math.max(1, plotBottom - plotTop);
        int plotLeft = layout.left;

        int previousX = 0;
        int previousY = 0;
        for (int i = 0; i < points.size(); i++) {
            int x = xFor(i, plotLeft);
            int y = yFor(points.get(i).getValue(), stats, plotTop, plotHeight);
            if (i != 0) {
                g2.setColor(lineColor());
                g2.drawLine(previousX, previousY, x, y);
            }
            previousX = x;
            previousY = y;
        }
    }

    private void paintPoints(Graphics2D g2, ChartLayout layout) {
        ElowbePalette palette = PaintUtils.palette();
        ValueStats stats = computeValueStats();
        int plotTop = layout.top;
        int plotBottom = getHeight() - layout.bottom;
        int plotHeight = Math.max(1, plotBottom - plotTop);
        int plotLeft = layout.left;

        g2.setFont(labelFont());
        FontMetrics labelMetrics = g2.getFontMetrics();

        for (int i = 0; i < points.size(); i++) {
            ElowbeGraphPoint point = points.get(i);
            int centerX = xFor(i, plotLeft);
            int centerY = yFor(point.getValue(), stats, plotTop, plotHeight);
            boolean selected = i == selectedPointIndex;
            int diameter = pointRadius * 2;

            Color base = pointColor(point);
            g2.setColor(selected ? PaintUtils.mix(base, palette.background, 0.35f) : base);
            g2.fillOval(centerX - pointRadius, centerY - pointRadius, diameter, diameter);

            if (selected && pointRadius > 0) {
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(palette.foreground);
                g2.drawOval(centerX - pointRadius - 1, centerY - pointRadius - 1, diameter + 2, diameter + 2);
            }

            if (labelsVisible) {
                paintPointLabel(g2, labelMetrics, point, centerX, centerY, layout);
            }
        }
    }

    private void paintPointLabel(Graphics2D g2, FontMetrics metrics, ElowbeGraphPoint point,
            int centerX, int centerY, ChartLayout layout) {
        String label = point.getLabel();
        if (!hasText(label)) {
            return;
        }

        int labelWidth = metrics.stringWidth(label);
        int clearance = pointRadius + 2;
        int plotTop = layout.top;
        int plotBottom = getHeight() - layout.bottom;
        boolean placeBelow = (centerY - plotTop) < (plotBottom - plotTop) / 3.0;

        int textY = placeBelow
                ? centerY + clearance + metrics.getAscent() + LABEL_GAP
                : centerY - clearance - metrics.getDescent() - LABEL_GAP;

        int textX = centerX - labelWidth / 2;
        textX = Math.max(layout.left, Math.min(textX, getWidth() - layout.right - labelWidth));

        g2.setFont(labelFont());
        g2.setColor(labelColor());
        g2.drawString(label, textX, textY);
    }

    private int xFor(int index, int plotLeft) {
        return plotLeft + index * pointSpacing;
    }

    private int yFor(double value, ValueStats stats, int plotTop, int plotHeight) {
        double normalized = (value - stats.min + yHeadroom) / (stats.range + 2 * yHeadroom);
        int plotBottom = plotTop + plotHeight;
        return plotBottom - (int) Math.round(normalized * plotHeight);
    }

    // ---------------------------------------------------------------------
    // Layout and theme helpers
    // ---------------------------------------------------------------------

    private static final class ChartLayout {
        private final int top;
        private final int left;
        private final int bottom;
        private final int right;

        private ChartLayout(int top, int left, int bottom, int right) {
            this.top = top;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
        }
    }

    private ChartLayout computeLayout() {
        FontMetrics axisMetrics = getFontMetrics(axisNumberFont());
        FontMetrics titleMetrics = getFontMetrics(axisTitleFont());

        int left = padding;
        if (!points.isEmpty()) {
            left += maxYAxisLabelWidth(axisMetrics) + LABEL_GAP;
        }
        if (hasText(yAxisTitle)) {
            left += titleMetrics.getAscent() + titleMetrics.getDescent() + LABEL_GAP;
        }

        int bottom = padding;
        if (!points.isEmpty()) {
            bottom += axisMetrics.getAscent() + axisMetrics.getDescent() + LABEL_GAP;
        }
        if (hasText(xAxisTitle)) {
            bottom += titleMetrics.getAscent() + titleMetrics.getDescent() + LABEL_GAP;
        }

        return new ChartLayout(padding, left, bottom, padding);
    }

    private int maxYAxisLabelWidth(FontMetrics metrics) {
        if (points.isEmpty()) {
            return 0;
        }
        ValueStats stats = computeValueStats();
        String min = formatValue(stats.min - yHeadroom);
        String avg = formatValue(stats.avg);
        String max = formatValue(stats.max + yHeadroom);
        return Math.max(metrics.stringWidth(min), Math.max(metrics.stringWidth(avg), metrics.stringWidth(max)));
    }

    private Font axisNumberFont() {
        return baseSmallFont().deriveFont(Font.BOLD);
    }

    private Font axisTitleFont() {
        return baseSmallFont();
    }

    private Font labelFont() {
        return baseSmallFont();
    }

    private Font baseSmallFont() {
        Font font = UIManager.getFont("Elowbe.font.small");
        if (font == null) {
            font = getFont();
        }
        if (font == null) {
            font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
        }
        return font;
    }

    private Color axisNumberColor() {
        return PaintUtils.palette().cardForeground;
    }

    private Color lineColor() {
        return lineColor != null ? lineColor : PaintUtils.palette().primary;
    }

    private Color pointColor(ElowbeGraphPoint point) {
        if (point.getColor() != null) {
            return point.getColor();
        }
        return pointColor != null ? pointColor : lineColor();
    }

    private Color labelColor() {
        return labelColor != null ? labelColor : PaintUtils.palette().mutedForeground;
    }

    private static boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    private String formatValue(double value) {
        String text = valueFormatter.apply(value);
        return text == null ? "" : text;
    }

    private static String formatNumber(double value) {
        if (!Double.isFinite(value)) {
            return String.valueOf(value);
        }
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        String text = String.format(Locale.ROOT, "%.3f", value);
        while (text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        if (text.endsWith(".")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private ValueStats computeValueStats() {
        double max = points.stream().mapToDouble(ElowbeGraphPoint::getValue).max().orElse(1);
        double min = points.stream().mapToDouble(ElowbeGraphPoint::getValue).min().orElse(0);
        double avg = points.stream().mapToDouble(ElowbeGraphPoint::getValue).average().orElse(0);
        double range = max - min;
        if (range == 0) {
            range = 1;
        }
        return new ValueStats(min, max, avg, range);
    }

    private static final class ValueStats {
        private final double min;
        private final double max;
        private final double avg;
        private final double range;

        private ValueStats(double min, double max, double avg, double range) {
            this.min = min;
            this.max = max;
            this.avg = avg;
            this.range = range;
        }
    }

    // ---------------------------------------------------------------------
    // Interaction
    // ---------------------------------------------------------------------

    private int pointIndexAt(Point click) {
        if (points.isEmpty()) {
            return -1;
        }
        ChartLayout layout = computeLayout();
        ValueStats stats = computeValueStats();
        int plotTop = layout.top;
        int plotBottom = getHeight() - layout.bottom;
        int plotHeight = Math.max(1, plotBottom - plotTop);
        int plotLeft = layout.left;

        for (int i = 0; i < points.size(); i++) {
            int x = xFor(i, plotLeft);
            int y = yFor(points.get(i).getValue(), stats, plotTop, plotHeight);
            if (click.distance(x, y) <= hitRadius) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int index = pointIndexAt(event.getPoint());
        if (index < 0) {
            return null;
        }
        ElowbeGraphPoint point = points.get(index);
        String label = point.getLabel();
        return hasText(label) ? label : formatValue(point.getValue());
    }

    private void installMouseBehavior() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (!isEnabled()) {
                    return;
                }
                requestFocusInWindow();
                setSelectedPointIndex(pointIndexAt(event.getPoint()));
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                Cursor cursor = isEnabled() && pointIndexAt(event.getPoint()) >= 0
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor();
                if (!cursor.equals(getCursor())) {
                    setCursor(cursor);
                }
            }
        });
    }

    private void installKeyBindings() {
        bindSelectionKey("LEFT", -1);
        bindSelectionKey("RIGHT", 1);
    }

    private void bindSelectionKey(String key, int delta) {
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key), "select-" + key);
        getActionMap().put("select-" + key, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (!isEnabled() || points.isEmpty()) {
                    return;
                }
                int next;
                if (selectedPointIndex < 0) {
                    next = delta > 0 ? 0 : points.size() - 1;
                } else {
                    next = Math.max(0, Math.min(points.size() - 1, selectedPointIndex + delta));
                }
                setSelectedPointIndex(next);
            }
        });
    }
}
