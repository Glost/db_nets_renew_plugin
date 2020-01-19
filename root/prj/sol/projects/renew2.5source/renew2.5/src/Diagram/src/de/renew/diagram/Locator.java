/*
 * Created on Apr 28, 2003
 */
package de.renew.diagram;

import CH.ifa.draw.framework.Figure;

import java.io.Serializable;

import java.util.Iterator;
import java.util.Vector;


/**
 * @author cabac
 *
 */


/**
 * Holds a <code>Vector</code> of <code>Figures</code> of a <code>DiagramDrawing</code>
 * and returns the figures that are located next to (north of) a given position.
 * @see de.renew.diagram.drawing.DiagramDrawing
 * @author Lawrence Cabac
 */
public class Locator implements Serializable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(Locator.class);
    /**
     * Comment for <code>figures</code>.
     * @serial
     */
    Vector<Figure> figures;

    /**
     * Creates a new Locator.
     */
    public Locator() {
        super();
        figures = new Vector<Figure>();
    }

    /**
     * Adds a <code>Figure</code> to the Locator.
     * Dublicates are being ignored.
     * @param fig
     */
    public void add(Figure fig) {
        if (!figures.contains(fig)) {
            figures.add(fig);
        }
    }

    /**
     * Removes a <code>Figure</code> from the Locator.
     * @param fig
     */
    public void remove(Figure fig) {
        figures.remove(fig);
    }

    //    /**
    //     * @param x
    //     * @return
    //     */
    //    public Figure getElementAtPosition(int x) {
    //        Figure ret = null;
    //        Iterator it = figures.iterator();
    //        while (it.hasNext()) {
    //            Figure figure = (Figure) it.next();
    //            if (holds(figure, x)) {
    //                ret = figure;
    //            }
    //        }
    //        return ret;
    //    }


    /**
     * Returns a <code>Figure</code> located above (north) of a given position.
     * @param x coordinate of the position
     * @param y coordinate of the position
     * @return the lowest (most south) figure that is located above (north) of the given position
     */
    public Figure getElementAtPosition(int x, int y) {
        Figure ret = null;
        Iterator<Figure> it = figures.iterator();
        while (it.hasNext()) {
            Figure figure = it.next();
            if (holds(figure, x) && over(figure, y)) {
                ret = figure;
            }
        }
        return ret;
    }

    private static boolean over(Figure fig, int y) {
        return (y >= fig.displayBox().y + fig.displayBox().height);
    }

    private static boolean holds(Figure fig, int x) {
        return (x >= getStart(fig) && x <= getEnd(fig));
    }

    private static int getEnd(Figure figure) {
        return figure.displayBox().x + figure.displayBox().width;
    }

    private static int getStart(Figure figure) {
        return figure.displayBox().x;
    }

    /*
     * print debug information
     */
    public void show() {
        Iterator<Figure> it = figures.iterator();
        while (it.hasNext()) {
            logger.debug("" + it.next());
        }
    }
}