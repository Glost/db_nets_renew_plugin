package CH.ifa.draw.standard;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureFilter;
import CH.ifa.draw.framework.UndoableCommand;

import CH.ifa.draw.util.Geom;

import java.awt.Point;
import java.awt.Rectangle;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * Spread a selection of figures e.g. make their relative distances
 * equal. The outmost figures (according to the selected operation
 * mode) serve as anchors, all other figures are distributed evemly
 * while respecting their left-to-right or top-to-bottom order.
 * <p>
 * <code>ConnectionFigure</code> and its descendants are ignored
 * in the computation as they cannot be repositioned explicitly.
 * </p>
 * SpreadCommand.java
 * Created: Mon May 22   2000
 * @author Michael Duvigneau
 */
public class SpreadCommand extends UndoableCommand {

    /**
     * Operation mode: equalize distance between left sides
     */
    public final static int LEFTS = 0;

    /**
     * Operation mode: equalize distance between centers (horizontally)
     */
    public final static int CENTERS = 1;

    /**
     * Operation mode: equalize distance between right sides
     */
    public final static int RIGHTS = 2;

    /**
     * Operation mode: equalize distance between tops
     */
    public final static int TOPS = 3;

    /**
     * Operation mode: equalize distance between middles (vertically)
     */
    public final static int MIDDLES = 4;

    /**
     * Operation mode: equalize distance between bottoms
     */
    public final static int BOTTOMS = 5;

    /**
     * Operation mode: equalize space between bounding rectangles (horizontally)
     **/
    public final static int HORIZONTAL_DISTANCE = 6;

    /**
     * Operation mode: equalize space between bounding rectangles (vertically)
     **/
    public final static int VERTICAL_DISTANCE = 7;

    /**
     * Operation mode: line up all figures along a straight line,
     * the line may be of any gradient. The center of each figure
     * is relevant.
     **/
    public final static int DIAGONAL_CENTERS = 8;

    /**
     * Operation mode: line up all figures along a straight line,
     * the line may be of any gradient. The distances between the
     * figures are relevant.
     * <p><b>Does this mode make any sense?
     *       The current implementation does not.
     * </b></p>
     **/
    private final static int DIAGONAL_DISTANCE = 9;
    private int fOp;

    /**
     * Constructs a spread command.
     *
     * @param name   the command name
     * @param op     kind of the spread operation (LEFTS, RIGHTS,
     *               CENTERS, etc.); determines the spread direction
     *               (horizontal or vertical) and also the location
     *               of the "hotspot" of each figure (the point from
     *               which the distances are calculated).
     */
    public SpreadCommand(String name, int op) {
        super(name);
        // getEditor() = editor;
        fOp = op;
    }

