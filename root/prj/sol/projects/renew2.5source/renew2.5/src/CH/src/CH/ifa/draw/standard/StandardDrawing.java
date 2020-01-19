/*
 * @(#)StandardDrawing.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingChangeEvent;
import CH.ifa.draw.framework.DrawingChangeListener;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FilterContainer;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.io.IFAFileFilter;
import CH.ifa.draw.io.SimpleFileFilter;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import de.renew.util.Lock;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;


/**
 * The standard implementation of the Drawing interface.
 *
 * @see Drawing
 */
public class StandardDrawing extends CompositeFigure implements Drawing {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(StandardDrawing.class);
    private static FilterContainer filterContainer;
    static String fgUntitled = "untitled";
    /*
     * Serialization support
     */
    private static final long serialVersionUID = -2602151437447962046L;

    /**
     * The registered listeners.
     * <p>
     * Transient, listeners must reregister themselves
     * after deserialization.
     * </p>
     */
    private transient Vector<DrawingChangeListener> fListeners;

    /**
     * Lock that blocks concurrent accesses to the drawing.
     * Unlike the previous solution, this lock counts the number of locks
     * and unlocks and will not prematurely regrant teh lock
     * in the case of multiple locks from the same thread.
     */
    private transient Lock fLock;

    /**
     * The name of the drawing, as it gets displayed and
     * can be used for references.
     * @serial
     **/
    private String fDrawingName = fgUntitled;

    /**
     * The named of the drawing, augmented by path and extension.
     * This information is used to save the drawing. Therefore
     * it doesn't make sense to keep it on serialization.
     **/
    private transient File fDrawingFilename = null;

    /**
     * Tells whether the drawing was modified since the
     * last save. Therefore it doesn't make sense to keep
     * the information on serialization.
     **/
    private transient boolean modified = false;

    /**
     * Tells whether a backup was created on the last save.
     * If <code>false</code>, a backup copy of an old file
     * with the same name will be made before the current
     * version gets written on the next save. Therefore
     * it doesn't make sense to keep the information on
     * serialization.
     **/
    private transient boolean fBackupStatus = false;
    @SuppressWarnings("unused")
    private int drawingSerializedDataVersion = 1;

    /**
     * Constructs the Drawing.
     */
    public StandardDrawing() {
        super();
        fListeners = new Vector<DrawingChangeListener>(2);
        fLock = new Lock();
    }

    public String getName() {
        return fDrawingName;
    }

    public void setName(String name) {
        fDrawingName = name;
    }

    public Dimension getSize() {
        return new Dimension(getBounds().width, getBounds().height);
    }

    public File getFilename() {
        return fDrawingFilename;
    }

    public void setFilename(File filename) {
        fDrawingFilename = filename;
    }

    /**
     * Return true, if a backup has been made since the
     * drawing has been generated.
     */
    public boolean getBackupStatus() {
        return fBackupStatus;
    }

    /**
     * Inform the drawing that a backup has been generated.
     */
    public void setBackupStatus(boolean status) {
        fBackupStatus = status;
    }

    /**
     * Adds a listener for this drawing.
     */
    public void addDrawingChangeListener(DrawingChangeListener listener) {
        fListeners.addElement(listener);
    }

    /**
     * Removes a listener from this drawing.
     */
    public void removeDrawingChangeListener(DrawingChangeListener listener) {
        fListeners.removeElement(listener);
    }

    /**
     * Adds a listener for this drawing.
     */
    public Enumeration<DrawingChangeListener> drawingChangeListeners() {
        return fListeners.elements();
    }

    /**
     * Removes the figure from the drawing and releases it.
     */
    public Figure remove(Figure figure) {
        // ensure that we remove the top level figure in a drawing
        if (figure.listener() != null) {
            figure.listener()
                  .figureRequestRemove(new FigureChangeEvent(figure, null));
            return figure;
        }
        return null;
    }

    /**
     * Handles a removeFromDrawing request that
     * is passed up the figure container hierarchy.
     * @see CH.ifa.draw.framework.FigureChangeListener
     */
    public void figureRequestRemove(FigureChangeEvent e) {
        Figure figure = e.getFigure();
        if (fFigures.contains(figure)) {
            modified = true;
            fFigures.removeElement(figure);
            figure.removeFromContainer(this); // will invalidate figure
            figure.release();
        } else {
            logger.error("Attempt to remove non-existing figure");
        }
    }

