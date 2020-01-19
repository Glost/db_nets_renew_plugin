package de.renew.gui;

import java.awt.Dimension;
import java.awt.Rectangle;


/**
 * Layouter that uses the {@link GraphLayout} class with simulated annealing.
 *
 * @author Michael Simon
 */
public class AnnealingGraphLayout {
    public int LAYOUT_OFFSET = 65536;
    public double SPRING_LENGTH = 1.0 * 4 * 0.25;
    public double SPRING_STRENGTH = 0.1 * 4 * 0.125;
    public double REPULSION_DISTANCE = 200.0 * 4 * 0.5;
    public double REPULSION_STRENGTH = 0.5 * 4 * 1.0;
    public double TORQUE_STRENGTH = 0.25 * 4 * 0.25;
    public double FRICTION = 0.25;
    public int STEPS = 2048;

    public GraphLayout annealingLayout(LayoutableDrawing drawing) {
        GraphLayout layout = new GraphLayout();
        drawing.fillInGraph(layout);

        randomInit(layout, drawing.defaultSize());
        anneal(layout);
        moveToTopLeft(layout, drawing.displayBox());

        return layout;
    }

    /**
     * Add some offset so the layout algorithm is not hindered.
     * It gets removed by {@link #moveToTopLeft} later.
     */
    private void randomInit(GraphLayout layout, Dimension windowSize) {
        layout.randomInit(LAYOUT_OFFSET, LAYOUT_OFFSET, windowSize.width,
                          windowSize.height);
    }

    private void anneal(GraphLayout layout) {
        layout.LENGTH_FACTOR = SPRING_LENGTH;
        layout.REPULSION_LIMIT = REPULSION_DISTANCE;
        layout.TORQUE_STRENGTH = TORQUE_STRENGTH;

        for (int i = 0; i < STEPS; i++) {
            double progress = ((double) i) / STEPS;
            double annealingFactor = 1 + (1 - progress) * 10;

            layout.SPRING_STRENGTH = annealingFactor * SPRING_STRENGTH;
            layout.REPULSION_STRENGTH = annealingFactor * REPULSION_STRENGTH;
            layout.FRICTION_FACTOR = 1 - progress * FRICTION;
            layout.relax();
        }
    }

    private static void moveToTopLeft(GraphLayout layout, Rectangle box) {
        layout.moveBy(-box.x, -box.y);
    }
}