    /**
     * This command is executable when at least 3 figures are
     * selected in the current drawing view.
     **/
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return getEditor().view().selectionCount() > 2;
    }

    public boolean executeUndoable() {
        // Check if execution conditions are still met.
        if (isExecutable()) {
            if (!super.isExecutable()) {
                return false;
            }
            DrawingView view = getEditor().view();
            FigureEnumeration selection = view.selectionElements();

            // Hide all connection figures in the enumeration.
            selection = new FilteredFigureEnumerator(selection,
                                                     new ConnectionFilter());


            // But now, there may be the problem that there are
            // less than 3 figures remaining...
            // I will handle that after building up the sorted list.
            // First, copy the figures from the selection enumeration
            // into a list I can work with repeatedly.
            // While touching all figures, I can also figure out the
            // global bounding box surrounding all hotspots.
            List<SortPair> sortedList = new LinkedList<SortPair>();
            Iterator<SortPair> sortedIter;
            SortPair currentFigurePair;
            Rectangle bBox = null;

            while (selection.hasMoreElements()) {
                currentFigurePair = new SortPair(fOp, selection.nextFigure());
                if (bBox == null) {
                    bBox = new Rectangle(currentFigurePair.hotspot);
                } else {
                    bBox.add(currentFigurePair.hotspot);
                }
                sortedList.add(0, currentFigurePair);
            }

            // Now the promised check whether there are enough figures
            // left after filtering out all connection figures:
            if (sortedList.size() < 3) {
                return false;
            }

            // Then, i need to sort the figures by their relevant
            // corner (the "hotspot" - it depends on the operation
            // mode selected on instantiation).
            if ((fOp != DIAGONAL_CENTERS) && (fOp != DIAGONAL_DISTANCE)) {
                Collections.sort(sortedList, new HotspotComparator(fOp));
            } else {
                // In diagonal mode, there are some additional computations
                // needed to find out the outmost figures and how to order
                // all other figures.
                // The proceeding is:
                // 1. Sort all figures by their distance from the center
                //    of the bounding box (largest distance first).
                // 2. Find out the two outmost figures:
                //    The first figure is the first element of the sorted list.
                //    The second figure is not neccessarily the second list
                //    element because that could lie near the first one.
                //    Instead all figures down to a minimum distance to the
                //    center are candidates, the one with the largest distance
                //    to the first figure is chosen.
                //    The minimum distance is half the horizontal or vertical
                //    size of the bounding box (whichever is greater) because
                //    there must exist at least one figure that stretched the
                //    box to that size.
                // 3. Sort all figures by their projection point to the
                //    direct line between first and second outmost figure.
                // After these steps, the first and second outmost figures
                // are forgotten. But they should be found as first and last
                // element in the resulting sorted list.
                // This assumption holds only if there are no figures with
                // projection points outside the end points of the projection
                // line. But I think that such figures do not exist (unproven).
                // Step 1
                Point bBoxCenter = Geom.center(bBox);

                Collections.sort(sortedList, new DistanceComparator(bBoxCenter));

                sortedIter = sortedList.iterator();
                SortPair firstPair = sortedIter.next();

                SortPair secondPair = firstPair;
                SortPair currentPair = firstPair;
                if (bBox != null) {
                    int minDistance = ((bBox.width > bBox.height) ? bBox.width
                                                                  : bBox.height) / 2;
                    long minDistance2 = minDistance * minDistance;

                    long foundDistance2 = 0;
                    long currentDistance2 = 0;
                    while (sortedIter.hasNext()
                                   && (currentPair.distance2(bBoxCenter) >= minDistance2)) {
                        currentPair = sortedIter.next();
                        currentDistance2 = currentPair.distance2(firstPair.hotspot);

                        if (foundDistance2 < currentDistance2) {
                            foundDistance2 = currentDistance2;
                            secondPair = currentPair;
                        }
                    }

                    Collections.sort(sortedList,
                                     new ProjectionComparator(firstPair.hotspot,
                                                              secondPair.hotspot));
                }
            }

            // Now compute the sum of the distances to distribute.
            // The sum may be negative, the figures will then overlap
            // after being repositioned.
            // I do not compute the should-distance between each 
            // pair of figures globally to avoid the round-off error
            // to be multiplied.
            int wholeLengthX = 0;
            int wholeLengthY = 0;
            Rectangle prevBox;
            Rectangle currentBox;
            int firstIndex = 0;
            int lastIndex = sortedList.size() - 1;
            sortedIter = sortedList.iterator();

            switch (fOp) {
            case LEFTS:
            case RIGHTS:
            case CENTERS:
            case TOPS:
            case BOTTOMS:
            case MIDDLES:
            case DIAGONAL_CENTERS:
                // Get the distance between the outmost figures.
                wholeLengthX = (sortedList.get(lastIndex)).hotspot.x
                               - (sortedList.get(firstIndex)).hotspot.x;
                wholeLengthY = (sortedList.get(lastIndex)).hotspot.y
                               - (sortedList.get(firstIndex)).hotspot.y;
                break;
            case HORIZONTAL_DISTANCE:
            case VERTICAL_DISTANCE:
                // Sum the distances between the right(lower) border of
                // a figure and the left(upper) border of its successor.
                prevBox = (sortedIter.next()).figure.displayBox();
                while (sortedIter.hasNext()) {
                    currentBox = (sortedIter.next()).figure.displayBox();
                    wholeLengthX += currentBox.x - prevBox.x - prevBox.width;
                    wholeLengthY += currentBox.y - prevBox.y - prevBox.height;
                    prevBox = currentBox;
                }
                break;
            case DIAGONAL_DISTANCE:
                // Get the distance between the outmost figures (as in classical modes)
                // then subtract the size of all figures (except the last figure,
                // which lies outside the bounding box due to hotspot computation).
                wholeLengthX = (sortedList.get(lastIndex)).hotspot.x
                               - (sortedList.get(firstIndex)).hotspot.x;
                wholeLengthY = (sortedList.get(lastIndex)).hotspot.y
                               - (sortedList.get(firstIndex)).hotspot.y;
                prevBox = (sortedIter.next()).figure.displayBox();
                while (sortedIter.hasNext()) {
                    currentBox = (sortedIter.next()).figure.displayBox();
                    wholeLengthX -= prevBox.width;
                    wholeLengthY -= prevBox.height;
                    prevBox = currentBox;
                }
                break;
            }

            // Finally move the hotspot of the figures to 
            // the new "shouldSpots" computed according to
            // the operation mode.
            Point shouldSpot = null;
            Point firstSpot = (sortedList.get(0)).hotspot;
            int nodeCount = 0;
            int intervals = sortedList.size() - 1;
            SortPair prevFigurePair = null; // will contain hotspot *AFTER* move!
            sortedIter = sortedList.iterator();

            while (sortedIter.hasNext()) {
                currentFigurePair = sortedIter.next();
                switch (fOp) {
                case LEFTS:
                case CENTERS:
                case RIGHTS:
                    shouldSpot = new Point(firstSpot.x
                                           + wholeLengthX * nodeCount / intervals,
                                           currentFigurePair.hotspot.y);
                    break;
                case TOPS:
                case BOTTOMS:
                case MIDDLES:
                    shouldSpot = new Point(currentFigurePair.hotspot.x,
                                           firstSpot.y
                                           + wholeLengthY * nodeCount / intervals);
                    break;
                case DIAGONAL_CENTERS:
                    shouldSpot = new Point(firstSpot.x
                                           + wholeLengthX * nodeCount / intervals,
                                           firstSpot.y
                                           + wholeLengthY * nodeCount / intervals);
                    break;
                case HORIZONTAL_DISTANCE:
                case VERTICAL_DISTANCE:
                case DIAGONAL_DISTANCE:
                    // The following calculation is dependant on the chosen
                    // hotspot location relative to the bounding rectangle!
                    if (prevFigurePair != null) {
                        // The distance to add is recomputed each round
                        // to get different rounding results.
                        int distanceToAddX = (wholeLengthX * nodeCount / intervals)
                                             - (wholeLengthX * (nodeCount - 1) / intervals);
                        int distanceToAddY = (wholeLengthY * nodeCount / intervals)
                                             - (wholeLengthY * (nodeCount - 1) / intervals);

                        switch (fOp) {
                        case HORIZONTAL_DISTANCE:
                            shouldSpot = new Point(prevFigurePair.hotspot.x
                                                   + prevFigurePair.figure
                                             .displayBox().width
                                                   + distanceToAddX,
                                                   currentFigurePair.hotspot.y);
                            break;
                        case VERTICAL_DISTANCE:
                            shouldSpot = new Point(currentFigurePair.hotspot.x,
                                                   prevFigurePair.hotspot.y
                                                   + prevFigurePair.figure
                                             .displayBox().height
                                                   + distanceToAddY);
                            break;
                        case DIAGONAL_DISTANCE:
                            // The figure to use the widht/height from depends on
                            // the direction we iterate through the figures:
                            // from left(top) to right(bottom) or opposite?
                            shouldSpot = new Point(prevFigurePair.hotspot.x
                                                   + ((wholeLengthX >= 0)
                                                      ? prevFigurePair.figure
                                             .displayBox().width
                                                      : currentFigurePair.figure
                                                        .displayBox().width)
                                                   + distanceToAddX,
                                                   prevFigurePair.hotspot.y
                                                   + ((wholeLengthY >= 0)
                                                      ? prevFigurePair.figure
                                             .displayBox().height
                                                      : currentFigurePair.figure
                                                        .displayBox().height)
                                                   + distanceToAddY);
                            break;
                        }
                    } else {
                        // The first figure should stay at its place.
                        shouldSpot = currentFigurePair.hotspot;
                    }
                    break;
                }
                if (shouldSpot != null) {
                    currentFigurePair.figure.moveBy(shouldSpot.x
                                                    - currentFigurePair.hotspot.x,
                                                    shouldSpot.y
                                                    - currentFigurePair.hotspot.y);
                    prevFigurePair = new SortPair(fOp, currentFigurePair.figure);
                }
                nodeCount++;
            }

            view.checkDamage();
            return true;
        }
        return false;
    }

    /**
     * Used by <code>execute()</code> to store the "hotspot"
     * along with the figures in a sorted list.
     **/
    private class SortPair {

        /**
         * A figure.
         **/
        public final Figure figure;

        /**
         * The relevant corner of the bounding rectangle of the
         * figure, may also be its center in some operation modes.
         **/
        public final Point hotspot;

        /**
         * Groups the figure together with its "hotspot" which
         * is computed according to the specified operation mode.
         * @param op operation mode, influences the hotspot
         *           computation (one of the SpreadCommand
         *           constants LEFTS, CENTERS, RIGHTS, ...).
         * @param figure is stored unchanged as SortPair.figure,
         *               the hotspot is calculated from it.
         **/
        public SortPair(int op, Figure figure) {
            // In horizontal or vertical distance modes the figures
            // are sorted with regard to their upper left corners
            // (same computation as in LEFTS or TOPS mode).
            // Note that in these modes there are two relevant points
            // per figure (the two opposite sides), but I had to choose
            // one of them.
            // I did not choose the center as hot spot because this
            // leads to many divisions with truncation discrepancies.
            // However, the distribution algorithm depends on the
            // chosen hotspot location, so do not change it without 
            // adapting the computation.
            Point newHotspot = null;

            switch (op) {
            case LEFTS:
            case TOPS:
            case HORIZONTAL_DISTANCE:
            case VERTICAL_DISTANCE:
                //case DIAGONAL_DISTANCE:
                newHotspot = figure.displayBox().getLocation();
                break;
            case RIGHTS:
            case BOTTOMS:
                newHotspot = new Point(figure.displayBox().x
                                       + figure.displayBox().width,
                                       figure.displayBox().y
                                       + figure.displayBox().height);
                break;
            case MIDDLES:
            case CENTERS:
            case DIAGONAL_CENTERS:
                newHotspot = Geom.center(figure.displayBox());
                break;
            }

            this.figure = figure;
            this.hotspot = newHotspot;
        }

        /**
         * Returns the square distance from the given point to my
         * "hotspot".
         **/
        public long distance2(Point origin) {
            // Perhaps it makes sense to cache popular results?
            // When sorting a list, the same distance may be needed
            // several times...
            return Geom.length2(origin.x, origin.y, hotspot.x, hotspot.y);
        }
    }

    /**
     * Used by <code>execute()</code> to order the figures
     * in a sorted list by their "hotspot" location.
     **/
    private class HotspotComparator implements Comparator<SortPair> {

        /**
         * The mode to use (the horizontal/vertical
         * distinction is of relevance).
         **/
        private int fOp;

        /**
         * Creates a comparator which can compare two
         * <code>SortPair</code> objects with regard to
         * their "hotspots".
         *
         * @param op operation mode, the comparision depends
         *           on its horizontal/vertical information
         *           (one of the SpreadCommand constants
         *           LEFTS, CENTERS, RIGHTS, ...).
         **/
        public HotspotComparator(int op) {
            this.fOp = op;
        }

        /**
         * @return <0, if the second SortPair's hotspot is more left or above <br>
         *         =0, if both hotspots have the same x or y coordinate <br>
         *         >0, if the first SortPair's hotspot is more left or above <br>
         *         Horizontal or vertical mode is selected via the <code>op</code>
         *         parameter of the constructor.
         **/
        public int compare(SortPair fst, SortPair snd) {
            SortPair fstPair = fst;
            SortPair sndPair = snd;

            switch (fOp) {
            case HORIZONTAL_DISTANCE:
            case LEFTS:
            case RIGHTS:
            case CENTERS:
                return fstPair.hotspot.x - sndPair.hotspot.x;
            case VERTICAL_DISTANCE:
            case TOPS:
            case BOTTOMS:
            case MIDDLES:
                return fstPair.hotspot.y - sndPair.hotspot.y;
            }

            throw new RuntimeException("SpreadCommand: Comparator: Illegal operation mode "
                                       + fOp + "!");
        }
    }

    /**
     * Used by <code>execute()</code> to order the figures
     * in a sorted list by their geometrical distance from
     * a given location.
     **/
    private class DistanceComparator implements Comparator<SortPair> {

        /**
         * The point to measure all distances from.
         **/
        private Point origin;

        /**
         * Creates a comparator which can compare two
         * <code>SortPair</code> objects with regard to
         * their distance from a given point.
         *
         * @param origin point to measure the distance from
         **/
        public DistanceComparator(Point origin) {
            this.origin = origin;
        }

        /**
         * @return -1, if the second SortPair's hotspot is nearer to origin. <br>
         *         =0, if both SortPairs' hotspots are within equal distance. <br>
         *         >0, if the first SortPair's hotspot is nearer to origin.
         **/
        public int compare(SortPair fst, SortPair snd) {
            SortPair fstPair = fst;
            SortPair sndPair = snd;
            long difference = sndPair.distance2(origin)
                              - fstPair.distance2(origin);
            if (difference < 0) {
                return -1;
            } else if (difference > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Used by <code>execute()</code> to order the figures
     * in a sorted list by the distance of their projection
     * on a line to the starting point of the line.
     *
     * <p>
     * The computation works as follows:
     * Let vl be the normalized gradient vector of the projection
     * line, consisting of (xl, yl), and let vn be a normalized
     * vector orthogonal to vl (this can be achieved by choosing
     * (xn=yl, yn=-xl) as components).
     * </p>
     * <p>
     * The vector vf from the origin of the line to the hotspot
     * of a figure to compare can the be expressed as linear
     * combination of vl and vn.
     * <pre>
     * (eq.1)         vf = i * vl + j * vn
     * </pre>
     * The value i is a measure for the distance from the projection
     * point of the hotspot on the line to the origin of the line.
     * </p>
     * <p>
     * Eq.1 can be transformed into a equation system and resolved
     * to i, which leads to the following formula:
     * <pre>
     *                     xf*xl + yf*yl
     * (eq.2)          i = -------------
     *                      xl^2 + yl^2
     * </pre>
     * This formula is not usable if xl=yl=0, this case has to
     * be rejected when the comparator is constructed.
     * <p>
     * </p>
     * In the derivation of the formula, also yl=0 had to be excluded.
     * However, the formula produces usable results in that case.
     * So that case is not handled in any special way.
     * </p>
     **/
    private class ProjectionComparator implements Comparator<SortPair> {

        /**
         * x component of the gradient triangle of the line.
         **/
        private float xl;

        /**
         * y component of the gradient triangle of the line.
         **/
        private float yl;

        /**
         * xl^2 + yl^2 (just to save computation time)
         **/
        private float denominator;

        /**
         * Creates a comparator which can compare two
         * <code>SortPair</code> objects with regard to
         * their projected distance from a given point.
         *
         * @param origin starting point of the projection line,
         *               also the point to measure the distances.
         * @param second end point of the projection line.
         *
         * @exception ArithmeticException
         *            if the given end points are too close,
         *            i.e. a projection line could not be computed.
         **/
        public ProjectionComparator(Point origin, Point second)
                throws ArithmeticException {
            float distance = (float) Math.sqrt(Geom.length2(origin.x, origin.y,
                                                            second.x, second.y));
            if (distance == 0) {
                throw new ArithmeticException("Projection line end points are too near!");
            }

            this.xl = (second.x - origin.x) / distance;
            this.yl = (second.y - origin.y) / distance;
            this.denominator = xl * xl + yl * yl;
        }

        /**
         * @return -1, if i1 <  i2. <br>
         *          0, if i1 == i2. <br>
         *          1, if i1  > i2.
         *
         * @see SpreadCommand.ProjectionComparator Overview (description of i)
         **/
        public int compare(SortPair fst, SortPair snd) {
            SortPair fstPair = fst;
            SortPair sndPair = snd;

            // As this computation will be repeated several times for
            // each SortPair, it could make sense to delegate the
            // computation to the SortPair and cache the result there.
            float i1 = fstPair.hotspot.x * xl
                       + fstPair.hotspot.y * yl / denominator;
            float i2 = sndPair.hotspot.x * xl
                       + sndPair.hotspot.y * yl / denominator;
            if (i1 > i2) {
                return 1;
            } else if (i1 < i2) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /**
     * This filter lets pass all types of figures
     * except instances of <code>ConnectionFigure</code>.
     * This makes sense for the spread command because
     * connections produce contra-intuitive spread results.
     * And they cannot be moved on their own, anyway...
     **/
    private class ConnectionFilter implements FigureFilter {

        /**
         * @return <code>false</code>, if the figure is a ConnectionFigure
         *       - <code>true</code>, otherwise.
         **/
        public boolean isUsed(Figure figure) {
            if (figure instanceof ConnectionFigure) {
                return false;
            } else {
                return true;
            }
        }
    }
}