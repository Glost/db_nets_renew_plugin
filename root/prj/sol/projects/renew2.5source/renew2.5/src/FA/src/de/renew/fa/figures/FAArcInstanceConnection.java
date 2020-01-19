package de.renew.fa.figures;

import CH.ifa.draw.framework.DrawingChangeEvent;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.AbstractFigure;
import CH.ifa.draw.standard.NullHandle;
import CH.ifa.draw.standard.RelativeLocator;

import CH.ifa.draw.util.AWTSynchronizedUpdate;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.fa.FAInstanceDrawing;

import de.renew.gui.InstanceFigure;

import de.renew.remote.EventListener;
import de.renew.remote.ObjectAccessor;
import de.renew.remote.RemoteEventForwarder;
import de.renew.remote.TransitionInstanceAccessor;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.Serializable;

import java.rmi.RemoteException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public class FAArcInstanceConnection extends AbstractFigure
        implements InstanceFigure, EventListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FAArcInstanceConnection.class);
    private FAArcConnection faArcConnection;
    private FAInstanceDrawing drawing;
    private TransitionInstanceAccessor transitionInstance;
    private boolean afterglow = false;
    private Thread afterglowThread;
    private AWTSynchronizedUpdate updateTask;
    private RemoteEventForwarder forwarder;

    public FAArcInstanceConnection(FAInstanceDrawing drawing,
                                   FAArcConnection fac,
                                   Hashtable<Serializable, ObjectAccessor> netElements) {
        this.faArcConnection = fac;
        this.drawing = drawing;
        initialize(netElements);
        this.updateTask = new AWTSynchronizedUpdate(new Runnable() {
                @Override
                public void run() {
                    executeUpdate();
                }
            });
    }

    protected void initialize(Hashtable<Serializable, ObjectAccessor> netElements) {
        if (netElements.size() != 1) {
            logger.error("Error: There are not excactly three net elements for the figure "
                         + this);
            return;
        }


        // find the TransitionInstance of the FAArcs' net elements
        Enumeration<ObjectAccessor> faArcNetElements = netElements.elements();
        while (faArcNetElements.hasMoreElements()) {
            ObjectAccessor netElementAccessor = faArcNetElements.nextElement();
            if (netElementAccessor instanceof TransitionInstanceAccessor) {
                transitionInstance = (TransitionInstanceAccessor) netElementAccessor;
            }
        }

        // What is this good for?
        try {
            forwarder = new RemoteEventForwarder(this);
            transitionInstance.addRemoteEventListener(forwarder);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        }
    }

    //------------------------------ Firing ------------------------------------   
    /**
     * Returns whether the figure shall display firing state.
     * @return Whether the figure is firing.
     */
    public boolean isChangingState() {
        try {
            return transitionInstance.isFiring();
        } catch (RemoteException e) {
            return false;
        }
    }

    //------------------------------ Figure (with highlight) ------------------------------   
    @Override
    public void draw(Graphics g) {
        /*
         * if (actionCounter > 0) {
         * g.setColor(Color.red);
         * Rectangle r = displayBox();
         * g.fillRect(r.x, r.y, r.width, r.height);
         * }
         */
    }

    @Override
    public Rectangle displayBox() {
        return faArcConnection.displayBox();
    }

    @Override
    public void basicDisplayBox(Point origin, Point corner) {
        // do nothing, as the DisplayBox of the FAArcConnection is used!
    }

    @Override
    protected void basicMoveBy(int dx, int dy) {
        // do nothing, as the DisplayBox of the FAArcConnection is used!
    }

    @Override
    public boolean isHighlighted() {
        return isChangingState() || afterglow;
    }

    private void invalidateHighlight() {
        Figure highlight = faArcConnection.getHighlightFigure();

        if (highlight != null) {
            Rectangle area = highlight.displayBox();
            DrawingChangeEvent dce = new DrawingChangeEvent(drawing, area);
            drawing.drawingInvalidated(dce);
        }
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

    private void executeUpdate() {
        logger.debug("executeUpdate() called");
        if (listener() != null) {
//            if (afterglowThread != null && afterglowThread.isAlive()) {
//                try {
//                    afterglowThread.notifyAll();
//                } catch (Exception e) {
//                }
//            }
            afterglow = true;
            invalidate();
            invalidateHighlight();
            listener().figureRequestUpdate(new FigureChangeEvent(this));


            final FAArcInstanceConnection thizz = this;
            afterglowThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            synchronized (this) {
                                this.wait(300);
                            }


                            // If another afterglow is instantiated, the current one is
                            // interrupted. So at this point, we were not.
                            try {
                                //                  drawing.lock();
                                afterglow = false;
                                invalidate();
                                invalidateHighlight();
                                listener()
                                    .figureRequestUpdate(new FigureChangeEvent(thizz));
                            } finally {
                                //                drawing.unlock();
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                };

            SimulationThreadPool.getCurrent().execute(afterglowThread);
        }
    }

    /**
     * This event signals that a Transition Occurrence has started or completed.
     * <p>
     * This method is naturally called asynchronously to the AWT event
     * queue, therefore the real update is scheduled to be executed within
     * the AWT thread. If multiple update notifications occur before the
     * update execution, those are ignored.
     * </p>
     **/
    @Override
    public void update() {
        if (updateTask != null) {
            updateTask.scheduleUpdate();
        }
    }
}