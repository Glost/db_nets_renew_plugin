package de.renew.fa;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureWithID;

import CH.ifa.draw.standard.CompositeFigure;

import de.renew.fa.figures.FAArcConnection;
import de.renew.fa.figures.FAStateFigure;

import de.renew.gui.CPNDrawing;
import de.renew.gui.CPNInstanceDrawing;

import de.renew.remote.NetInstanceAccessor;
import de.renew.remote.ObjectAccessor;

import java.io.Serializable;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


@SuppressWarnings("serial")
public class FAInstanceDrawing extends CPNInstanceDrawing {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FAInstanceDrawing.class);

    protected FAInstanceDrawing(NetInstanceAccessor netInstance,
                                CPNDrawing drawing) throws RemoteException {
        super(netInstance, drawing);
    }

    /**
     * Creates instance figures in this instance drawing which reflect the net
     * elements of the given net instance. Used by the (private) constructor to
     * build an instance drawing.
     * <p>
     * Also...
     * <ul>
     * <li>builds the instance lookup table (see
     * <code>getInstanceFigure()</code>)</li>
     * <li>registers this instance drawing in the (private) drawingsByInstance
     * map (see <code>getInstanceDrawing()</code>)</li>
     * </ul>
     * </p>
     * May be more, but that is what I could see in the code.
     *
     * @param netInstance
     *            the net instance to be displayed
     * @see #getInstanceFigure
     * @see #getInstanceDrawing
     * @exception RemoteException
     *                If an RMI problem occurred.
     */
    @Override
    public void connect(NetInstanceAccessor netInstance)
            throws RemoteException {
        if (netInstance != null) {
            // ---- manage drawingsByInstance map ----
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
            // ---- ----
            setName(netInstance.asString());
            instanceLookup = new Hashtable<FigureWithID, Figure>();

            NetInstanceElementLookup netElementLookup = buildNetElementLookup(netInstance);
            Enumeration<Figure> figures = netElementLookup.getFigures();

            // Create InstanceFigures for each Figure
            logger.debug("Creating InstanceFigures");
            while (figures.hasMoreElements()) {
                FigureWithID figure = (FigureWithID) figures.nextElement();


                // get NetElements for that figure
                Hashtable<Serializable, ObjectAccessor> netElements = netElementLookup
                                                                      .getNetElements(figure);
                if (netElements != null) {
                    if (figure instanceof FAStateFigure) {
                        Figure sif = ((FAStateFigure) figure)
                                         .createInstanceFigure(this, netElements);
                        instanceLookup.put(figure, sif);
                        add(sif);
                        sendToBack(sif);
                    } else if (figure instanceof FAArcConnection) {
                        Figure aic = ((FAArcConnection) figure)
                                         .createInstanceFigure(this, netElements);
                        instanceLookup.put(figure, aic);
                        add(aic);
                        sendToBack(aic);
                    }
                }
            }
        }
    }

    /**
     * Builds up a mapping of figure IDs to all figures with that ID (including
     * CompositeFigures).
     *
     * @param container
     *            The composite figure
     * @param figureLookup
     *            The figure lookup to be filled with the mapping
     */
    @Override
    // Override as superclass does not add FAStateFigures
    protected void addToFigureLookup(CompositeFigure container,
                                     Hashtable<Integer, List<Figure>> figureLookup) {
        FigureEnumeration figuresEnum = container.figures();

        while (figuresEnum.hasMoreElements()) {
            Figure figure = figuresEnum.nextElement();

            // Add CompositeFigure container + components
            if (figure instanceof CompositeFigure) {
                // recursively call for subcomponents
                addToFigureLookup((CompositeFigure) figure, figureLookup);
            }
            // Add FAStateFigures
            else if (figure instanceof FAStateFigure) {
                Integer id = new Integer(((FAStateFigure) figure).getID());
                List<Figure> figures = figureLookup.get(id);

                if (figures == null) {
                    // create new list, as there was none found
                    figures = new ArrayList<Figure>();
                    figureLookup.put(id, figures);
                }
                figures.add(figure);

                logger.debug("Added " + figure + " to figureLookup");
            }
            // Add FAArcConnection
            else if (figure instanceof FAArcConnection) {
                Integer id = new Integer(((FAArcConnection) figure).getID());
                List<Figure> figures = figureLookup.get(id);
                if (figures == null) {
                    figures = new ArrayList<Figure>();
                    figureLookup.put(id, figures);
                }
                figures.add(figure);

                logger.debug("Added " + figure + " to figureLookup");
            }
        }
    }
}