package de.renew.fa.figures;

import CH.ifa.draw.framework.DrawingChangeEvent;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureChangeListener;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.AbstractFigure;
import CH.ifa.draw.standard.NullHandle;
import CH.ifa.draw.standard.RelativeLocator;

import CH.ifa.draw.util.AWTSynchronizedUpdate;

import de.renew.fa.FAInstanceDrawing;

import de.renew.gui.CPNInstanceDrawing;
import de.renew.gui.InstanceFigure;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TokenBagFigure;

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


public class FAStateInstanceFigure extends AbstractFigure
        implements InstanceFigure, EventListener, FigureChangeListener {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(FAStateInstanceFigure.class);
    private boolean isActive;
    protected FAStateFigure faStateFigure;
    protected PlaceInstanceAccessor placeInstance;
    protected int markingAppearance;
    protected FAInstanceDrawing drawing;
    private AWTSynchronizedUpdate updateTask;
    protected RemoteEventForwarder forwarder;

    public FAStateInstanceFigure(FAInstanceDrawing drawing, FAStateFigure faf,
                                 Hashtable<Serializable, ObjectAccessor> netElements) {
        this.faStateFigure = faf;
        this.drawing = drawing;
        this.updateTask = new AWTSynchronizedUpdate(new StateHighlightUpdateTask(this));
        initialize(netElements);
    }

    /**
     * Initializes the figure with the net element lookup.
     * @param netElements A lookup mapping net element group ids to net elements.
     */
    protected void initialize(Hashtable<Serializable, ObjectAccessor> netElements) {
        // FAStates are compiled to Places
        Enumeration<ObjectAccessor> elems = netElements.elements();
        elems.hasMoreElements();
        placeInstance = (PlaceInstanceAccessor) elems.nextElement();

        faStateFigure.addFigureChangeListener(this);
        markingAppearance = faStateFigure.getMarkingAppearance();
        update();
        if (markingAppearance != FAStateFigure.HIGHLIGHT) {
            addTokenBagFigure();
        }

        // TODO: What is this good for? Necessary?
        try {
            forwarder = new RemoteEventForwarder(this);
            placeInstance.addRemoteEventListener(forwarder);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        }
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
    @Override
    public void update() {
        updateTask.scheduleUpdate();
    }

    FAInstanceDrawing drawing() {
        return drawing;
    }

    public PlaceInstanceAccessor getInstance() {
        return placeInstance;
    }

    @Override
    public String toString() {
        return "FAStateInstanceFigure(" + faStateFigure.getID() + ")";
    }

    //------------------------------ Highlighting and mark ------------------------------  
    protected void setHighlighted(boolean newIsActive) {
        if (newIsActive != isActive) {
            isActive = newIsActive;
            invalidate();
            Figure hilight = faStateFigure.getHighlightFigure();

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

    protected TokenBagFigure addTokenBagFigure() {
        TokenBagFigure tbf = new TokenBagFigure(drawing(), this, placeInstance,
                                                markingAppearance);

        drawing.add(tbf);
        return tbf;
    }

    @Override
    public void setAttribute(String attribute, Object value) {
        logger.debug("setAttribute(String, Object) called with " + attribute
                     + " and " + value);

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

    public TokenBagFigure getTokenBagFigure() {
        logger.debug("getTokenBagFigure called");

        FigureEnumeration childenumeration = children();

        if (childenumeration.hasMoreElements()) {
            return (TokenBagFigure) childenumeration.nextElement();
        } else {
            return null;
        }
    }

    //------------------------------ Figure (with highlight) ------------------------------   
    @Override
    public void draw(Graphics g) {
        // don't draw
    }

    @Override
    protected void basicMoveBy(int dx, int dy) {
        // do nothing, as the DisplayBox of the FAState is used.		
    }

    @Override
    public void basicDisplayBox(Point origin, Point corner) {
        // do nothing, as the DisplayBox of the FAState is used.		
    }

    @Override
    public boolean isHighlighted() {
        return isActive;
    }

    @Override
    public Rectangle displayBox() {
        return faStateFigure.displayBox();
    }

    @Override
    public Vector<Handle> handles() {
        Vector<Handle> handles = new Vector<Handle>();

        handles.addElement(new NullHandle(this, RelativeLocator.northWest()));
        handles.addElement(new NullHandle(this, RelativeLocator.northEast()));
        handles.addElement(new NullHandle(this, RelativeLocator.southWest()));
        handles.addElement(new NullHandle(this, RelativeLocator.southEast()));
        return handles;
    }

    //------------------------------ FigureChangeListener ------------------------------  
    @Override
    public void figureInvalidated(FigureChangeEvent e) {
    }

    @Override
    public void figureChanged(FigureChangeEvent e) {
        // If place changes, change
        changed();

    }

    @Override
    public void figureRemoved(FigureChangeEvent e) {
    }

    @Override
    public void figureRequestRemove(FigureChangeEvent e) {
    }

    @Override
    public void figureRequestUpdate(FigureChangeEvent e) {
    }

    @Override
    public void figureHandlesChanged(FigureChangeEvent e) {
    }


    //------------------------------ HighlightUpdateTask ------------------------------  

    /**
     * An instance of this class is associated to each
     * <code>FAStateInstanceFigure</code> to handle the marking update
     * notifications coming from the simulator.
     * This class is designed to be scheduled by a {@link AWTSynchronizedUpdate} instance.
     **/
    private static class StateHighlightUpdateTask implements Runnable {
        private FAStateInstanceFigure figure;

        /**
         * Creates a new <code>StateHighlightUpdateTask</code> instance
         * associated with the given <code>figure</code>.
         *
         * @param figure  the <code>StateInstanceFigure</code> which should
         *                be updated by this task.
         **/
        public StateHighlightUpdateTask(FAStateInstanceFigure figure) {
            this.figure = figure;
        }

        /**
         * Updates the associated <code>FAStateInstanceFigure</code> as long
         * as the figure has not been released.
         **/
        @Override
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

                boolean newIsActive;
                try {
                    TokenCountsAccessor tokenCountsAccessor = thePlaceInstance
                                                                  .getTokenCounts();
                    newIsActive = !tokenCountsAccessor.isEmpty();
                    logger.debug("PlaceInstance (" + thePlaceInstance.getID()
                                 + ") is now "
                                 + (newIsActive ? "active" : "inactive"));
                } catch (RemoteException e) {
                    logger.error(e.getMessage(), e);
                    newIsActive = false;
                }

                figure.setHighlighted(newIsActive);
            } finally {
                theDrawing.unlock();
            }
        }
    }
}