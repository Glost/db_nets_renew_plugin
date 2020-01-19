/*
 * @(#)FigureChangeEvent.java 5.1
 *
 */
package CH.ifa.draw.framework;

import java.awt.Rectangle;

import java.util.EventObject;


/**
 * FigureChange event passed to FigureChangeListeners.
 *
 */
public class FigureChangeEvent extends EventObject {
    private static final Rectangle fgEmptyRectangle = new Rectangle(0, 0, 0, 0);
    private Rectangle fRectangle;

    /**
     * Constructs an event for the given source Figure. The rectangle is the
     * area to be invalidated.
     * @param source the object on which the Event initially occurred.
     * @param r the area to be invalidated.
     */
    public FigureChangeEvent(Figure source, Rectangle r) {
        super(source);
        fRectangle = r;
    }

    /**
     * Constructs an event for the given source Figure.
     *
     * @param source the object on which the Event initially occurred.
     */
    public FigureChangeEvent(Figure source) {
        super(source);
        fRectangle = fgEmptyRectangle;
    }

    /**
     *  Gets the changed figure
     * @return the changed figure.
     */
    public Figure getFigure() {
        return (Figure) getSource();
    }

    /**
     *  Gets the changed rectangle
     * @return the invalidated rectangle
     */
    public Rectangle getInvalidatedRectangle() {
        return fRectangle;
    }
}