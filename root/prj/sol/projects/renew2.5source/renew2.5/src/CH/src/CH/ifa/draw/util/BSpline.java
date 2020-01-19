// Time-stamp: <01/05/09 11:24:16 5duvigne>

/**
 * Calculates B-Splines and caches the computed results.
 * <p>
 * This class consists of two major parts:
 * <UL>
 * <LI>The static method {@link #calculateSpline} computes a
 *     B-Spline for a given vector of control points with
 *     specifiable degree and number of curve segments.
 *     </LI>
 * <LI>An instance of this class can be used to cache the
 *     computation results and automatically recompute them
 *     if changes are made to the vector of control points,
 *     degree or segment number.
 *     </LI>
 * </UL>
 * All calculations are carried out in double precision,
 * resulting in a vector of <code>DoublePoint</code>s.
 * But the caching <code>BSpline</code> instance can also
 * be queried for a vector of integer <code>Point</code>s.
 * </p>
 * BSpline.java
 * Created: Wed Nov  1  2000
 *
 * @author Friedrich Delgado Friedrichs, Lutz Kirsten, Klaus Mitreiter
 * @see DoublePoint
 * @see Point
 **/
package CH.ifa.draw.util;

import java.awt.Point;

import java.util.Vector;


public class BSpline {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(BSpline.class);

    /**
     * Default value to use for the number of segments.
     * Currently 15.
     **/
    public final static int DEFSEGMENTS = 15;

    /**
     * Default value to use for the spline's degree.
     * Currently 2.
     **/
    public final static int DEFDEGREE = 2;

    // -------------- instantiable part for result caching ----------------


    /**
     * Caches the last result of the point computation
     * as <code>DoublePoint</code> objects
     * (e.g. <code>double</code> values).
     **/
    private Vector<DoublePoint> curvepoints = null;

    /**
     * Caches the last result of the point computation
     * as <code>Point</code> objects
     * (e.g. <code>int</code> values).
     **/
    private Vector<Point> curvepointsInt = null;

    /**
     * Indicates wether the cached points in <code>curvepoints</code>
     * and <code>curvepointsInt</code> are still valid.
     **/
    private boolean curvepointsValid = false;

    /**
     * The current list of control points as
     * <code>Point</code> objects.
     **/
    private Vector<Point> controlpoints = null;

    /**
     * The number of subdivisions between two control points.
     **/
    private int segments;

    /**
     * The degree of the B-Spline.
     **/
    private int degree;

    /**
     * No-Arg-Constructor, not useful???
     **/
    public BSpline() {
    }

    /**
     * New B-Spline with 15 segments and degree of 2
     * @param points <code>Vector</code> of <code>Point</code>s
     */
    public BSpline(Vector<Point> points) {
        BSplineHelper(points, DEFSEGMENTS, DEFDEGREE);
    }

    public BSpline(Vector<Point> points, int segments) {
        BSplineHelper(points, segments, DEFDEGREE);
    }

    public BSpline(Vector<Point> points, int psegments, int pdegree) {
        BSplineHelper(points, psegments, pdegree);
    }

    private void BSplineHelper(Vector<Point> points, int psegments, int pdegree) {
        segments = psegments;
        degree = pdegree;
        setVector(points);
        invalidatePointCache();
    }

    /**
     * Set the number of segments
     */
    public void setSegments(int psegments) {
        if (segments != psegments) {
            segments = psegments;
            invalidatePointCache();
        }
    }

    /**
     * Set the degree of the B-Spline.
     */
    public void setDegree(int pdegree) {
        if (degree != pdegree) {
            degree = pdegree;
            invalidatePointCache();
        }
    }

    /**
     * Set the control points of the b-spline.
     *
     * @param points Vector of <code>Point</code> objects.
     * @see Point
     **/
    public void setPoints(Vector<Point> points) {
        // The following distinction addresses the
        // tradeoff between spline calculation and
        // vector comparision.
        // If the point cache is already invalid, we
        // can set the new control points without any
        // check (else-branch), but in the other case
        // we can save the spline calculation time if
        // the new point vector does not differ from
        // the old one. But the equality check takes
        // some time, too.
        if (isPointCacheValid()) {
            if (!theSame(points)) {
                setVector(points);
                invalidatePointCache();
            }
        } else {
            setVector(points);
        }
    }

    public int getSegments() {
        return segments;
    }

    public int getDegree() {
        return degree;
    }

    /**
     * Calculates a b-spline and returns the curve points.
     *
     * @return Vector of <code>Point</code>
     *
     * @see Point
     */
    public Vector<Point> getCurvepointsInt() {
        //NOTICERedundant
        updatePointCache();
        return curvepointsInt;
    }

    /**
     * Calculates a b-spline and returns the curve points.
     *
     * @return Vector of <code>DoublePoint</code>
     *
     * @see DoublePoint
     */
    public Vector<DoublePoint> getCurvepointsDouble() {
        //NOTICERedundant
        updatePointCache();
        return curvepoints;
    }

    public int lineSegment(int i) {
        return ((i - 1) / segments);
    }

    /**
     * Marks the point cache as invalid, so that the spline
     * has to be recomputed on the next query.
     **/
    private void invalidatePointCache() {
        curvepointsValid = false;
    }

    /**
     * Checks if the point cache is still valid and
     * initiates recalculation of the spline, if needed.
     **/
    private void updatePointCache() {
        if (!isPointCacheValid()) {
            curvepoints = calculateSpline(degree, segments, controlpoints);
            curvepointsInt = DoublePoint.convertDoublePointVector(curvepoints);
            curvepointsValid = true;
        }
    }

