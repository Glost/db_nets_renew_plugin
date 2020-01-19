/*
 * Created on Sep 13, 2005
 *
 */
package de.renew.fa;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureWithID;
import CH.ifa.draw.framework.FilterContainer;

import CH.ifa.draw.io.SimpleFileFilter;

import de.renew.fa.figures.FAStateFigure;

import de.renew.gui.CPNDrawing;
import de.renew.gui.GraphLayout;
import de.renew.gui.LayoutableDrawing;

import java.util.HashSet;
import java.util.Hashtable;


/**
 * This {@link Drawing} is a container of arbitrary figures, but it
 * only supports functionality for finite automaton constructs.
 */
public class FADrawing extends CPNDrawing implements LayoutableDrawing {
    static final long serialVersionUID = -4229673070089970973L;
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FADrawing.class);

    /**
     * Container for fa {@link FileFilter}.
     */
    private static FilterContainer filterContainer;

    // ---- ID-Management -----------------------------------

    /**
     * Caches the greatest used ID by any known figure.
     */
    private transient int maxUsedID = FigureWithID.NOID;

    /**
     * Caches used IDs in this and all subfigures.
     */
    private transient Hashtable<Integer, Figure> usedIDs = new Hashtable<Integer, Figure>();

    /**
     * Gets the {@link FileFilter}s of FADrawings.
     *
     * @return  the FileFilter for FADrawings
     */
    static public FilterContainer getFilterContainer() {
        if (filterContainer == null) {
            return new FilterContainer(new FAFileFilter());
        } else {
            return filterContainer;
        }
    }

    /**
     * Constructs an empty FADrawing.
     */
    public FADrawing() {
        super();
    }

    @Override
    synchronized public void fillInGraph(GraphLayout layout) {
        logger.debug("fillInGraph(GraphLayout) called with " + layout);
        FigureEnumeration k = figures();

        while (k.hasMoreElements()) {
            Figure f = k.nextFigure();

            // if (f instanceof TransitionFigure || f instanceof PlaceFigure) {
            if (f instanceof FAStateFigure
                        || (f instanceof TextFigure
                                   && ((TextFigure) f).parent() == null)) {
                layout.addNode(f);

                // } else if (f instanceof ArcConnection) {
            } else if (f instanceof ConnectionFigure) {
                layout.addEdge((ConnectionFigure) f, 20);
            }
            logger.debug("added " + f);
        }
    }

    @Override
    public String getDefaultExtension() {
        return getDefaultFileFilter().getExtension();
    }

    @Override
    public SimpleFileFilter getDefaultFileFilter() {
        return getFilterContainer().getDefaultFileFilter();
    }

    @Override
    public HashSet<SimpleFileFilter> getExportFileFilters() {
        return getFilterContainer().getExportFileFilters();
    }

    @Override
    public HashSet<SimpleFileFilter> getImportFileFilters() {
        return getFilterContainer().getImportFileFilters();
    }

    @Override
    public String getWindowCategory() {
        return "Finite Automata";
    }

    @Override
    public Figure add(Figure figure) {
        logger.debug("add(Figure) called with " + figure);

        // The ID check has to be done before the
        // figure is added to the list to avoid
        // collision of the figure with itself.
        // If figure is capable of holding an ID,
        // check its ID and assign a new one, if
        // needed.
        if (figure instanceof FigureWithID) {
            checkAndAssignID((FigureWithID) figure);
        }

        Figure result = super.add(figure);
        return result;
    }

    /**
     * Assigns the given figure object a new ID,
     * if it does not already have a legal (unique) ID.
     */
    private void checkAndAssignID(FigureWithID figure) {
        int oldID = figure.getID();
        int newID = oldID;
        FigureWithID inCache = (FigureWithID) usedIDs.get(new Integer(oldID));

        if ((inCache != null) && (inCache != figure)) {
            // The old ID is already used by another figure,
            // so reset it temporarily to NOID.
            newID = FigureWithID.NOID;
        }
        if (newID == FigureWithID.NOID) {
            newID = newUniqueID();
            figure.setID(newID);
        }
        usedIDs.put(new Integer(newID), figure);
    }

    /**
     * Gives an ID, that is not used inside this drawing object.
     *
     * @see CH.ifa.draw.framework.FigureWithID
     */
    private int newUniqueID() {
        // If the cache is uninitialized,
        // recompute its value.
        // If the greatest used ID is  still equal to
        // NOID afterwards, then there is no figure.
        if (maxUsedID == FigureWithID.NOID) {
            recomputeIDCache();
        }

        maxUsedID++;
        return maxUsedID;
    }

    /**
     * Recomputes the ID cache (maxUsedID and usedIDs)
     * and eliminates IDs used more than once.
     */


//    private void recomputeIDCache() {
//        // To ensure that no ID will be reassigned
//        // even after it has been freed, do never
//        // reset the greatest ID cache.
//        // However, after closing the drawing, this
//        // value will be reset anyway...
//        // maxUsedID = FigureWithID.NOID;
//        Vector<Figure> offendingFigures = new Vector<Figure>();
//
//        usedIDs.clear();
//
//        addToIDCache(this, offendingFigures);
//
//        Enumeration<Figure> figureList = offendingFigures.elements();
//
//        while (figureList.hasMoreElements()) {
//            checkAndAssignID((FigureWithID) figureList.nextElement());
//        }
//    }

//    /**
//     * Do not call this method directly, use
//     * <code>recomputeIDCache()</code> instead.
//     * @param container CompositeFigure to scan for figures with ID.
//     * @param offendingFigures Collects figures with illegal IDs.
//     */
//    private void addToIDCache(CompositeFigure container,
//                              Vector<Figure> offendingFigures) {
//        FigureEnumeration knownFigures = container.figures();
//        Figure figure;
//        int usedID;
//        FigureWithID inCache;
//
//
//        // Iterate through all known Figures and update the
//        // greatest seen ID.
//        // Also update the Hashtable of used IDs and check if
//        // some ID is already used by some other figure.
//        // If there are CompositeFigures contained in the
//        // drawing (like GroupFigures), recurse into them.
//        while (knownFigures.hasMoreElements()) {
//            figure = knownFigures.nextFigure();
//            if (figure instanceof FigureWithID) {
//                usedID = ((FigureWithID) figure).getID();
//                inCache = (FigureWithID) usedIDs.get(new Integer(usedID));
//                if ((inCache == null) || (inCache == figure)) {
//                    usedIDs.put(new Integer(usedID), figure);
//                    if (usedID > maxUsedID) {
//                        maxUsedID = usedID;
//                    }
//                } else {
//                    // An ID is used twice.This will be silently corrected
//                    // by the caller of this method.
//                    // logger.debug("ID used more than once: "+usedID);
//                    offendingFigures.addElement(figure);
//                }
//            } else if (figure instanceof CompositeFigure) {
//                addToIDCache((CompositeFigure) figure, offendingFigures);
//            }
//        }
//    }
}