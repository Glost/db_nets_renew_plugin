package de.renew.gui;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeAdapter;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureWithID;
import CH.ifa.draw.framework.FilterContainer;

import CH.ifa.draw.io.SimpleFileFilter;

import CH.ifa.draw.standard.AbstractFigure;
import CH.ifa.draw.standard.CompositeFigure;
import CH.ifa.draw.standard.StandardDrawing;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import de.renew.io.RNWFileFilter;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowNetSystem;

import java.awt.Dimension;

import java.io.IOException;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;


public class CPNDrawing extends StandardDrawing implements LayoutableDrawing {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(CPNDrawing.class);
    private static FilterContainer filterContainer;

    /**
    * The shadow net of this drawing.
    * <p>
    * Transient, can be recalculated via
    * <code>buildShadow()</code>.
    * </p>
    */
    protected transient ShadowNet shadowNet = null;

    /**
     * The figure which should be displayed as representation
     * for instances of this net.
     * @serial
     */
    private AbstractFigure iconFigure = null;

    /**
     * Cache for all associations to
     * {@link FigureWithHighlight}s from their
     * highlight figures.
     * To point it out: the figure to be highlighted is
     * the key, and the figure with hilight is the value
     * of a pair in the hashtable.
     * <p>
     * Transient, will be rebuilt on deserialization.
     * </p>
     */
    private transient Hashtable<Figure, Figure> hilightMap = new Hashtable<Figure, Figure>();

    // ---- ID-Management -----------------------------------


    /**
     * Caches the greatest used ID by any known figure.
     * Updated by <code>recomputeIDCache()</code>.
     * This field is transient because it is only a cache.
     */
    private transient int maxUsedID = FigureWithID.NOID;

    /**
     * Caches used IDs in this and all subfigures.
     * Updated by <code>recomputeIDCache()</code>.
     * This field is transient because it is only a cache.
     */
    private transient Hashtable<Integer, Figure> usedIDs = new Hashtable<Integer, Figure>();

    public CPNDrawing() {
        super();
    }

    public void release() {
        super.release();
        discardShadow();
    }

    public ShadowNet getShadow() {
        return shadowNet;
    }

    static CPNDrawing findDrawing(Object errorObject) {
        // Determine the drawing containing the errorObject.
        if (errorObject instanceof ShadowNetElement) {
            return (CPNDrawing) ((ShadowNetElement) errorObject).getNet().context;
        }
        return null;
    }

    public void discardShadow() {
        if (shadowNet != null) {
            shadowNet.discard();
            shadowNet = null;
        }
    }

    /**
     * Calls the {@link ShadowHolder#buildShadow} method on all net
     * element figures of the given type.
     *
     * @param clazz  the <code>ShadowHolder</code> subclass to use
     *               as filter criterium
     */
    private void buildShadow(Class<?> clazz) {
        FigureEnumeration k = figures();

        while (k.hasMoreElements()) {
            Figure fig = k.nextFigure();

            if (fig instanceof ShadowHolder && clazz.isInstance(fig)) {
                ((ShadowHolder) fig).buildShadow(shadowNet);
            }
        }
    }

    public ShadowNet buildShadow(ShadowNetSystem netSystem) {
        discardShadow();
        shadowNet = new ShadowNet(getName(), netSystem);
        shadowNet.context = this;


        // Build shadows for declaration nodes (java-nets: only one per net!)
        buildShadow(DeclarationFigure.class);


        // Build shadows for nodes:
        buildShadow(NodeFigure.class);


        // Build shadows for connections:
        buildShadow(CH.ifa.draw.figures.LineConnection.class);


        // Build shadows for inscriptions:
        buildShadow(CPNTextFigure.class);

        return shadowNet;
    }

    public void setIconFigure(AbstractFigure iconFigure) {
        this.iconFigure = iconFigure;
    }

    public Figure getIconFigure() {
        return iconFigure;
    }

    /**
     * Removes a figure from the composite.
     * Additionally checks if the icon figure is removed.
     * Also checks if the ID cache has to be updated.
     */
    public Figure remove(Figure figure) {
        if (figure == iconFigure) {
            iconFigure = null;
        }
        if (figure instanceof FigureWithID) {
            freeID((FigureWithID) figure);
        }
        if (figure instanceof FigureWithHighlight) {
            Figure hilight = ((FigureWithHighlight) figure).getHighlightFigure();

            if (hilight != null) {
                hilightMap.remove(hilight);
            }
        }

        Figure result = super.remove(figure);

        if (figure instanceof CompositeFigure) {
            recomputeIDCache();

        }
        return result;
    }

    // add(Figure) is handled somewhere below.


