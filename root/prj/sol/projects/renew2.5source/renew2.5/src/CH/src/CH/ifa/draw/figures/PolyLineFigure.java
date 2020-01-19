/*
 * @(#)PolyLineFigure.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.framework.Locator;

import CH.ifa.draw.util.BSpline;
import CH.ifa.draw.util.DoublePoint;
import CH.ifa.draw.util.Geom;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import de.renew.util.ClassSource;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import java.io.IOException;

import java.util.Enumeration;
import java.util.Vector;


/**
 * A poly line figure consists of a list of points.
 * It has an optional line decoration at the start and end.
 *
 * @see LineDecoration
 */


// The write method assumes that PolyLineFigure inherits
// directly from AttributeFigure.
public class PolyLineFigure extends AttributeFigure implements PolyLineable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PolyLineFigure.class);
    public final static int ARROW_TIP_NONE = 0;
    public final static int ARROW_TIP_START = 1;
    public final static int ARROW_TIP_END = 2;
    public final static int ARROW_TIP_BOTH = 3;
    public final static int LINE_SHAPE = 0;
    public final static int BSPLINE_SHAPE = 1;

    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -7951352179906577773L;
    private static Integer BSPLINE_SHAPE_OBJ = new Integer(BSPLINE_SHAPE);

    /**
     * List of control points which determine the form
     * of the polyline figure.
     * @serial
     **/
    protected Vector<Point> fPoints;

    /**
     * Helper object which transforms a vector of control
     * points into a vector of drawing points. Initially
     * <code>null</code>, will be created when needed.
     * <p>
     * This field is transient because it is sufficient
     * to serialize the control points in <code>fPoints</code>
     * along with the spline-related attributes.
     * </p>
     **/
    protected transient BSpline spline = null;

    /**
     * Line decoration (like an arrow tip, for example)
     * at the start of the line. May be <code>null</code>.
     * @serial
     **/
    protected LineDecoration fStartDecoration = null;

    /**
     * Line decoration (like an arrow tip, for example)
     * at the end of the line. May be <code>null</code>.
     * @serial
     **/
    protected LineDecoration fEndDecoration = null;

    /**
     * Determines the type of decoration to be added by the
     * <code>setStart</code>- or -<code>EndDecoration()</code>
     * methods. Defaults to {@link ArrowTip}.
     * @serial
     **/
    protected Class<?> fArrowTipClass = ArrowTip.class;
    @SuppressWarnings("unused")
    private int polyLineFigureSerializedDataVersion = 1;

    public PolyLineFigure() {
        fPoints = new Vector<Point>(4);
    }

    public PolyLineFigure(int size) {
        fPoints = new Vector<Point>(size);
    }

    public PolyLineFigure(int x, int y) {
        fPoints = new Vector<Point>();
        fPoints.addElement(new Point(x, y));
    }

    private boolean drawAsSpline() {
        Object line_shape = getAttribute("LineShape");
        return BSPLINE_SHAPE_OBJ.equals(line_shape);
    }

    //NOTICEredundant
    private Vector<Point> drawPointsVector() {
        if (drawAsSpline()) {
            if (spline == null) {
                // We only need to set the degree, segments and points in
                // this case since this should be done immediately when the
                // parameter change occurs (e.g. in setAttribute or
                // insert/delete/movePoint).
                updateSpline();
            }
            return spline.getCurvepointsInt();
        }
        return fPoints;
    }

    //NOTICEredundant
    private Vector<DoublePoint> drawDoublePointsVector() {
        if (drawAsSpline()) {
            if (spline == null) {
                // We only need to set the degree, segments and points in
                // this case since this should be done immediately when the
                // parameter change occurs (e.g. in setAttribute or
                // insert/delete/movePoint).
                updateSpline();
            }
            return spline.getCurvepointsDouble();
        }
        return DoublePoint.convertPointVector(fPoints);
    }

    private void updateSpline() {
        Object spline_degree = getAttribute("BSplineDegree");
        Object spline_segments = getAttribute("BSplineSegments");
        spline = new BSpline(fPoints, ((Integer) spline_segments).intValue(),
                             ((Integer) spline_degree).intValue());
    }

    /**
     * returns a rectangle which contains the drawn figure as
     * well as the handles
     */
    public Rectangle displayBox() {
        Enumeration<Point> k = points();
        Rectangle r = new Rectangle(k.nextElement());

        while (k.hasMoreElements()) {
            r.add(k.nextElement());
        }

        return r;
    }

    public boolean isEmpty() {
        return (size().width < 3) && (size().height < 3);
    }

    /**
     * Adds to the given vector a <code>PolylineHandle</code>
     * for each intermediate control point in the line and a
     * <code>InsertPointHandle</code> for each line segment.
     * Start and end control points are not included!
     **/
    protected void addIntermediateHandles(Vector<Handle> handles) {
        for (int i = 1; i < fPoints.size() - 1; i++) {
            handles.addElement(new PolyLineHandle(this, locator(i), i));
        }
        for (int i = 0; i < fPoints.size() - 1; i++) {
            handles.addElement(new InsertPointHandle(this, i));
        }
    }

    /**
     * Returns a set of handles: <br>
     * - one handle for each control point, including start and end <br>
     * - one handle in the middle of each segment to add new points.
     **/
    public Vector<Handle> handles() {
        int n = fPoints.size();
        Vector<Handle> handles = new Vector<Handle>(2 * n);
        handles.addElement(new PolyLineHandle(this, locator(0), 0));
        addIntermediateHandles(handles);
        handles.addElement(new PolyLineHandle(this, locator(n - 1), n - 1));
        return handles;
    }

    public void basicDisplayBox(Point origin, Point corner) {
    }

    /**
     * Adds a node to the list of points.
     */
    public void addPoint(int x, int y) {
        fPoints.addElement(new Point(x, y));
        if (spline != null) {
            spline.setPoints(fPoints);
        }
        changed();
    }

    public Enumeration<Point> points() {
        return fPoints.elements();
    }

    public int pointCount() {
        return fPoints.size();
    }

    protected void basicMoveBy(int dx, int dy) {
        Enumeration<Point> k = fPoints.elements();
        while (k.hasMoreElements()) {
            k.nextElement().translate(dx, dy);
        }
        if (spline != null) {
            spline.setPoints(fPoints);
        }
    }

    /**
     * Changes the position of a node.
     */
    public void setPointAt(Point p, int i) {
        willChange();
        fPoints.setElementAt(p, i);
        if (spline != null) {
            spline.setPoints(fPoints);
        }
        changed();
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.figures.PolyLineable#insertPointAt(java.awt.Point, int)
     */
    public void insertPointAt(Point p, int i) {
        fPoints.insertElementAt(p, i);
        if (spline != null) {
            spline.setPoints(fPoints);
        }
        changed();
    }

    public void removePointAt(int i) {
        // Don't remove the last two points.
        if (fPoints.size() > 2) {
            willChange();
            fPoints.removeElementAt(i);
            if (spline != null) {
                spline.setPoints(fPoints);
            }
            changed();
        }
    }

    /**
     * Splits the segment at the given point if a segment was hit.
     * @return the index of the segment or 0 if no segment was hit.
     */
    public int splitSegment(int x, int y) {
        int i = findSegment(x, y);
        if (i != -1) {
            insertPointAt(new Point(x, y), i + 1);
        }

        // logger.debug("splitsegment returns: "+(i+1));
        return i + 1;
    }

    public Point pointAt(int i) {
        return fPoints.elementAt(i);
    }

    /**
     * Joins to segments into one if the given point hits a node
     * of the polyline.
     * @return true if the two segments were joined.
     */
    public boolean joinSegments(int x, int y) {
        for (int i = 1; i < fPoints.size() - 1; i++) {
            Point p = pointAt(i);
            if (Geom.length(x, y, p.x, p.y) < 3) {
                removePointAt(i);
                return true;
            }
        }
        return false;
    }

    public Connector connectorAt(int x, int y) {
        return new PolyLineConnector(this);
    }

    /**
     * Sets the start decoration.
     */
    public void setStartDecoration(LineDecoration l) {
        fStartDecoration = l;
    }

    /**
     * Sets the default start decoration.
     */
    protected void setStartDecoration() {
        try {
            setStartDecoration((LineDecoration) fArrowTipClass.newInstance());
        } catch (Exception e) {
            logger.error("Could not set arrow tip:\n" + e);
        }
    }

    /**
     * Sets the end decoration.
     */
    public void setEndDecoration(LineDecoration l) {
        fEndDecoration = l;
    }

    /**
     * Sets the default end decoration.
     */
    protected void setEndDecoration() {
        try {
            setEndDecoration((LineDecoration) fArrowTipClass.newInstance());
        } catch (Exception e) {
            logger.error("Could not set arrow tip:\n" + e);
        }
    }

    public void drawFrame(Graphics g) {
        Vector<DoublePoint> drawPoints = drawDoublePointsVector();
        int size = drawPoints.size();


        double[] xPoints = new double[size];
        double[] yPoints = new double[size];


        for (int i = 0; i < size; i++) {
            DoublePoint p = (drawPoints.elementAt(i));
            xPoints[i] = p.x;
            yPoints[i] = p.y;
        }


        GeneralPath shape = new GeneralPath();
        int i = 0;

        //TODO We have to cast to float here as Java 1.5 only supports float
        //Remove the cast when we switch to Java 1.6
        shape.moveTo((float) xPoints[i], (float) yPoints[i]);
        while (i < size) {
            shape.lineTo((float) xPoints[i], (float) yPoints[i]);
            i++;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        Integer width = (Integer) getAttribute(LINE_WIDTH_KEY);
        BasicStroke str = (BasicStroke) g2.getStroke();
        if (width != null) {
            //}else{
//            g2.setStroke(new BasicStroke((float)width.intValue()));
            BasicStroke s = new BasicStroke(width, str.getEndCap(),
                                            str.getLineJoin(),
                                            str.getMiterLimit(),
                                            str.getDashArray(),
                                            str.getDashPhase());
            g2.setStroke(s);
        }
        g2.draw(shape);
//        Stroke stroke = g2.getStroke();
        if (width == null) {
            g2.setStroke(new BasicStroke());
        } else {
            g2.setStroke(new BasicStroke(width));
        }
        decorate(g);
        g2.setStroke(str);
    }

    public boolean containsPoint(int x, int y) {
        Rectangle bounds = displayBox();
        bounds.grow(4, 4);
        if (!bounds.contains(x, y)) {
            return false;
        }

        Vector<Point> drawPoints = drawPointsVector();
        Point p1;
        Point p2;
        for (int i = 0; i < drawPoints.size() - 1; i++) {
            p1 = (drawPoints.elementAt(i));
            p2 = (drawPoints.elementAt(i + 1));
            if (Geom.lineContainsPoint(p1.x, p1.y, p2.x, p2.y, x, y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return line segment number i. In case of a spline, the returned
     * value indicates a segment between two handles.
     */
    private int lineSegment(int i) {
        if (drawAsSpline()) {
            return spline.lineSegment(i);
        } else {
            return i;
        }
    }

    /**
     * Gets the segment of the polyline that is hit by
     * the given point.
     * @return the index of the segment or -1 if no segment was hit.
     */
    public int findSegment(int x, int y) {
        Vector<Point> drawPoints = drawPointsVector();
        Point p1;
        Point p2;
        for (int i = 0; i < drawPoints.size() - 1; i++) {
            p1 = (drawPoints.elementAt(i));
            p2 = (drawPoints.elementAt(i + 1));
            if (Geom.lineContainsPoint(p1.x, p1.y, p2.x, p2.y, x, y)) {
                // logger.debug("findsegment: i="+i);
                return lineSegment(i);
            }
        }
        return -1;
    }

    private void decorate(Graphics g) {
        if (fStartDecoration != null) {
            Point p1 = fPoints.elementAt(0);
            Point p2 = fPoints.elementAt(1);
            fStartDecoration.draw(g, p1.x, p1.y, p2.x, p2.y, getFillColor(),
                                  getFrameColor());
        }
        if (fEndDecoration != null) {
            Point p3 = fPoints.elementAt(fPoints.size() - 2);
            Point p4 = fPoints.elementAt(fPoints.size() - 1);
            fEndDecoration.draw(g, p4.x, p4.y, p3.x, p3.y, getFillColor(),
                                getFrameColor());
        }
    }

    /**
     * Gets the attribute with the given name.
     * PolyLineFigure maps "ArrowMode" and "ArrowTip" to a
     * line decoration.
     */
    public Object getAttribute(String name) {
        if (name.equals("ArrowMode")) {
            int value = 0;
            if (fStartDecoration != null) {
                value |= ARROW_TIP_START;
            }
            if (fEndDecoration != null) {
                value |= ARROW_TIP_END;
            }
            return new Integer(value);
        } else if (name.equals("ArrowTip")) {
            return fArrowTipClass.getName();
        }
        return super.getAttribute(name);
    }

    /**
     * Sets the attribute with the given name.
     * PolyLineFigure interprets "ArrowMode" and "ArrowTip" to set
     * the line decoration.
     */
    public void setAttribute(String name, Object value) {
        // Reasons for the case distinction in this method:
        // - ArrowMode and ArrowTip
        //     are not stored in the usual attribute management
        //     of the superclass, but interpreted directly and
        //     applied to the appropriate fields.
        // - LineStyle 
        //     is checked for validity before it is stored by
        //     the usual attribute mechanism.
        // - BSplineDegree and BSplineSegments
        //     are stored the normal way, but the caching spline
        //     object must be informed immediately, too.
        if (name.equals("ArrowMode")) {
            Integer intObj = (Integer) value;
            if (intObj != null) {
                int decoration = intObj.intValue();

                if ((decoration & ARROW_TIP_START) != 0) {
                    setStartDecoration();
                } else {
                    setStartDecoration(null);
                }

                if ((decoration & ARROW_TIP_END) != 0) {
                    setEndDecoration();
                } else {
                    setEndDecoration(null);
                }
            }
        } else if (name.equals("ArrowTip")) {
            String decorationClass = (String) value;
            try {
                fArrowTipClass = ClassSource.classForName(decorationClass);
                if (fStartDecoration != null) {
                    setStartDecoration();
                }
                if (fEndDecoration != null) {
                    setEndDecoration();
                }
            } catch (ClassNotFoundException e) {
                logger.error("Trying to use " + decorationClass
                             + " for arrow tips:\n" + e);
            }
        } else if (name.equals("LineStyle")) {
            String lineStyle = (String) value;
            if (value != null) {
                super.setAttribute("LineStyle", lineStyle);
            }
        } else {
            if (spline != null) {
                if (name.equals("BSplineDegree")) {
                    int degree = ((Integer) value).intValue();
                    spline.setDegree(degree);
                } else if (name.equals("BSplineSegments")) {
                    int segments = ((Integer) value).intValue();
                    spline.setSegments(segments);
                }
            }
            super.setAttribute(name, value);
        }


        // To be safe, I always claim that I have changed.
        changed();
    }

    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeInt(fPoints.size());
        Enumeration<Point> k = fPoints.elements();
        while (k.hasMoreElements()) {
            Point p = k.nextElement();
            dw.writeInt(p.x);
            dw.writeInt(p.y);
        }
        dw.writeStorable(fStartDecoration);
        dw.writeStorable(fEndDecoration);
        dw.writeString(fArrowTipClass.getName());
    }

    public void read(StorableInput dr) throws IOException {
        if (dr.getVersion() >= 1) {
            // We are reading a recent file where
            // attributes have been saved.
            super.read(dr);
        }

        int size = dr.readInt();
        fPoints = new Vector<Point>(size);
        for (int i = 0; i < size; i++) {
            int x = dr.readInt();
            int y = dr.readInt();
            fPoints.addElement(new Point(x, y));
        }
        fStartDecoration = (LineDecoration) dr.readStorable();
        fEndDecoration = (LineDecoration) dr.readStorable();

        if (dr.getVersion() >= 8) {
            String arrowName = dr.readString();
            setAttribute("ArrowTip", arrowName);
        }

        if (dr.getVersion() == 0) {
            // We are reading an ancient file where the
            // color was saved explicitly.
            Color color = dr.readColor();
            setAttribute("FrameColor", color);
        }
    }

    /**
     * Creates a locator for the point with the given index.
     */
    public static Locator locator(int pointIndex) {
        return new PolyLineLocator(pointIndex);
    }
}