    /**
     * Invalidates a rectangle and merges it with the
     * existing damaged area.
     * @see CH.ifa.draw.framework.FigureChangeListener
     */
    public void figureInvalidated(FigureChangeEvent e) {
        modified = true;
        if (fListeners != null) {
            for (int i = 0; i < fListeners.size(); i++) {
                DrawingChangeListener l = fListeners.elementAt(i);
                l.drawingInvalidated(new DrawingChangeEvent(this,
                                                            e
                    .getInvalidatedRectangle()));
            }
        }
    }

    /**
     * Forces an update
     */
    public void figureRequestUpdate(FigureChangeEvent e) {
        if (fListeners != null) {
            for (int i = 0; i < fListeners.size(); i++) {
                DrawingChangeListener l = fListeners.elementAt(i);
                l.drawingRequestUpdate(new DrawingChangeEvent(this, null));
            }
        }
    }

    /**
     * Checks whether the drawing has some accumulated damage
     * and informs all views about the required update,
     * if neccessary.
     */
    public synchronized void checkDamage() {
        final StandardDrawing object = this;
        EventQueue.invokeLater(new Runnable() {
                public void run() {
                    Enumeration<DrawingChangeListener> each = fListeners
                        .elements();
                    while (each.hasMoreElements()) {
                        DrawingChangeListener l = each.nextElement();
                        l.drawingRequestUpdate(new DrawingChangeEvent(object,
                                                                      null));
                    }
                }
            });
    }

    /**
     * Return's the figure's handles. This is only used when a drawing
     * is nested inside another drawing.
     */
    public Vector<Handle> handles() {
        Vector<Handle> handles = new Vector<Handle>();
        handles.addElement(new NullHandle(this, RelativeLocator.northWest()));
        handles.addElement(new NullHandle(this, RelativeLocator.northEast()));
        handles.addElement(new NullHandle(this, RelativeLocator.southWest()));
        handles.addElement(new NullHandle(this, RelativeLocator.southEast()));
        return handles;
    }

    /**
     * Gets the display box. This is the union of all figures.
     */
    public Rectangle displayBox() {
        Rectangle box = null;
        FigureEnumeration k = figures();
        while (k.hasMoreElements()) {
            Figure f = k.nextFigure();
            if (f.isVisible()) {
                Rectangle r = f.displayBox();
                if (box == null) {
                    box = r;
                } else {
                    box.add(r);
                }
            }
        }
        if (box == null) {
            return new Rectangle(0, 0, 100, 100);
        } else {
            return new Rectangle(box.x - 10, box.y - 10, box.width + 20,
                                 box.height + 20);
        }
    }

    public void basicDisplayBox(Point p1, Point p2) {
    }

    /**
     * Acquires the drawing lock.
     */
    public void lock() {
        fLock.lock();
    }

    /**
     * Releases the drawing lock.
     */
    public void unlock() {
        fLock.unlock();
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException {
        s.defaultReadObject();

        fListeners = new Vector<DrawingChangeListener>(2);
        fLock = new Lock();
    }

    /**
     * Returns whether drawing has been modified since last save.
     */
    public boolean isModified() {
        return modified;
    }

    public void clearModified() {
        modified = false;
    }

    public Rectangle getBounds() {
        return displayBox();
    }

    public Dimension defaultSize() {
        return new Dimension(430, 406);
    }

    /**
     * Writes the contained figures to the StorableOutput.
     */
    public void write(StorableOutput dw) {
        super.write(dw);
    }

    /**
     * Reads the contained figures from StorableInput.
     */
    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        modified = false;
    }

    public String getWindowCategory() {
        return "JHotDrawings";
    }

    //------------------------------------------------------------------------------   
    static public FilterContainer getFilterContainer() {
        if (filterContainer == null) {
            return new FilterContainer(new IFAFileFilter());
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

    public void init() {
    }

    @Override
    public Drawing add(Drawing drawing) {
        FigureEnumeration figures = drawing.figures();
        while (figures.hasMoreElements()) {
            Figure figure = (Figure) figures.nextElement();
            add(figure);
        }
        return drawing;
    }

    @Override
    public Drawing add(Drawing drawing, int x, int y) {
        FigureEnumeration figures = drawing.figures();
        while (figures.hasMoreElements()) {
            Figure figure = (Figure) figures.nextElement();
            add(figure);
            figure.moveBy(x, y);
        }
        return drawing;
    }

    @Override
    public boolean isStorable() {
        return true;
    }
}