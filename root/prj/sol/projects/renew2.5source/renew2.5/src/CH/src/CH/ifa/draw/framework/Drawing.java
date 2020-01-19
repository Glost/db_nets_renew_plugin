/*
 * @(#)Drawing.java 5.1
 *
 */
package CH.ifa.draw.framework;

import CH.ifa.draw.io.SimpleFileFilter;

import CH.ifa.draw.util.Storable;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.io.File;
import java.io.Serializable;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;


/**
 * Drawing is a container for figures.
 * <p>
 * Drawing sends out DrawingChanged events to DrawingChangeListeners
 * whenever a part of its area was invalidated.
 * <hr>
 * <b>Design Patterns</b><P>
 * <img src="images/red-ball-small.gif" width=6 height=6 alt=" o ">
 * <b><a href=../pattlets/sld026.htm>Observer</a></b><br>
 * The Observer pattern is used to decouple the Drawing from its views and
 * to enable multiple views.<hr>
 *
 * @see Figure
 * @see DrawingView
 * @see FigureChangeListener
 */
public interface Drawing extends Storable, FigureChangeListener, Serializable {
    public String getName();

    public void setName(String name);

    public File getFilename();

    public void setFilename(File filename);

    /**
     * Return true, if a backup has been made since the
     * drawing has been generated.
     */
    public boolean getBackupStatus();

    /**
     * Inform the drawing that a backup has been generated.
     */
    public void setBackupStatus(boolean status);

    /**
     * Releases the drawing and its contained figures.
     */
    public void release();

    /**
     * Gets a box that contains all figures of this drawing.
     * @see #displayBox
     */
    public Rectangle displayBox();

    /**
     * Returns an enumeration to iterate in
     * Z-order back to front over the figures.
     */
    public FigureEnumeration figures();

    /**
     * Returns an enumeration to iterate in
     * Z-order front to back over the figures.
     */
    public FigureEnumeration figuresReverse();

    /**
     * Finds a top level Figure. Use this call for hit detection that
     * should not descend into the figure's children.
     */
    public Figure findFigure(int x, int y);

    /**
     * Finds a top level Figure that intersects the given rectangle.
     */
    public Figure findFigure(Rectangle r);

    /**
     * Finds a top level Figure, but supresses the passed
     * in figure. Use this method to ignore a figure
     * that is temporarily inserted into the drawing.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param without the figure to be ignored during
     * the find.
     */
    public Figure findFigureWithout(int x, int y, Figure without);

    /**
     * Finds a top level Figure that intersects the given rectangle.
     * It supresses the passed
     * in figure. Use this method to ignore a figure
     * that is temporarily inserted into the drawing.
     */
    public Figure findFigure(Rectangle r, Figure without);

    /**
     * Finds a figure but descends into a figure's
     * children. Use this method to implement <i>click-through</i>
     * hit detection, that is, you want to detect the inner most
     * figure containing the given point.
     */
    public Figure findFigureInside(int x, int y);

    /**
     * Finds a figure but descends into a figure's
     * children. It supresses the passed
     * in figure. Use this method to ignore a figure
     * that is temporarily inserted into the drawing.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param without the figure to be ignored during
     * the find.
     */
    public Figure findFigureInsideWithout(int x, int y, Figure without);

    /**
     * Adds a listener for this drawing.
     */
    public void addDrawingChangeListener(DrawingChangeListener listener);

    /**
     * Removes a listener from this drawing.
     */
    public void removeDrawingChangeListener(DrawingChangeListener listener);

    /**
     * Gets the listeners of a drawing.
     */
    public Enumeration<DrawingChangeListener> drawingChangeListeners();

    /**
     * Adds a figure and sets its container to refer
     * to this drawing.
     * @return the figure that was inserted.
     */
    public Figure add(Figure figure);

    /**
     * Adds a drawing and sets its container to refer
     * to this drawing.
     * @return the drawing that was inserted.
     */
    public Drawing add(Drawing drawing);

    /**
     * Adds a drawing and sets its container to refer
     * to this drawing.
     * @return the drawing that was inserted.
     */
    public Drawing add(Drawing drawing, int x, int y);

    /**
     * Adds a vector of figures.
     */
    public void addAll(Vector<?extends Figure> newFigures);

    /**
     * Removes the figure from the drawing and releases it.
     */
    public Figure remove(Figure figure);

    /**
     * Removes a figure from the figure list, but
     * doesn't release it. Use this method to temporarily
     * manipulate a figure outside of the drawing.
     */
    public Figure orphan(Figure figure);

    /**
     * Removes a vector of figures from the figure's list
     * without releasing the figures.
     * @see #orphan
     */
    public void orphanAll(Vector<?extends Figure> newFigures);

    /**
     * Removes a vector of figures.
     * @see #remove
     */
    public void removeAll(Vector<?extends Figure> figures);

    /**
     * Removes all figures.
     * @see #remove
     */
    public void removeAll();

    /**
     * Replaces a figure in the drawing without
     * removing it from the drawing.
     */
    public void replace(Figure figure, Figure replacement);

    /**
     * Sends a figure to the back of the drawing.
     */
    public void sendToBack(Figure figure);

    /**
     * Brings a figure to the front.
     */
    public void bringToFront(Figure figure);

    /**
     * Draws all the figures back to front.
     * g is of type Graphics2D
     */
    public void draw(Graphics g);

    /**
     * Invalidates a rectangle and merges it with the
     * existing damaged area.
     */
    public void figureInvalidated(FigureChangeEvent e);

    /**
     * Forces an update of the drawing change listeners.
     */
    public void figureRequestUpdate(FigureChangeEvent e);

    /**
     * Handles a removeFigureRequestRemove request that
     * is passed up the figure container hierarchy.
     * @see FigureChangeListener
     */
    public void figureRequestRemove(FigureChangeEvent e);

    /**
     * Forces an update of the drawing change listeners.
     */
    public void checkDamage();

    /**
     * Acquires the drawing lock.
     */
    public void lock();

    /**
     * Releases the drawing lock.
     */
    public void unlock();

    /**
     * Returns whether drawing has been modified since last save.
     */
    public boolean isModified();

    /**
     * Notifies the drawing that its modifications have been saved.
     */
    public void clearModified();

    /**
     * Tells the current drawing bounds. E.g. the area
     * occupied by all figures.
     **/
    public Rectangle getBounds();

    /**
     * Tells the default size for drawings of this type.
     * E.g. the area that should be visible by default
     * when a new drawing is created.
     **/
    public Dimension defaultSize();

    /**
     * Determines the type of drawings this drawing should be sorted into.
     * Make it a plural, if possible. Currently this value is used by
     * {@link CH.ifa.draw.application.WindowsMenu}.
     **/
    public String getWindowCategory();

    //-------------------------------------------------
    // Methods for file handling determination
    public SimpleFileFilter getDefaultFileFilter();

    /**
     * @return ext - the Extension of the default SimpleFileFilter for this Drawing.
     */
    public String getDefaultExtension();

    public HashSet<SimpleFileFilter> getImportFileFilters();

    public HashSet<SimpleFileFilter> getExportFileFilters();

    public void init();

    /**
     * @return true if the Drawing is storable
     */
    public boolean isStorable();
}