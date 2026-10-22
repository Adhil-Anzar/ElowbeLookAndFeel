package com.elowbe.laf.graph;

/**
 * Notified when the selected point of an {@link Elowbe2DGraph} changes.
 */
public interface PointSelectionListener {
    /**
     * Called when a point becomes selected.
     */
    void pointSelected(ElowbeGraphPoint point);

    /**
     * Called when the selection is cleared, for example by clicking an empty
     * area of the graph. The default implementation does nothing.
     */
    default void pointSelectionCleared() {
    }
}
