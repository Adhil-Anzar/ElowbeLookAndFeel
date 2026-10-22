package com.elowbe.laf.graph;

import java.awt.Color;

/**
 * A single data point in an {@link Elowbe2DGraph}.
 *
 * <p>Each point has a numeric {@link #getValue() value} that determines its
 * position on the Y axis and an optional label that is drawn next to it. A
 * point may also carry its own {@link #getColor() color}; when no color is set
 * the graph's point color is used.</p>
 */
public class ElowbeGraphPoint {
    private double value;
    private String label;
    private Color color;

    public ElowbeGraphPoint() {
    }

    public ElowbeGraphPoint(double value) {
        this.value = value;
    }

    public ElowbeGraphPoint(double value, String label) {
        this.value = value;
        this.label = label;
    }

    public ElowbeGraphPoint(double value, String label, Color color) {
        this.value = value;
        this.label = label;
        this.color = color;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Per-point color override, or {@code null} to use the graph's point color.
     */
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @deprecated Use {@link #getValue()}; graphs are not limited to weights.
     */
    @Deprecated
    public double getWeight() {
        return getValue();
    }

    /**
     * @deprecated Use {@link #setValue(double)}; graphs are not limited to weights.
     */
    @Deprecated
    public void setWeight(double weight) {
        setValue(weight);
    }
}
