package com.elowbe.laf;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import com.elowbe.laf.theme.ElowbeDefaults;
import com.elowbe.laf.theme.ElowbePalette;
import com.elowbe.laf.util.PaintUtils;

public class ElowbeRangeSlider extends JComponent {
    private static final long serialVersionUID = 1L;
    private static final int THUMB_SIZE = 18;
    private static final int TRACK_HEIGHT = 5;

    private final EventListenerList listeners = new EventListenerList();
    private int minimum;
    private int maximum;
    private int lowerValue;
    private int upperValue;
    private DragTarget dragTarget = DragTarget.NONE;

    public ElowbeRangeSlider() {
        this(0, 100, 20, 80);
    }

    public ElowbeRangeSlider(int minimum, int maximum, int lowerValue, int upperValue) {
        if (maximum <= minimum) {
            throw new IllegalArgumentException("maximum must be greater than minimum");
        }
        this.minimum = minimum;
        this.maximum = maximum;
        this.lowerValue = clamp(lowerValue, minimum, maximum);
        this.upperValue = clamp(upperValue, this.lowerValue, maximum);
        setOpaque(false);
        setFocusable(true);
        setPreferredSize(new Dimension(240, 36));
        setMinimumSize(new Dimension(120, 36));
        installInteraction();
    }

    public int getMinimum() {
        return minimum;
    }

    public int getMaximum() {
        return maximum;
    }

    public int getLowerValue() {
        return lowerValue;
    }

    public int getUpperValue() {
        return upperValue;
    }

    public void setMinimum(int minimum) {
        setRange(minimum, maximum, lowerValue, upperValue);
    }

    public void setMaximum(int maximum) {
        setRange(minimum, maximum, lowerValue, upperValue);
    }

    public void setLowerValue(int value) {
        setValues(value, upperValue);
    }

    public void setUpperValue(int value) {
        setValues(lowerValue, value);
    }

    public void setValues(int lowerValue, int upperValue) {
        int nextLower = clamp(lowerValue, minimum, maximum);
        int nextUpper = clamp(upperValue, nextLower, maximum);
        if (this.lowerValue == nextLower && this.upperValue == nextUpper) {
            return;
        }
        this.lowerValue = nextLower;
        this.upperValue = nextUpper;
        repaint();
        fireStateChanged();
    }

    public void setRange(int minimum, int maximum, int lowerValue, int upperValue) {
        if (maximum <= minimum) {
            throw new IllegalArgumentException("maximum must be greater than minimum");
        }
        int nextLower = clamp(lowerValue, minimum, maximum);
        int nextUpper = clamp(upperValue, nextLower, maximum);
        boolean changed = this.minimum != minimum || this.maximum != maximum
                || this.lowerValue != nextLower || this.upperValue != nextUpper;
        this.minimum = minimum;
        this.maximum = maximum;
        this.lowerValue = nextLower;
        this.upperValue = nextUpper;
        if (changed) {
            repaint();
            fireStateChanged();
        }
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(ChangeListener.class, Objects.requireNonNull(listener, "listener"));
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(ChangeListener.class, listener);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = PaintUtils.prepare(graphics);
        try {
            ElowbePalette palette = PaintUtils.palette();
            Rectangle track = trackBounds();
            int lowerX = xForValue(lowerValue);
            int upperX = xForValue(upperValue);

            PaintUtils.fillRound(g2, track.x, track.y, track.width, track.height,
                    ElowbeDefaults.RADIUS_PILL, palette.secondary);
            PaintUtils.fillRound(g2, lowerX, track.y, Math.max(TRACK_HEIGHT, upperX - lowerX), track.height,
                    ElowbeDefaults.RADIUS_PILL, palette.primary);
            paintThumb(g2, lowerX, palette);
            paintThumb(g2, upperX, palette);
            PaintUtils.paintFocusRing(g2, this, ElowbeDefaults.RADIUS_MD);
        } finally {
            g2.dispose();
        }
    }

    private void paintThumb(Graphics2D g2, int centerX, ElowbePalette palette) {
        int x = centerX - THUMB_SIZE / 2;
        int y = getHeight() / 2 - THUMB_SIZE / 2;
        g2.setColor(palette.card);
        g2.fillOval(x, y, THUMB_SIZE - 1, THUMB_SIZE - 1);
        g2.setColor(palette.primary);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x, y, THUMB_SIZE - 1, THUMB_SIZE - 1);
    }

    private void installInteraction() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                requestFocusInWindow();
                dragTarget = nearestTarget(event.getX());
                updateValue(event.getX());
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                updateValue(event.getX());
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                dragTarget = DragTarget.NONE;
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                int step = 1;
                if (event.getKeyCode() == KeyEvent.VK_LEFT) {
                    setValues(lowerValue - step, upperValue);
                    event.consume();
                } else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
                    setValues(lowerValue + step, upperValue);
                    event.consume();
                } else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
                    setValues(lowerValue, upperValue - step);
                    event.consume();
                } else if (event.getKeyCode() == KeyEvent.VK_UP) {
                    setValues(lowerValue, upperValue + step);
                    event.consume();
                }
            }
        });
    }

    private DragTarget nearestTarget(int x) {
        int lowerX = xForValue(lowerValue);
        int upperX = xForValue(upperValue);
        return Math.abs(x - lowerX) <= Math.abs(x - upperX) ? DragTarget.LOWER : DragTarget.UPPER;
    }

    private void updateValue(int x) {
        int value = valueForX(x);
        if (dragTarget == DragTarget.LOWER) {
            setValues(value, upperValue);
        } else if (dragTarget == DragTarget.UPPER) {
            setValues(lowerValue, value);
        }
    }

    private Rectangle trackBounds() {
        int inset = THUMB_SIZE / 2 + 2;
        int y = getHeight() / 2 - TRACK_HEIGHT / 2;
        return new Rectangle(inset, y, Math.max(1, getWidth() - inset * 2), TRACK_HEIGHT);
    }

    private int xForValue(int value) {
        Rectangle track = trackBounds();
        double percent = (value - minimum) / (double) (maximum - minimum);
        return track.x + (int) Math.round(track.width * percent);
    }

    private int valueForX(int x) {
        Rectangle track = trackBounds();
        double percent = (clamp(x, track.x, track.x + track.width) - track.x) / (double) track.width;
        return minimum + (int) Math.round((maximum - minimum) * percent);
    }

    private void fireStateChanged() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners.getListeners(ChangeListener.class)) {
            listener.stateChanged(event);
        }
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private enum DragTarget {
        NONE,
        LOWER,
        UPPER
    }
}
