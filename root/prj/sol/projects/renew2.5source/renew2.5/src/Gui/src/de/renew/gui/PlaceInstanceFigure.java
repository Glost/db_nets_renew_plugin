package de.renew.gui;

import CH.ifa.draw.framework.DrawingChangeEvent;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureChangeListener;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.AbstractFigure;
import CH.ifa.draw.standard.NullHandle;
import CH.ifa.draw.standard.RelativeLocator;

import CH.ifa.draw.util.AWTSynchronizedUpdate;

import de.renew.remote.EventListener;
import de.renew.remote.ObjectAccessor;
import de.renew.remote.PlaceInstanceAccessor;
import de.renew.remote.RemoteEventForwarder;
import de.renew.remote.TokenCountsAccessor;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.Serializable;

import java.rmi.RemoteException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public class PlaceInstanceFigure extends AbstractFigure
        implements InstanceFigure, EventListener, FigureChangeListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PlaceInstanceFigure.class);
    protected PlaceFigure placeFigure;
    protected PlaceInstanceAccessor placeInstance;
    protected RemoteEventForwarder forwarder;
    private boolean isMarked;
    protected int markingAppearance;
    protected CPNInstanceDrawing drawing;
    private AWTSynchronizedUpdate updateTask;

    /**
     * Creates the place instance figure.
     * @param drawing The CPN instance drawing to create for.
     * @param pf The corresponding place figure.
     * @param netElements A lookup mapping net element group ids to net elements.
     */
    public PlaceInstanceFigure(CPNInstanceDrawing drawing, PlaceFigure pf,
                               Hashtable<Serializable, ObjectAccessor> netElements) {
        this.drawing = drawing;
        placeFigure = pf;
        this.updateTask = new AWTSynchronizedUpdate(new PlaceHighlightUpdateTask(this));
        initialize(netElements);
    }

    /**
     * Initializes the figure with the net element lookup.
     * @param netElements A lookup mapping net element group ids to net elements.
     */
    protected void initialize(Hashtable<Serializable, ObjectAccessor> netElements) {
        if (netElements.size() != 1) {
            logger.error("Error: There is not exactly one place instance for the figure "
                         + this);
            return;
        }

        Enumeration<ObjectAccessor> elems = netElements.elements();
        elems.hasMoreElements();
        placeInstance = (PlaceInstanceAccessor) elems.nextElement();

        placeFigure.addFigureChangeListener(this);
        markingAppearance = placeFigure.getMarkingAppearance();
        update();
        if (markingAppearance != PlaceFigure.HIGHLIGHT) {
            addTokenBagFigure();
        }

        try {
            forwarder = new RemoteEventForwarder(this);
            placeInstance.addRemoteEventListener(forwarder);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected PlaceInstanceAccessor getInstance() {
        return placeInstance;
    }

    public boolean isHighlighted() {
        return isMarked;
    }

    // Allow access to drawing, so that token bag figures
    // may lock the drawing.
    CPNInstanceDrawing drawing() {
        return drawing;
    }

    /**
     * Receives the {@link EventListener} events.
     * <p>
     * This method is naturally called asynchronously to the AWT event
     * queue, therefore the real update is scheduled to be executed within
     * the AWT thread. If multiple update notifications occur before the
     * update execution, those are ignored.
     * </p>
     **/
    public void update() {
        updateTask.scheduleUpdate();
    }

    protected void setHighlighted(boolean newIsMarked) {
        if (newIsMarked != isMarked) {
            isMarked = newIsMarked;
            invalidate();
            Figure hilight = placeFigure.getHighlightFigure();

            if (hilight != null) {
                Rectangle area = hilight.displayBox();

                area.grow(5, 5);
                DrawingChangeEvent dce = new DrawingChangeEvent(drawing, area);

                drawing.drawingInvalidated(dce);
            }
            if (listener() != null) {
                listener().figureRequestUpdate(new FigureChangeEvent(this));
            }
        }
    }

    public void basicDisplayBox(Point origin, Point corner) {
        // do nothing, as the DisplayBox of the Place is used!
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = new Vector<Handle>();

        handles.addElement(new NullHandle(this, RelativeLocator.northWest()));
        handles.addElement(new NullHandle(this, RelativeLocator.northEast()));
        handles.addElement(new NullHandle(this, RelativeLocator.southWest()));
        handles.addElement(new NullHandle(this, RelativeLocator.southEast()));
        return handles;
    }

    public Rectangle displayBox() {
        return placeFigure.displayBox();
    }

    protected void basicMoveBy(int x, int y) {
        // do nothing, as the DisplayBox of the Place is used!
    }

    public void draw(Graphics g) {
        // don't draw anything!
    }

    protected TokenBagFigure addTokenBagFigure() {
        TokenBagFigure tbf = new TokenBagFigure(drawing(), this, placeInstance,
                                                markingAppearance);

        drawing.add(tbf);
        return tbf;
    }

    public boolean inspect(DrawingView view, boolean alternate) {
        if (alternate) {
            try {
                ((CPNApplication) view.editor()).openTokenBagDrawing(placeInstance);
                return true;
            } catch (RemoteException e) {
                return false;
            }
        } else {
            if (getTokenBagFigure() == null) {
                view.clearSelection();
                view.addToSelection(addTokenBagFigure());
                return true;
            } else {
                return super.inspect(view, alternate);
            }
        }
    }

    public void setAttribute(String attribute, Object value) {
        if ("MarkingAppearance".equals(attribute)) {
            markingAppearance = ((Integer) value).intValue();
            TokenBagFigure etbf = getTokenBagFigure();

            if (markingAppearance == PlaceFigure.HIGHLIGHT) {
                if (etbf != null) {
                    drawing.remove(etbf);
                }
            } else {
                if (etbf == null) {
                    addTokenBagFigure();
                } else {
                    etbf.setMarkingAppearance(markingAppearance);
                }
            }
        } else {
            super.setAttribute(attribute, value);
        }
    }

    public void release() {
        CPNInstanceDrawing theDrawing = drawing;

        if (theDrawing == null) {
            // This figure is released a second time.
            return;
        }


        if (drawing == null) {
            // This figure has been released concurrently.
            return;
        }

        if (placeInstance != null) {
            try {
                placeInstance.removeRemoteEventListener(forwarder);
            } catch (RemoteException e) {
                logger.error(e.getMessage(), e);
            }


            // OK: Release reference to place instance so that it
            // can be garbage collected.
            placeInstance = null;
            drawing = null;


            // etbf=null;
            // placeFigure=null;
        }
        placeFigure.removeFigureChangeListener(this);
        super.release();

    }

    public TokenBagFigure getTokenBagFigure() {
        FigureEnumeration childenumeration = children();

        if (childenumeration.hasMoreElements()) {
            return (TokenBagFigure) childenumeration.nextElement();
        } else {
            return null;
        }
    }

    public void figureInvalidated(FigureChangeEvent e) {
    }

    public void figureChanged(FigureChangeEvent e) {
        // If my place changes, I change!
        changed();
    }

    public void figureHandlesChanged(FigureChangeEvent e) {
    }

    public void figureRemoved(FigureChangeEvent e) {
        // My place figure has been removed -- that's bad!!!
    }

    public void figureRequestRemove(FigureChangeEvent e) {
    }

    public void figureRequestUpdate(FigureChangeEvent e) {
    }

    /**
     * This class is not serializable, although inheriting
     * serializability from its superclass.
     * <p>
     * Reason: Serialization is currently used for Copy&Paste
     * and the Undo machanism only. As such modifications don't
     * make sense for instance drawings, this  figure, which is
     * a part of such drawings, doesn't need to be serializable.
     * </p><p>
     * Second reason: Some fields of this class would lead to
     * a serialization of the compiled net, which is not desirable.
     * </p>
     * @throws java.io.NotSerializableException always.
     */


    //NOTICEredundant UNUSED
    private void writeObject(java.io.ObjectOutputStream out)
            throws java.io.IOException {
        throw new java.io.NotSerializableException("PlaceInstanceFigure is not serializable!");
    }

    /**
     * An instance of this class is associated to each
     * <code>PlaceInstanceFigure</code> to handle the marking update
     * notifications coming from the simulator.
     * This class is designed to be scheduled by a {@link AWTSynchronizedUpdate} instance.
     **/
    private static class PlaceHighlightUpdateTask implements Runnable {
        private PlaceInstanceFigure figure;

        /**
         * Creates a new <code>PlaceHighlightUpdateTask</code> instance
         * associated with the given <code>figure</code>.
         *
         * @param figure  the <code>PlaceInstanceFigure</code> which should
         *                be updated by this task.
         **/
        public PlaceHighlightUpdateTask(PlaceInstanceFigure figure) {
            this.figure = figure;
        }

        /**
         * Updates the associated <code>PlaceInstanceFigure</code> as long
         * as the figure has not been released.
         **/
        public void run() {
            // Now we have to check if the update check is still valid
            // or if the figure has already been released inbetween.
            CPNInstanceDrawing theDrawing = figure.drawing();
            PlaceInstanceAccessor thePlaceInstance = figure.getInstance();
            if (theDrawing == null || thePlaceInstance == null) {
                // The figure has already been released.
                return;
            }

            try {
                theDrawing.lock();
                if (figure.drawing() == null || figure.getInstance() == null) {
                    // The figure was just now concurrently released (such
                    // a check would not be needed if all modifications
                    // occur in synchronization with the EventQueue).
                    return;
                }

                boolean newIsMarked;
                try {
                    TokenCountsAccessor tokenCountsAccessor = thePlaceInstance
                                                                  .getTokenCounts();
                    newIsMarked = !tokenCountsAccessor.isEmpty();
                } catch (RemoteException e) {
                    logger.error(e.getMessage(), e);
                    newIsMarked = false;
                }

                figure.setHighlighted(newIsMarked);
            } finally {
                theDrawing.unlock();
            }
        }
    }
}