    /**
     * Returns wether the point cache is still valid.
     **/
    private boolean isPointCacheValid() {
        return curvepointsValid;
    }

    private void setVector(Vector<Point> vector) {
        Vector<Point> v = new Vector<Point>();
        int s = vector.size();
        for (int i = 0; i < s; i++) {
            v.addElement(new Point(vector.elementAt(i)));
        }


        //	oldcontrolpoints=v;
        // 	if (s < degree + 1) {
        // 	    for (int i=0; s<(degree-i+1); i++) {
        // 		v.addElement(new Point((Point)vector.lastElement()));
        // 	    }
        // 	}
        controlpoints = v;
        //logger.debug(v);
    }

    private boolean theSame(Vector<Point> vector) {
        Point p1;
        Point p2;
        if ((vector == null) || (controlpoints == null)) {
            return false;
        }
        if (vector.size() != controlpoints.size()) {
            return false;
        }
        for (int i = 0; i < vector.size(); i++) {
            p1 = vector.elementAt(i);
            p2 = controlpoints.elementAt(i);
            if (!p1.equals(p2)) {
                return false;
            }
        }
        return true;
    }

    // --------------- static methods for spline calculation ------------------


    /** Calculate the pointvector for a B-Spline.
    @param mydegree is the degree p of the spline,
    @param sections controls the subdivisions between two control points
    @param points is the vector of control <code>Point</code>s
    @return Vector of <code>DoublePoints</code> that make up the spline
    */
    public static Vector<DoublePoint> calculateSpline(int mydegree,
                                                      int sections,
                                                      Vector<Point> points) {
        int numcontrol = points.size();
        int degree = mydegree;
        if (numcontrol < degree + 1) {
            degree = numcontrol - 1;
        }
        if (numcontrol > 2) {
            int numpoints = (numcontrol - 1) * sections;
            Vector<DoublePoint> cpoints = new Vector<DoublePoint>();

            int m = numcontrol + degree;

            double[] us = knotvector(m, degree);
            double ustep = 1.0 / numpoints;
            double u;

            DoublePoint tmppoint;

            for (int i = 0; i < numpoints; ++i) {
                u = i * ustep;
                tmppoint = sppoint(u, degree, points, us);


                //logger.debug("U: "+u+" point: "+tmppoint);
                cpoints.addElement(tmppoint);
            }
            cpoints.addElement(new DoublePoint(points.lastElement()));
            return cpoints;
        } else {
            return DoublePoint.convertPointVector(points);
        }
    }

    private static double[] knotvector(int m, int p) {
        //return evenly spaced vector for a clamped curve of degree p
        int mb = m + 1;
        double[] us = new double[mb];
        int bound = m - (p * 2);
        for (int i = 0; i < bound; ++i) {
            us[i + p] = (1 / (double) bound) * i;
        }

        //logger.debug("m="+m+", p="+p+", bound="+bound+", mb="+mb);
        for (int i = 0; i < p + 1; ++i) {
            us[i] = 0.0;
            us[mb - 1 - i] = 1.0;
        }


        // 	String debugMsg = "us = [";
        // 	for (int i=0; i<mb; ++i) {
        // 	    debugMsg = debugMsg + us[i]+" ";
        // 	}
        //  debugMsg = debugMsg + "]";
        // 	logger.debug(debugMsg);
        return us;
    }

    private static double div0(double divident, double divisor) {
        /*
          if (divident == 0.0 && divisor == 0.0) {
          return 0.0;
          } else {
          return divident / divisor;
          }
        */
        if (divisor == 0.0) {
            return 0.0;
        } else {
            return divident / divisor;
        }
    }

    //http://www.cs.mtu.edu/~shene/COURSES/cs3621/NOTES/spline/bspline-basis.html
    private static double[] weightDyn(int mini, int maxi, int p, double[] us,
                                      double u) {
        double[] result = new double[maxi + 1 + p];
        for (int ii = mini; ii <= maxi + p; ii++) {
            if ((us[ii] <= u) && (u < us[ii + 1])) {
                result[ii] = 1.0;
            } else {
                result[ii] = 0.0;
            }
        }

        for (int pp = 1; pp <= p; pp++) {
            for (int ii = mini; ii <= maxi + p - pp; ii++) {
                result[ii] = div0((u - us[ii]) * result[ii],
                                  (us[ii + pp] - us[ii]))
                             + div0((us[ii + pp + 1] - u) * result[ii + 1],
                                    (us[ii + pp + 1] - us[ii + 1]));
            }
        }

        return result;
    }

    public static DoublePoint sppoint(double u, int p, Vector<Point> points,
                                      double[] us) {
        int n = points.size();

        double x = 0;
        double y = 0;
        double w;
        Point pt;

        int maxi = 0;
        for (int i = 1; i < n; ++i) {
            if (us[i] <= u) {
                maxi = i;
            }
        }
        int mini = maxi;
        for (int i = maxi; i >= 0; --i) {
            if (u < us[i + p + 1]) {
                mini = i;
            }
        }

        double[] weights = weightDyn(mini, maxi, p, us, u);

        for (int i = 0; i < n; ++i) {
            if (us[i] <= u && u < us[i + p + 1]) {
                w = weights[i];


                //logger.debug ("w("+i+","+p+","+u+")="+w);
                pt = points.elementAt(i);
                x += w * pt.x;
                y += w * pt.y;
            }
        }
        return new DoublePoint(x, y);
    }
} // BSpline
