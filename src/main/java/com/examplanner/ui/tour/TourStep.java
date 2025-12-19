package com.examplanner.ui.tour;

import javafx.scene.Node;

/**
 * Represents a single step in the guided tour.
 */
public record TourStep(
        Node targetNode,
        String title,
        String description,
        TourPosition position) {
    public enum TourPosition {
        TOP, BOTTOM, LEFT, RIGHT, CENTER
    }
}
