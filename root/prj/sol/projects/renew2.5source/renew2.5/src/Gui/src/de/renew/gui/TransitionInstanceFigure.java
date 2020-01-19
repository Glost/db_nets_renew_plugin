package de.renew.gui;

import CH.ifa.draw.framework.DrawingChangeEvent;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.AbstractFigure;
import CH.ifa.draw.standard.NullHandle;
import CH.ifa.draw.standard.RelativeLocator;

import CH.ifa.draw.util.AWTSynchronizedUpdate;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.remote.ObjectAccessor;
import de.renew.remote.RemoteEventForwarder;
import de.renew.remote.TransitionInstanceAccessor;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.Serializable;

import java.rmi.RemoteException;

import java.util.Hashtable;
import java.util.Vector;


public class TransitionInstanceFigure extends AbstractFigure
        implements InstanceFigure, de.renew.remote.EventListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(TransitionInstanceFigure.class);
    private TransitionFigure transitionFigure;
    private TransitionInstanceAccessor transitionInstance;
    private boolean afterglow = false;
    private Thread afterglowThread = null;
    private RemoteEventForwarder forwarder;
    protected CPNInstanceDrawing drawing;
    private AWTSynchronizedUpdate updateTask;

    /**
     * Creates the transition instance figure.
     * @param drawing The CPN instance drawing to create for.
     * @param tf The corresponding transition figure.
     * @param netElements A lookup mapping net element group ids to net elements.
     */
    public TransitionInstanceFigure(CPNInstanceDrawing drawing,
                                    TransitionFigure tf,
                                    Hashtable<Serializable, ObjectAccessor> netElements) {
        this.drawing = drawing;
        transitionFigure = tf;
        initialize(netElements);
        this.updateTask = new AWTSynchronizedUpdate(new Runnable() {
                public void run() {
                    executeUpdate();
                }
            });
    }

    /**
     * Initializes the figure with the net element lookup.
     * @param netElements A lookup mapping net element group ids to net elements.
     */
    protected void initialize(Hashtable<Serializable, ObjectAccessor> netElements) {
        if (netElements.size() != 1) {
            logger.error("Error: There is not exactly one transition instance for the figure "
                         + this);
            return;
        }

        transitionInstance = (TransitionInstanceAccessor) netElements.elements()
                                                                     .nextElement();

        try {
            forwarder = new RemoteEventForwarder(this);
            transitionInstance.addRemoteEventListener(forwarder);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Returns whether the figure shall display firing state.
     * @return Whether the figure is firing.
     */
    public boolean isFiring() {
        try {
            return transitionInstance.isFiring();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isHighlighted() {
        return isFiring() || afterglow;
    }

    private void invalidateHighlight() {
        Figure highlight = transitionFigure.getHighlightFigure();

        if (highlight != null) {
            Rectangle area = highlight.displayBox();
            DrawingChangeEvent dce = new DrawingChangeEvent(drawing, area);
            drawing.drawingInvalidated(dce);
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
    public void update() {
        if (updateTask != null) {
            updateTask.scheduleUpdate();
        }
    }

    private void executeUpdate() {
        if (listener() != null) {
            if (afterglowThread != null && afterglowThread.isAlive()) {
                try {
                    afterglowThread.notifyAll();
                } catch (Exception e) {
                }
            }


            afterglow = true;
            invalidate();
            invalidateHighlight();
            listener().figureRequestUpdate(new FigureChangeEvent(this));


            final TransitionInstanceFigure thizz = this;
            afterglowThread = new Thread() {
                    public void run() {
                        try {
                            synchronized (this) {
                                this.wait(100);
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

    public void basicDisplayBox(Point origin, Point corner) {
        // do nothing, as the DisplayBox of the Transition is used!
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
        return transitionFigure.displayBox();
    }

    protected void basicMoveBy(int x, int y) {
        // do nothing, as the DisplayBox of the Transition is used!
    }

    public void draw(Graphics g) {
        /*
         * if (actionCounter > 0) {
         * g.setColor(Color.red);
         * Rectangle r = displayBox();
         * g.fillRect(r.x, r.y, r.width, r.height);
         * }
         */
    }

    public boolean inspect(DrawingView view, boolean alternate) {
        if (alternate) { // right-click
            if (fire()) {
                String name;
                try {
                    name = transitionInstance.asString();
                } catch (RemoteException e) {
                    name = e.toString();
                }
                view.editor().showStatus("Transition " + name + " fired.");
            } else {
                view.editor().showStatus("No binding for this transition.");
            }
        } else { // double-click
            logger.debug("Searching bindings for " + transitionInstance + "...");
            BindingSelectionFrame.open(transitionInstance, getSimulation());
        }
        return true;
    }

    protected CPNSimulation getSimulation() {
        //return drawing.getMode().getSimulation();
        return ModeReplacement.getInstance().getSimulation();
    }

    public boolean fire() {
        CPNSimulation simulation = getSimulation();

        simulation.getBreakpointManager().clearLog();
        boolean result;
        try {
            result = transitionInstance.fireOneBinding();
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
            result = false;
        }

        simulation.simulationRefresh();
        return result;
    }

    public void release() {
        if (transitionInstance != null) {
            try {
                transitionInstance.removeRemoteEventListener(forwarder);
            } catch (RemoteException e) {
                logger.error(e.getMessage(), e);
            }


            // OK: Release reference to transition instance so that it
            // can be garbage collected.
            transitionInstance = null;
        }
        super.release();
    }

    protected TransitionInstanceAccessor getInstance() {
        return transitionInstance;
    }
}