    /**
     * Writes the contained figures to the StorableOutput.
     */
    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeStorable(iconFigure);
    }

    /**
     * Reads the contained figures from StorableInput.
     */
    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        if (dr.getVersion() >= 2) {
            try {
                iconFigure = (AbstractFigure) dr.readStorable();
            } catch (IOException e) {
                logger.error("Icon expected.");
                logger.debug("Icon expected.", e);
            }
            if (dr.getVersion() >= 3) {
                recomputeHilightMap();
            }
        }
        recomputeIDCache();
    }

    synchronized public void fillInGraph(GraphLayout layout) {
        FigureEnumeration k = figures();

        while (k.hasMoreElements()) {
            Figure f = k.nextFigure();

            // if (f instanceof TransitionFigure || f instanceof PlaceFigure) {
            if (f instanceof TransitionFigure || f instanceof PlaceFigure
                        || f instanceof TextFigure
                        && ((TextFigure) f).parent() == null) {
                layout.addNode(f);

                // } else if (f instanceof ArcConnection) {
            } else if (f instanceof ConnectionFigure) {
                layout.addEdge((ConnectionFigure) f, 20);
            }
        }
    }

    void setHighlightFigure(final FigureWithHighlight node, final Figure fig) {
        Figure oldHighlight = node.getHighlightFigure();

        if (oldHighlight != null) {
            hilightMap.remove(oldHighlight);
        }
        node.setHighlightFigure(fig);
        if (fig != null) {
            hilightMap.put(fig, node);
            fig.addFigureChangeListener(new FigureChangeAdapter() {
                    public void figureRemoved(FigureChangeEvent e) {
                        setHighlightFigure(node, null);
                    }
                });
            node.addFigureChangeListener(new FigureChangeAdapter() {
                    public void figureRemoved(FigureChangeEvent e) {
                        hilightMap.remove(fig);
                    }
                });
        }
    }

    FigureWithHighlight getFigureForHighlight(Figure hilightFig) {
        try {
            return (FigureWithHighlight) hilightMap.get(hilightFig);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public Dimension defaultSize() {
        return new Dimension(535, 788);
    }

    /**
     * Deserialization method, behaves like default readObject
     * method except restoring default values for transient
     * fields.
     */
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();


        // The serialization mechanism constructs the object
        // with a less-than-no-arg-constructor, so that the
        // default values for transient fields have to be
        // reinitialized manually.
        hilightMap = new Hashtable<Figure, Figure>();
        maxUsedID = FigureWithID.NOID;
        usedIDs = new Hashtable<Integer, Figure>();


        // For full functionality we need to recompute some
        // tables.
        recomputeIDCache();
        recomputeHilightMap();
    }

    /**
     * Recomputes the <code>hilightMap</code> by examining
     * all figures.
     */
    private void recomputeHilightMap() {
        hilightMap.clear();
        addToHilightMap(this);
    }

    /**
     * Adds the highlight figures of all figures contained in the
     * given <code>CompositeFigure</code> to the <code>hilightMap</code>.
     * Used by {@link #recomputeHilightMap}.
     *
     * @param container  CompositeFigure to scan for figures
     * with hilights.
     */
    private void addToHilightMap(CompositeFigure container) {
        FigureEnumeration figenumeration = container.figures();

        while (figenumeration.hasMoreElements()) {
            Figure fig = figenumeration.nextFigure();

            if (fig instanceof FigureWithHighlight) {
                Figure hilight = ((FigureWithHighlight) fig).getHighlightFigure();

                if (hilight != null) {
                    // logger.debug("Highlight for "+fig+" restored!");
                    hilightMap.put(hilight, fig);
                }
            }
            if (fig instanceof CompositeFigure) {
                addToHilightMap((CompositeFigure) fig);
            }
        }
    }

    public String getWindowCategory() {
        return "Nets";
    }


    /**
     * Adds a figure to the list of figures
     * (as the superclass does).
     *
     * If the figure implements the interface<code>
     * CH.ifa.draw.framework.FigureWithID</code>, checks
     * its ID and assigns a new one if needed.
     */
    public Figure add(Figure figure) {
        // The ID check has to be done before the
        // figure is added to the list to avoid
        // collision of the figure with itself.
        // If figure is capable of holding an ID,
        // check its ID and assign a new one, if
        // needed.
        if (figure instanceof FigureWithID) {
            checkAndAssignID((FigureWithID) figure);

            // Now the figure can be added to the list.
        }
        Figure result = super.add(figure);


        // If the figure can hilight other figures, put
        // its highlight figure into the map (if there
        // is any).
        if (figure instanceof FigureWithHighlight) {
            Figure hilight = ((FigureWithHighlight) figure).getHighlightFigure();

            if (hilight != null) {
                hilightMap.put(hilight, figure);
            }
        }


        // If a CompositeFigure is added, it may
        // contain a lot of other figures.
        if (figure instanceof CompositeFigure) {
            recomputeIDCache();
            recomputeHilightMap();
        }

        return result;
    }

    // remove(...) is handled somewhere above.


    /**
     * Checks the figure if it has already a
     * legal ID. If neccessary, generates new
     * unique ID and assigns it.
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
     * Tries to assign a given id to the figure.
     */
    public void assignID(FigureWithID figure, int id) {
        // check if the new id can be given to the figure:        
        FigureWithID inCache = (FigureWithID) usedIDs.get(new Integer(id));
        if ((inCache != null) && (inCache != figure)) {
            throw new IllegalArgumentException("The id is already in use!");
        }

        // remove old mapping from cache:
        usedIDs.remove(new Integer(figure.getID()));

        // set new id and add to cache:
        figure.setID(id);
        usedIDs.put(new Integer(id), figure);
    }

    /**
     * Frees up the ID used by the given figure.
     */
    private void freeID(FigureWithID figure) {
        // Check if the ID was really cached
        // before freeing it up.
        Integer usedID = new Integer(figure.getID());

        if (usedIDs.get(usedID) == figure) {
            usedIDs.remove(usedID);
        }
    }

    /**
     * Generates a new ID not used in the list of figures.
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

        if (usedIDs.containsKey(new Integer(maxUsedID))) {
            boolean resetOnce = false;

            while (usedIDs.containsKey(new Integer(maxUsedID))) {
                maxUsedID++;

                if (maxUsedID == Integer.MIN_VALUE) {
                    maxUsedID = 1;

                    if (resetOnce) {
                        throw new RuntimeException("Maximum numnber of figures exeeded.");
                    } else {
                        resetOnce = true;
                    }
                }
            }
        }

        return maxUsedID;
    }

    /**
     * Recomputes the ID cache (maxUsedID and usedIDs)
     * and eliminates IDs used more than once.
     */
    protected void recomputeIDCache() {
        // To ensure that no ID will be reassigned
        // even after it has been freed, do never
        // reset the greatest ID cache.
        // However, after closing the drawing, this
        // value will be reset anyway...
        // maxUsedID = FigureWithID.NOID;
        Vector<Figure> offendingFigures = new Vector<Figure>();

        usedIDs.clear();

        addToIDCache(this, offendingFigures);

        Enumeration<Figure> figureList = offendingFigures.elements();

        while (figureList.hasMoreElements()) {
            checkAndAssignID((FigureWithID) figureList.nextElement());
        }
    }

    /**
     * Do not call this method directly, use
     * <code>recomputeIDCache()</code> instead.
     * @param container CompositeFigure to scan for figures with ID.
     * @param offendingFigures Collects figures with illegal IDs.
     */
    private void addToIDCache(CompositeFigure container,
                              Vector<Figure> offendingFigures) {
        FigureEnumeration knownFigures = container.figures();
        Figure figure;
        int usedID;
        FigureWithID inCache;


        // Iterate through all known Figures and update the
        // greatest seen ID.
        // Also update the Hashtable of used IDs and check if
        // some ID is already used by some other figure.
        // If there are CompositeFigures contained in the
        // drawing (like GroupFigures), recurse into them.
        while (knownFigures.hasMoreElements()) {
            figure = knownFigures.nextFigure();
            if (figure instanceof FigureWithID) {
                usedID = ((FigureWithID) figure).getID();
                inCache = (FigureWithID) usedIDs.get(new Integer(usedID));
                if ((inCache == null) || (inCache == figure)) {
                    usedIDs.put(new Integer(usedID), figure);
                    if (usedID > maxUsedID) {
                        maxUsedID = usedID;
                    }
                } else {
                    // An ID is used twice.This will be silently corrected
                    // by the caller of this method.
                    // logger.debug("ID used more than once: "+usedID);
                    offendingFigures.addElement(figure);
                }
            } else if (figure instanceof CompositeFigure) {
                addToIDCache((CompositeFigure) figure, offendingFigures);
            }
        }
    }

    /**
     * Returns the FigureWithID currently assigned to
     * the given ID. If no figure is assigned to that
     * ID, returns <code>null</code>.
     * <p>
     * It is possible that the returned figure is not
     * contained in the <code>figures()</code> enumeration.
     * Then it was found in some CompositeFigure contained
     * in the enumeration.
     * </p>
     * @see CH.ifa.draw.framework.FigureWithID
     */
    public FigureWithID getFigureWithID(int id) {
        return (FigureWithID) usedIDs.get(new Integer(id));
    }

    //------------------------------------------------------------------------------   
    static public FilterContainer getFilterContainer() {
        if (filterContainer == null) {
            return new FilterContainer(new RNWFileFilter());
        } else {
            return filterContainer;
        }
    }

    public SimpleFileFilter getDefaultFileFilter() {
        return getFilterContainer().getDefaultFileFilter();
    }

    public HashSet<SimpleFileFilter> getImportFileFilters() {
        return getFilterContainer().getImportFileFilters();
    }

    public HashSet<SimpleFileFilter> getExportFileFilters() {
        return getFilterContainer().getExportFileFilters();
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.framework.Drawing#getDefaultExtension()
     */
    public String getDefaultExtension() {
        return getDefaultFileFilter().getExtension();
    }
}