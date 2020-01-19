package de.renew.gui;

import CH.ifa.draw.framework.DrawingChangeEvent;
import CH.ifa.draw.framework.DrawingChangeListener;
import CH.ifa.draw.framework.DrawingContext;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureWithID;

import CH.ifa.draw.standard.CompositeFigure;
import CH.ifa.draw.standard.StandardDrawing;

import CH.ifa.draw.util.StorableOutput;

import de.renew.database.TransactionSource;

import de.renew.net.NetElementID;
import de.renew.net.NetInstance;

import de.renew.remote.NetAccessor;
import de.renew.remote.NetInstanceAccessor;
import de.renew.remote.NetInstanceAccessorImpl;
import de.renew.remote.ObjectAccessor;
import de.renew.remote.PlaceInstanceAccessor;
import de.renew.remote.TransitionInstanceAccessor;

import de.renew.util.StringUtil;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.io.File;
import java.io.Serializable;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;


public class CPNInstanceDrawing extends StandardDrawing
        implements DrawingChangeListener, DrawingContext {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(CPNInstanceDrawing.class);
    public static String ID_MACRO = "$ID";
    protected static Hashtable<String, CPNInstanceDrawing> drawingsByInstance = new Hashtable<String, CPNInstanceDrawing>();
    private static HashMap<Class<?>, InstanceDrawingFactory> instanceDrawingFactories = new HashMap<Class<?>, InstanceDrawingFactory>();
    protected NetInstanceAccessor netInstance;
    protected CPNDrawing cpnDrawing;

    //    private RenewMode mode;
    protected Hashtable<FigureWithID, Figure> instanceLookup;

    protected CPNInstanceDrawing(NetInstanceAccessor netInstance,
                                 CPNDrawing drawing) throws RemoteException {
        //        this.mode = mode;
        this.cpnDrawing = drawing;
        connect(netInstance);
        cpnDrawing.addDrawingChangeListener(this);
    }

    //    public RenewMode getMode() {
    //        return mode;
    //    }


    /**
     * Get the net instance accessor associated with this drawing.
     */
    public NetInstanceAccessor getNetInstance() {
        return netInstance;
    }

    public Rectangle displayBox() {
        return super.displayBox().union(cpnDrawing.displayBox());
    }

    /**
     * Returns the instance drawing associated to the given net
     * instance accessor, creates the drawing if neccessary.
     *
     * @param netInstance the net instance accessor to look up
     *                    the drawing for
     * @return CPNInstanceDrawing which displays the given netInstance.
     * If there is currently no such drawing, one is created.
     * If there is not even any CPNDrawing matching the net
     * known to the active simulation, returns
     * <code>null</code>.
     * @exception RemoteException If an RMI problem occurred.
     */
    public static CPNInstanceDrawing getInstanceDrawing(NetInstanceAccessor netInstance)
            throws RemoteException {
        // forbid concurrent searches for instance drawings.
        synchronized (drawingsByInstance) {
            // find instance drawing:
            String key = netInstance.getNet().getName() + ":"
                         + netInstance.getID();
            if (drawingsByInstance.containsKey(key)) {
                return drawingsByInstance.get(key);
            } else {
                // find drawing to base the instance drawing on
                CPNDrawing drawing = ModeReplacement.getInstance()
                                                    .getDrawingLoader()
                                                    .getDrawing(netInstance.getNet()
                                                                           .getName());

                if (drawing != null) {
                    CPNInstanceDrawing d = null;
                    InstanceDrawingFactory idf = instanceDrawingFactories.get(drawing
                                                                              .getClass());
                    if (idf != null) {
                        d = idf.getInstanceDrawing(netInstance, drawing);
                    }
                    if (d == null) {
                        d = new CPNInstanceDrawing(netInstance, drawing);
                    }
                    return d;
                } else {
                    String msg = "Cannot display net instance \""
                                 + netInstance.asString()
                                 + "\": net drawing not found.";
                    logger.error(msg);
                    JOptionPane.showMessageDialog(null,
                                                  msg
                                                  + "\nPlease load the net drawing and try again.",
                                                  "Renew",
                                                  JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
        }
    }

    /**
     * Returns all instance drawings based on the specified CPNDrawing.
     * Returns <code>null</code>, if no such drawing exists.
     */
    public static Enumeration<CPNInstanceDrawing> getDependentInstanceDrawings(CPNDrawing baseDrawing) {
        Enumeration<CPNInstanceDrawing> allDrawings = drawingsByInstance
                                                          .elements();
        Vector<CPNInstanceDrawing> dependentDrawings = new Vector<CPNInstanceDrawing>();

        while (allDrawings.hasMoreElements()) {
            CPNInstanceDrawing oneDrawing = allDrawings.nextElement();

            if (oneDrawing.cpnDrawing == baseDrawing) {
                dependentDrawings.addElement(oneDrawing);
            }
        }

        if (dependentDrawings.size() > 0) {
            return dependentDrawings.elements();
        } else {
            return null;
        }
    }

    public void draw(Graphics g) {
        // g.setColor(Color.yellow.brighter());
        // Rectangle b=displayBox();
        // g.fillRect(b.x,b.y,b.width,b.height);
        cpnDrawing.draw(g, this);
        super.draw(g);
    }

    public InstanceFigure getInstanceFigure(Figure fig) {
        try {
            return (InstanceFigure) instanceLookup.get(fig);
        } catch (Exception e) {
            return null;
        }
    }

    public InstanceFigure getInstanceFigureOfFigureWithID(int id) {
        Figure baseFigure = cpnDrawing.getFigureWithID(id);
        if (baseFigure != null) {
            return getInstanceFigure(baseFigure);
        }
        return null;
    }

    private boolean isEmphasized(Figure fig) {
        FigureWithHighlight node = cpnDrawing.getFigureForHighlight(fig);

        if (node != null) {
            fig = node;
        }
        InstanceFigure instFig = getInstanceFigure(fig);

        if (instFig != null) {
            return instFig.isHighlighted();
        }
        return false;
    }

    /**
     * Returns whether the CPN instance drawing shows a local net instance.
     * Otherwise, the simulation of the shown net instance is run in a
     * different VM, usually on another computer.
     * @return Whether the shown net instance is local.
     */
    public boolean isLocal() {
        return netInstance instanceof NetInstanceAccessorImpl;
    }

    public boolean isHighlighted(Figure fig) {
        return fig.isVisible() && isEmphasized(fig);
    }

    public boolean isVisible(Figure fig) {
        return (fig.isVisible() || isEmphasized(fig))
               && (!((fig instanceof CPNTextFigure
                     && ((CPNTextFigure) fig).getType() == CPNTextFigure.INSCRIPTION
                     && ((CPNTextFigure) fig).parent() instanceof PlaceFigure)
                  || fig == cpnDrawing.getIconFigure()));
    }

    public String expandMacro(String text) {
        return expandMacro(text, netInstance);
    }

    public static String expandMacro(String text, NetInstanceAccessor instance) {
        try {
            return StringUtil.replace(text, ID_MACRO, instance.getID());
        } catch (RemoteException e) {
            return e.toString();
        }
    }

    /**
     * Creates instance figures in this instance drawing which
     * reflect the net elements of the given net instance.
     * Used by the (private) constructor to build an instance
     * drawing.
     * <p>
     * Also... <ul>
     * <li> builds the instance lookup table
     * (see <code>getInstanceFigure()</code>) </li>
     * <li> registers this instance drawing in the
     * (private) drawingsByInstance map
     * (see <code>getInstanceDrawing()</code>) </li>
     * </ul></p>
     * May be more, but that is what I could see in the code.
     *
     * @param netInstance the net instance to be displayed
     * @see #getInstanceFigure
     * @see #getInstanceDrawing
     * @exception RemoteException If an RMI problem occurred.
     */
    public void connect(NetInstanceAccessor netInstance)
            throws RemoteException {
        if (netInstance != null) {
            if (this.netInstance != null) {
                String key = netInstance.getNet().getName() + ":"
                             + netInstance.getID();
                drawingsByInstance.remove(key);
                notifyTransactionStrategyAboutRelease();
            }

            this.netInstance = netInstance;
            String key = netInstance.getNet().getName() + ":"
                         + netInstance.getID();
            drawingsByInstance.put(key, this);
            notifyTransactionStrategyAboutConnect();

            setName(netInstance.asString());
            instanceLookup = new Hashtable<FigureWithID, Figure>();

            NetInstanceElementLookup netElementLookup = buildNetElementLookup(netInstance);
            Enumeration<Figure> figures = netElementLookup.getFigures();

            while (figures.hasMoreElements()) {
                FigureWithID figure = (FigureWithID) figures.nextElement();

                Hashtable<Serializable, ObjectAccessor> netElements = netElementLookup
                                                                      .getNetElements(figure);
                if (netElements != null) {
                    if (figure instanceof PlaceFigure) {
                        Figure pif = ((PlaceFigure) figure).createInstanceFigure(this,
                                                                                 netElements);
                        instanceLookup.put(figure, pif);
                        add(pif);
                        sendToBack(pif);
                    } else if (figure instanceof TransitionFigure) {
                        Figure tif = ((TransitionFigure) figure)
                                         .createInstanceFigure(this, netElements);
                        instanceLookup.put(figure, tif);
                        add(tif);
                    }
                }
            }
        }
    }

    public static void registerInstanceDrawingFactory(Class<?> key,
                                                      InstanceDrawingFactory factory) {
        instanceDrawingFactories.put(key, factory);
    }

    /**
     * Tries to map graphical figures of an open CPNDrawing
     * to net elements of the given net instance.
     * @param netInstance The net instance.
     * @return The net element, mapping figures to mappings
     * of group ids to net elements.
     * @exception RemoteException If an RMI problem occurred.
     */
    protected NetInstanceElementLookup buildNetElementLookup(NetInstanceAccessor netInstance)
            throws RemoteException {
        NetAccessor net = netInstance.getNet();
        String netName = net.getName();
        CPNDrawing netDrawing = ModeReplacement.getInstance().getDrawingLoader()
                                               .getDrawing(netName);
        if (netDrawing == null) {
            logger.error("No open drawing found matching the compiled net \""
                         + netName + "\"");
            return null;
        }


        // Get a lookup for the figures.
        // It maps figure ids to lists of figures.
        Hashtable<Integer, List<Figure>> figureLookup = new Hashtable<Integer, List<Figure>>();
        addToFigureLookup(netDrawing, figureLookup);

        boolean missingFigure = false;
        boolean missingNetElement = false;
        NetInstanceElementLookup netElementLookup = new NetInstanceElementLookup();

        // Fill the net element lookup with places
        NetElementID[] placeIDs = net.getPlaceIDs();
        for (int i = 0; i < placeIDs.length; i++) {
            List<Figure> figures = figureLookup.get(new Integer(placeIDs[i]
                                       .getFigureID()));
            if (figures == null) {
                logger.warn("The place with figure id "
                            + placeIDs[i].getFigureID()
                            + " has no corresponding graphical element.");
                missingFigure = true;
            } else {
                Iterator<Figure> iterator = figures.iterator();
                while (iterator.hasNext()) {
                    FigureWithID figure = (FigureWithID) iterator.next();
                    netElementLookup.put(figure,
                                         netInstance.getPlaceInstance(placeIDs[i]));
                }
            }
        }

        // Fill the net element lookup with transitions
        NetElementID[] transitionIDs = net.getTransitionIDs();
        for (int i = 0; i < transitionIDs.length; i++) {
            List<Figure> figures = figureLookup.get(new Integer(transitionIDs[i]
                                                                .getFigureID()));
            if (figures == null) {
                logger.warn("The transition with figure id "
                            + transitionIDs[i].getFigureID()
                            + " has no corresponding graphical element.");
                missingFigure = true;
            } else {
                Iterator<Figure> iterator = figures.iterator();
                while (iterator.hasNext()) {
                    FigureWithID figure = (FigureWithID) iterator.next();
                    netElementLookup.put(figure,
                                         netInstance.getTransitionInstance(transitionIDs[i]));
                }
            }
        }


        // Check if any figure has no net elements
        Enumeration<List<Figure>> figuresEnum = figureLookup.elements();
        while (figuresEnum.hasMoreElements()) {
            Iterator<Figure> iterator = figuresEnum.nextElement().iterator();
            while (iterator.hasNext()) {
                FigureWithID figure = (FigureWithID) iterator.next();
                Hashtable<Serializable, ObjectAccessor> groupIdToNetElementMap = netElementLookup
                                                                                 .getNetElements(figure);
                if (groupIdToNetElementMap == null
                            || groupIdToNetElementMap.isEmpty()) {
                    logger.warn(figure.getClass().getName() + " with id "
                                + figure.getID()
                                + " has no corresponding net element.");
                    missingNetElement = true;
                }
            }
        }

        if (missingFigure) {
            logger.warn("The compiled net"
                        + " contains one or more net elements with no"
                        + " corresponding graphical element in the drawing \""
                        + netName + "\".");
            logger.warn("These net elements will not be displayed.");
        }
        if (missingNetElement) {
            logger.warn("The drawing \"" + netName
                        + "\" contains one or more graphical"
                        + " elements with no corresponding net element.");
            logger.warn("These graphical elements will not be functional.");
        }

        return netElementLookup;
    }

    /**
     * Builds up a mapping for figure ids to figures for the elements
     * of a given composite figure.
     * @param container The composite figure.
     * @param figureLookup The figure lookup to be filled with the mapping.
     */
    protected void addToFigureLookup(CompositeFigure container,
                                     Hashtable<Integer, List<Figure>> figureLookup) {
        FigureEnumeration figuresEnum = container.figures();
        while (figuresEnum.hasMoreElements()) {
            Figure figure = figuresEnum.nextElement();
            if (figure instanceof CompositeFigure) {
                addToFigureLookup((CompositeFigure) figure, figureLookup);
            } else if (figure instanceof PlaceFigure) {
                Integer id = new Integer(((PlaceFigure) figure).getSemanticPlaceFigure()
                                          .getID());
                List<Figure> figures = figureLookup.get(id);
                if (figures == null) {
                    figures = new ArrayList<Figure>();
                    figureLookup.put(id, figures);
                }
                figures.add(figure);
            } else if (figure instanceof TransitionFigure) {
                Integer id = new Integer(((TransitionFigure) figure).getID());
                List<Figure> figures = figureLookup.get(id);
                if (figures == null) {
                    figures = new ArrayList<Figure>();
                    figureLookup.put(id, figures);
                }
                figures.add(figure);
            }
        }
    }

    public void release() {
        cpnDrawing.removeDrawingChangeListener(this);
        if (netInstance != null) {
            try {
                String key = netInstance.getNet().getName() + ":"
                             + netInstance.getID();
                drawingsByInstance.remove(key);
            } catch (RemoteException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(null,
                                              "A problem occurred: " + e + "\n"
                                              + "See the console for details.",
                                              "Renew", JOptionPane.ERROR_MESSAGE);
            }

            notifyTransactionStrategyAboutRelease();


            // logger.debug("Instance Drawing removed from list.");
            instanceLookup = null;
            netInstance = null;
        }
        super.release();
    }

    /**
     * Notifies the TransactionSource about the connect of
     * the current netInstance.
     */
    protected void notifyTransactionStrategyAboutConnect() {
        try {
            TransactionSource.netInstanceDrawingOpened(netInstance.getID());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(null,
                                          "A problem occurred: " + e + "\n"
                                          + "See the console for details.",
                                          "Renew", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Notifies the TransactionSource about the release of
     * the current netInstance.
     */
    protected void notifyTransactionStrategyAboutRelease() {
        try {
            TransactionSource.netInstanceDrawingClosed(netInstance.getID());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(null,
                                          "A problem occurred: " + e + "\n"
                                          + "See the console for details.",
                                          "Renew", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Sent when an area is invalid
     */
    public void drawingInvalidated(DrawingChangeEvent e) {
        // logger.debug("Source invalidated: "+e.getInvalidatedRectangle());
        DrawingChangeEvent myEvent = new DrawingChangeEvent(this,
                                                            e
                                         .getInvalidatedRectangle());
        Enumeration<DrawingChangeListener> listeners = drawingChangeListeners();

        while (listeners.hasMoreElements()) {
            DrawingChangeListener listener = listeners.nextElement();

            listener.drawingInvalidated(myEvent);
            listener.drawingRequestUpdate(myEvent);
        }
    }

    /**
     * Sent when the drawing wants to be refreshed
     */
    public void drawingRequestUpdate(DrawingChangeEvent e) {
        // logger.debug("Source requests update: "+e.getInvalidatedRectangle());
    }

    /**
     * {@inheritDoc}
     * <p>
     * In the case of CPNInstanceDrawing, this method prohibits the
     * removal of any figures.
     * </p>
     **/
    public void figureRequestRemove(FigureChangeEvent figureChangeEvent) {
        logger.error("Figures from net instance drawings may not be removed.");
        return;
    }

    /**
     * Returns whether drawing has been modified since last save.
     */
    public boolean isModified() {
        return false; // an InstanceDrawing should not be saved!
    }

    public Dimension defaultSize() {
        return cpnDrawing.defaultSize();
    }

    public String getWindowCategory() {
        if (isLocal()) {
            return "Net instances";
        } else {
            return "Remote net instances";
        }
    }

    /**
     * Acquires the drawing lock of this drawing and the
     * background drawing. The background drawing is locked first.
     */
    public void lock() {
        cpnDrawing.lock();
        super.lock();
    }

    /**
     * Releases the drawing lock of this drawing and the
     * background drawing.
     */
    public void unlock() {
        super.unlock();
        cpnDrawing.unlock();
    }

    /**
     * This class is not serializable, although inheriting
     * serializability from its superclass.
     * <p>
     * Reason: Serialization is currently used for Copy&Paste
     * and the Undo machanism only. As such modifications don't
     * make sense for instance drawings, this class doesn't
     * need to be serializable.
     * </p><p>
     * Second reason: Some fields of this class would lead to
     * a serialization of the compiled net, which is not desirable.
     * </p>
     * @param out UNUSED
     * @throws java.io.NotSerializableException always.
     */
    private void writeObject(java.io.ObjectOutputStream out)
            throws java.io.IOException {
        throw new java.io.NotSerializableException("CPNInstanceDrawing is not serializable!");
    }

    /**
     * Gathers all currently known <code>NetInstances</code>s
     * of the currently running local simulation.
     * The returned information is <b>not</b>
     * guaranteed to describe the complete current simulation
     * state!
     * A <code>NetInstance</code> is known, if it is displayed
     * by a <code>CPNInstanceDrawing</code>.
     * Net instances of remote simulations are ignored.
     * <p>
     * <b>Caution:</b> In order to get consistent data, you have
     * to ensure that there are no concurrent modifications of
     * the simulation state.
     * This method is not able to lock the simulation.
     * </p>
     * Added Feb 29 2000  Michael Duvigneau
     */
    public static NetInstance[] getAllLocalInstances() {
        Enumeration<CPNInstanceDrawing> enumeration;
        Vector<Object> saveList;
        CPNInstanceDrawing drawing;

        // Determine which net instances are to be saved.
        // Go through all instance drawings and extract references to
        // local net instances.
        saveList = new Vector<Object>(drawingsByInstance.size());
        enumeration = drawingsByInstance.elements();
        while (enumeration.hasMoreElements()) {
            drawing = enumeration.nextElement();
            if (drawing.isLocal()) {
                saveList.addElement(((NetInstanceAccessorImpl) drawing
                    .getNetInstance()).getObject());
            }
        }

        return saveList.toArray(new NetInstance[saveList.size()]);
    }

    /**
     * The net instance element lookup class maps figures to net instance
     * elements. Since there may be several net instance elements for one
     * figure, the figures are mapped to Hashtables of group id and net
     * instance element pairs. The class provides functionality for x to 1
     * mappings like for places and transitions, and functionality for x
     * to n mappings like tasks and other aggregations. x usually is 1 in
     * these cases, but n is also supported, of course.
     */
    public class NetInstanceElementLookup {
        /**
         * The lookup mapping figures to hashtables, which map
         * net element group ids to net instance elements.
         */
        Hashtable<Figure, Hashtable<Serializable, ObjectAccessor>> lookup;

        /**
         * Creates a new net instance element lookup.
         */
        private NetInstanceElementLookup() {
            lookup = new Hashtable<Figure, Hashtable<Serializable, ObjectAccessor>>();
        }

        /**
         * Returns an enumeration of all figures in this lookup.
         * @return The enumeration.
         */
        public Enumeration<Figure> getFigures() {
            return lookup.keys();
        }

        /**
         * For a given figure, it returns a lookup that maps net
         * element group ids to net elements.
         * @param figure The figure.
         * @return The lookup.
         */
        public Hashtable<Serializable, ObjectAccessor> getNetElements(FigureWithID figure) {
            return lookup.get(figure);
        }

        /**
         * Adds a figure to place instance relation to the lookup.
         * @param figure The figure.
         * @param placeInstance The place instance.
         * @exception RemoteException If an RMI problem occurred.
         */
        private void put(FigureWithID figure,
                         PlaceInstanceAccessor placeInstance)
                throws RemoteException {
            Hashtable<Serializable, ObjectAccessor> groupIdToNetElementMap = getNetElements(figure);
            if (groupIdToNetElementMap == null) {
                groupIdToNetElementMap = new Hashtable<Serializable, ObjectAccessor>();
                lookup.put(figure, groupIdToNetElementMap);
            }
            groupIdToNetElementMap.put(placeInstance.getPlace().getID()
                                                    .getGroupID(), placeInstance);
        }

        /**
         * Adds a figure to transition instance relation to the lookup.
         * @param figure The figure.
         * @param transitionInstance The transition instance.
         * @exception RemoteException If an RMI problem occurred.
         */
        private void put(FigureWithID figure,
                         TransitionInstanceAccessor transitionInstance)
                throws RemoteException {
            Hashtable<Serializable, ObjectAccessor> groupIdToNetElementMap = getNetElements(figure);
            if (groupIdToNetElementMap == null) {
                groupIdToNetElementMap = new Hashtable<Serializable, ObjectAccessor>();
                lookup.put(figure, groupIdToNetElementMap);
            }
            groupIdToNetElementMap.put(transitionInstance.getTransition().getID()
                                                         .getGroupID(),
                                       transitionInstance);
        }
    }

    /**
     * @return Returns the filename of the drawing template.
     */
    public File getFilename() {
        return cpnDrawing.getFilename();
    }

    @Override
    public boolean isStorable() {
        return false;
    }

    @Override
    public void write(StorableOutput dw) {
        // do nothing. CPNInstanceDrawing is not storable.
        logger.warn(CPNInstanceDrawing.class.getSimpleName()
                    + ": CPNInstanceDrawing is not storable.");
    }
}