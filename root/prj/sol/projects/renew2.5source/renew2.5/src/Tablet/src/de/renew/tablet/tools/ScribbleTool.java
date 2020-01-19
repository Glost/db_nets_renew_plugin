/*
 * @(#)ScribbleTool.java 5.1
 *
 */
package de.renew.tablet.tools;

import CH.ifa.draw.figures.PolyLineFigure;

import CH.ifa.draw.framework.DrawingEditor;

import CH.ifa.draw.standard.UndoableTool;

import java.awt.event.MouseEvent;


/**
 * Tool to scribble a PolyLineFigure
 * @see PolyLineFigure
 *
 * @author Lawrence Cabac
 */
public class ScribbleTool extends UndoableTool {
    private PolyLineFigure fScribble;
    private int fLastX;
    private int fLastY;

    public ScribbleTool(DrawingEditor editor) {
        super(editor);
    }

    public void activate() {
        super.activate();
        fScribble = null;
    }

    public void deactivate() {
        if (fScribble != null) {
            if (fScribble.size().width < 4 || fScribble.size().height < 4) {
                drawing().remove(fScribble);
                noChangesMade();
            }
        }
        fScribble = null;
        super.deactivate();
    }

    private void point(int x, int y) {
        if (fScribble == null) {
            fScribble = new PolyLineFigure(x, y);
            view().add(fScribble);
            changesMade();
        } else if (fLastX != x || fLastY != y) {
            fScribble.addPoint(x, y);
        }

        fLastX = x;
        fLastY = y;
    }

    public void mouseDown(MouseEvent e, int x, int y) {
        if (e.getClickCount() >= 2) {
            editor().toolDone();
        } else {
            // use original event coordinates to avoid
            // supress that the scribble is constrained to
            // the grid
            point(e.getX(), e.getY());
        }
    }

    public void mouseDrag(MouseEvent e, int x, int y) {
        if (fScribble != null) {
            point(e.getX(), e.getY());
        }
    }

    public void mouseUp(MouseEvent e, int x, int y) {
        //smoothPoints(fScribble); does not work
        fScribble = null;
    }

//    /**
//     * Remove points that are nearly colinear with others
//     **/
//    public void smoothPoints(PolyLineFigure polyline) {
//        polyline.willChange();
//        boolean removed = false;
//        Enumeration<Point> points = polyline.points();
//        int j = 0;
//        Point[] pointL = new Point[2000];
//        int[] xpoints = new int[2000];
//        int[] ypoints = new int[2000];
//        while (points.hasMoreElements()) {
//            Point point = (Point) points.nextElement();
//            pointL[j] = point;
//            xpoints[j] = point.x;
//            ypoints[j] = point.y;
//            j++;
//        }
//        int npoints = j;
//        int n = npoints;
//        do {
//            removed = false;
//            int i = 0;
//            while (i < n && n >= 3) {
//                int nxt = (i + 1) % n;
//                int prv = (i - 1 + n) % n;
//                String strategy = DrawPlugin.getCurrent().getProperties()
//                                            .getProperty(PolygonFigure.SMOOTHINGSTRATEGY);
//                boolean doremove = false;
//                if (strategy == null || strategy.equals("")
//                            || PolygonFigure.SMOOTHING_INLINE.equals(strategy)) {
//                    if ((PolygonFigure.distanceFromLine(xpoints[prv],
//                                                                ypoints[prv],
//                                                                xpoints[nxt],
//                                                                ypoints[nxt],
//                                                                xpoints[i],
//                                                                ypoints[i]) < PolygonFigure.TOO_CLOSE)) {
//                        doremove = true;
//                    }
//                } else if (PolygonFigure.SMOOTHING_DISTANCES.equals(strategy)) {
//                    if (Math.abs(xpoints[prv] - xpoints[i]) < 5
//                                && Math.abs(ypoints[prv] - ypoints[i]) < 5) {
//                        doremove = true;
//                    }
//                }
//                if (doremove) {
//                    removed = true;
//                    --n;
//                    polyline.removePointAt(i);
//                } else {
//                    ++i;
//                }
//            }
//        } while (removed);
////        if (n != npoints) {
////            polyline = new Polygon(polyline.xpoints, polyline.ypoints, n);
////        }
//        polyline.changed();
//    }
}