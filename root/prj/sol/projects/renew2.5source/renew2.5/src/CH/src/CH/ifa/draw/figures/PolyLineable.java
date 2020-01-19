package CH.ifa.draw.figures;

import CH.ifa.draw.framework.Figure;

import java.awt.Point;


public interface PolyLineable extends Figure {
    public final static int LINE_SHAPE = 0;
    public final static int BSPLINE_SHAPE = 1;

    /**
     * Insert a node at the given point.
     */
    public abstract void insertPointAt(Point p, int i);

    public abstract void removePointAt(int i);

    public abstract void setPointAt(Point p, int i);

    public abstract int pointCount();

    public abstract Point pointAt(int i);
}