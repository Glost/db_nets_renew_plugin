/*
 * Created on Apr 26, 2003
 */
package de.renew.diagram.peer;

import CH.ifa.draw.figures.AttributeFigure;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.standard.StandardDrawingView;

import de.renew.diagram.DiagramTextFigure;
import de.renew.diagram.IDiagramElement;
import de.renew.diagram.MessageConnection;
import de.renew.diagram.PaletteCreatorPlugin;
import de.renew.diagram.RightConnector;
import de.renew.diagram.TailFigure;
import de.renew.diagram.TaskFigure;
import de.renew.diagram.VJoinFigure;
import de.renew.diagram.VSplitFigure;

import de.renew.gui.ArcConnection;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TransitionFigure;

import java.awt.Point;

import java.io.Serializable;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;


/**
 * @author Lawrence Cabac
 */
public class DrawingPeer implements Serializable, IDrawingPeer {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(DrawingPeer.class);
    protected Vector<Figure> figuresV;
    protected Point location;
    protected Vector<Point> nextLocations;
    protected Vector<Figure> startFigures;
    protected Vector<Figure> endFigures;
    static final int XOFFSET = 300;
    static final int YOFFSET = 200;
    private TailFigure owner;
    private String name;

    /**
     * Empty constructor for subclasses.
     */
    public DrawingPeer(TailFigure owner, Point location, String fileName) {
        this.name = fileName;
        this.owner = owner;
        this.location = location;
    }

    /**
     * Empty constructor for use in DCDrawingPeer
     */
    public DrawingPeer() {
    }

    public DrawingPeer(Point location, String fileName, TailFigure owner,
                       IDiagramElement originator) {
        this.owner = owner;
        this.location = location;
        this.name = fileName;
        nextLocations = new Vector<Point>();

        NCLoader io = PaletteCreatorPlugin.getNCLoader();
        figuresV = new Vector<Figure>();
        FigureEnumeration enumeration = io.getfigures(fileName);
        while (enumeration != null && enumeration.hasMoreElements()) {
            figuresV.add(enumeration.nextElement());
        }
        startFigures = findStartConnectionTransitions(figuresV);
        endFigures = findEndConnectionPlaces(figuresV);
        int plusx = 0;
        int plusy = 0;


        // if (fileName.equals("start.rnw")) {
        // plusx = 150;
        // plusy = 300;
        // }
        if (this.getOwner() instanceof VSplitFigure) {
            logger.debug("===============================================");
            logger.debug("VSplitFigure");
            logger.debug("===============================================");

            plusy = YOFFSET;
            nextLocations.add(new Point(location.x + XOFFSET + plusx,
                                        location.y + plusy));
            nextLocations.add(new Point(location.x + XOFFSET + plusx,
                                        location.y - plusy));

            if ("cond.rnw".equals(name)) {
                logger.debug("DrawingPeer: encountered cond component.");
                TextFigure textFigure = getSelectedInscriptionFigure(figuresV,
                                                                     "$booleanCondition");
                if ((textFigure != null)
                            && (this.getOwner().children().hasMoreElements())) {
                    DiagramTextFigure dtf = (DiagramTextFigure) this.getOwner()
                                                                    .children()
                                                                    .nextFigure();
                    String actionText = dtf.getRealText();

                    logger.debug("  DrawingPeer: replaced text: " + actionText);
                    if (actionText.matches("[.*]")) {
                        actionText = actionText.substring(1,
                                                          actionText.length()
                                                          - 2);
                    }
                    if (!actionText.startsWith("cond =")) {
                        actionText = "cond = " + actionText;
                    }
                    textFigure.setText(actionText);
                }
            }
        } else if (this.getOwner() instanceof VJoinFigure) {
            logger.debug("===============================================");
            logger.debug("VJoinFigure");
            logger.debug("===============================================");


            nextLocations.add(new Point(location.x + XOFFSET + plusx,
                                        location.y + plusy));


        } else if (this.getOwner() instanceof TaskFigure) {
            if (logger.isDebugEnabled()) {
                logger.debug("\n===============================================");
                logger.debug("TaskFigure");
                logger.debug("===============================================\n");
            }
            if (location == null) {
                location = new Point(0, 0);
            }
            int extra = 0;
            if (name.endsWith("exchange.rnw")) {
                extra = 100;
            }
            nextLocations.add(new Point(location.x + XOFFSET + plusx + extra,
                                        location.y + plusy));
            if ("out.rnw".equals(name)) {
                logger.debug("DrawingPeer: encountered out component.");
                TextFigure textFigure = getSelectedInscriptionFigure(figuresV,
                                                                     "$generateMessage");
                if (textFigure != null) {
                    logger.debug("  DrawingPeer: found textFigure with text: $generateMessage");
                    if (originator instanceof MessageConnection) {
                        MessageConnection message = (MessageConnection) originator;
                        if (message.getMessageText() != null) {
                            if (message.getMessageText().startsWith("action")) {
                                // Text in diagram starts with keyword action, we presume
                                // that this is Java code:
                                textFigure.setText(message.getMessageText());
                                logger.debug("  DrawingPeer: replaced text: "
                                             + message.getMessageText());

                            } else {
                                // Otherwise we presume it is a message content (standard settings request)
                                textFigure.setText("action p2 = Sl0Creator.createActionRequest(\n   aid,\n   "
                                                   + message.getMessageText()
                                                   + ")");
                                logger.debug("  DrawingPeer: replaced text: "
                                             + "action p2 = Sl0Creator.createActionRequest(\n   aid,\n   "
                                             + message.getMessageText() + ")");


                            }
                        } else {
                            textFigure.setText("action p2=Sl0Creator.createActionRequest(aid,\"content\")");
                        }
                    } else {
                        logger.warn("DrawingPeer: Could not find text figure for outgoing message inscription.");
                    }
                }
            } else if ("sequence-template.rnw".equals(name)) {
                logger.debug("DrawingPeer: encountered sequence component.");
                TextFigure textFigure = getSelectedInscriptionFigure(figuresV,
                                                                     "$sequenceAction");
                if (textFigure != null) {
                    DiagramTextFigure dtf = (DiagramTextFigure) originator;
                    String actionText = dtf.getRealText();
                    Object attr = dtf.getAttribute(DiagramTextFigure.IS_HIDDEN);
                    if (attr != null && (Boolean) attr) {
                        actionText = (String) dtf.getAttribute(DiagramTextFigure.HIDDEN_TEXT);
                    }
                    logger.debug("  DrawingPeer: replaced text: " + actionText);
                    textFigure.setText(":access(kb);\n" + actionText);
                }
            } else if ("stop.rnw".equals(name)) {
                logger.debug("DrawingPeer: encountered stop component.");
                TextFigure textFigure = getSelectedInscriptionFigure(figuresV,
                                                                     "$sequenceAction");
                if (textFigure != null) {
                    DiagramTextFigure dtf = (DiagramTextFigure) originator;
                    String actionText = dtf.getRealText();
                    Object attr = dtf.getAttribute(DiagramTextFigure.IS_HIDDEN);
                    if (attr != null && (Boolean) attr) {
                        actionText = (String) dtf.getAttribute(DiagramTextFigure.HIDDEN_TEXT);
                    }
                    logger.debug("  DrawingPeer: replaced text: " + actionText);
                    if (actionText.equals("stop")
                                || actionText.equals(":stop()")) {
                        textFigure.setText(":stop()");
                    }
                }
            } else if ("manual.rnw".equals(name)) {
                logger.debug("DrawingPeer: encountered stop component.");
                TextFigure textFigure = getSelectedInscriptionFigure(figuresV,
                                                                     "$sequenceAction");
                if (textFigure != null) {
                    DiagramTextFigure dtf = (DiagramTextFigure) originator;
                    String actionText = dtf.getRealText();
                    Object attr = dtf.getAttribute(DiagramTextFigure.IS_HIDDEN);
                    if (attr != null && (Boolean) attr) {
                        actionText = (String) dtf.getAttribute(DiagramTextFigure.HIDDEN_TEXT);
                    }
                    logger.debug("  DrawingPeer: replaced text: " + actionText);
                    if (actionText.equals("manual")
                                || actionText.equals("manual;")) {
                        textFigure.setText("manual");
                    }
                }
            } else if (name.endsWith("exchange.rnw")) {
                logger.debug("DrawingPeer: encountered exchange component.");
                TextFigure textFigure = getSelectedInscriptionFigure(figuresV,
                                                                     "$descr");
                if (textFigure != null) {
                    DiagramTextFigure dtf = (DiagramTextFigure) originator;
                    String actionText = dtf.getRealText();
                    Object attr = dtf.getAttribute(DiagramTextFigure.IS_HIDDEN);
                    if (attr != null && (Boolean) attr) {
                        actionText = (String) dtf.getAttribute(DiagramTextFigure.HIDDEN_TEXT);
                    }
                    if (actionText.startsWith("simple ")) {
                        actionText = actionText.substring(7);
                    } else if (actionText.startsWith("simple")) {
                        actionText = actionText.substring(6);
                    }
                    logger.debug("  DrawingPeer: replaced text: " + actionText);
                    textFigure.setText(actionText);
                }
            }
        } else {
            nextLocations.add(new Point(location.x + XOFFSET + plusx,
                                        location.y + plusy));
        }
        logger.debug("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        logger.debug("Name " + fileName);
        logger.debug("location " + location);
        logger.debug("nextLoc " + nextLocations.firstElement());
        logger.debug(" ");
        logger.debug(" ");
        logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");


    }

    /**
     * @param figuresV
     * @param string
     * @return
     */
    private TextFigure getSelectedInscriptionFigure(Vector<Figure> figuresV,
                                                    String string) {
        return getSelectedInscription(figuresV, string);
    }

    /* (non-Javadoc)
     * @see de.renew.diagram.peer.IDrawingPeer#getFigures()
     */
    @Override
    public Vector<Figure> getFigures() {
        return figuresV;
    }

    /* (non-Javadoc)
     * @see de.renew.diagram.peer.IDrawingPeer#getLocation()
     */
    @Override
    public Point getLocation() {
        return location;
    }

    /* (non-Javadoc)
     * @see de.renew.diagram.peer.IDrawingPeer#setLocation(java.awt.Point)
     */
    @Override
    public void setLocation(Point point) {
        location = point;
    }

    /* (non-Javadoc)
     * @see de.renew.diagram.peer.IDrawingPeer#getNextLocations()
     */
    @Override
    public Vector<Point> getNextLocations() {
        return nextLocations;
    }

    // abstract public DiagramTextFigure inscription();
    /* (non-Javadoc)
     * @see de.renew.diagram.peer.IDrawingPeer#getStartFigures()
     */
    @Override
    public Vector<Figure> getStartFigures() {
        return startFigures;
    }

    /* (non-Javadoc)
     * @see de.renew.diagram.peer.IDrawingPeer#getEndFigures()
     */
    @Override
    public Vector<Figure> getEndFigures() {
        return endFigures;
    }

    /* (non-Javadoc)
     * @see de.renew.diagram.peer.IDrawingPeer#drawFigures(CH.ifa.draw.framework.DrawingView)
     */
    @Override
    public void drawFigures(DrawingView view) {
        view.addAll(figuresV);
        view.addToSelectionAll(figuresV);
        StandardDrawingView.moveFigures(figuresV, getLocation().x,
                                        getLocation().y);
        view.clearSelection();
        view.checkDamage();
    }

    /* (non-Javadoc)
     * @see de.renew.diagram.peer.IDrawingPeer#connectFigures(CH.ifa.draw.framework.DrawingView)
     */
    @Override
    public void connectFigures(DrawingView view) {
        if (!this.toString().equals("start.rnw")) {
            connectToPreceding(view);
            view.checkDamage();
        }
    }

    /**
     * @param view The current DrawingView, i.e. the view associated to the
     *            DiagramDrawing.
     */
    private void connectToPreceding(DrawingView view) {
        AttributeFigure start = null;
        Vector<Connector> v = this.getOwner().getDParentConnectors(); // Connectors of the parent

        // diagram figures
        int index = 0;
        if (v.size() > 0) {
            Connector con = v.firstElement();
            if (con instanceof RightConnector) {
                index++;
            }


            // get the correct place marked with a ">" of the last peer of the
            // parent
            Vector<Figure> endFigures2 = this.getOwner().getPrecedingPeer(this)
                                             .getEndFigures();
            try {
                start = (AttributeFigure) endFigures2.elementAt(index);
            } catch (ArrayIndexOutOfBoundsException e) {
                // logger.error(e.getMessage(), e);
                if (endFigures2.size() > 0) {
                    start = (AttributeFigure) endFigures2.elementAt(0);
                }
            }
            if (start != null) {
                AttributeFigure end;
                ArcConnection connection;

                if (this.getOwner() instanceof VJoinFigure) {
                    // get the correct transitions of this drawing peer
                    end = (AttributeFigure) getStartFigures().firstElement();
                    connection = new ArcConnection(1);

                    //NOTICEsignature
                    AttributeFigure start2 = (AttributeFigure) this.getOwner()
                                                                   .getSecondPrecedingPeer(this)
                                                                   .getEndFigures()
                                                                   .elementAt(0);
                    AttributeFigure end2 = (AttributeFigure) getStartFigures()
                                                                 .elementAt(1);
                    ArcConnection connection2 = new ArcConnection(1);

                    connection.startPoint(start2.center());
                    connection.endPoint(end.center());
                    connection.connectEnd(end.connectorAt(end.center()));
                    connection.connectStart(start2.connectorAt(start2.center()));
                    logger.debug("connection " + connection);
                    connection.updateConnection();
                    view.add(connection);


                    connection2.startPoint(start2.center());
                    connection2.endPoint(end2.center());
                    connection2.connectEnd(end2.connectorAt(end2.center()));
                    connection2.connectStart(start.connectorAt(start.center()));
                    logger.debug("connection " + connection2);
                    connection2.updateConnection();
                    view.add(connection2);

                } else {
                    // get the correct transitions of this drawing peer
                    end = (AttributeFigure) getStartFigures().firstElement();
                    connection = new ArcConnection(1);


                    connection.startPoint(start.center());
                    connection.endPoint(end.center());
                    connection.connectEnd(end.connectorAt(end.center()));
                    connection.connectStart(start.connectorAt(start.center()));
                    logger.debug("connection " + connection);
                    connection.updateConnection();
                    view.add(connection);
                }
            }
        }
    }

    protected void updateNextLocations() {
        Iterator<Figure> it = endFigures.iterator();
        while (it.hasNext()) {
            AttributeFigure fig = (AttributeFigure) it.next();
            Point nextLocation = new Point(fig.center().x + XOFFSET,
                                           fig.center().y);
            nextLocations.add(nextLocation);
        }
    }

    protected Vector<Figure> findStartConnectionTransitions(Vector<Figure> figures) {
        Iterator<Figure> it = figures.iterator();
        Vector<Figure> result = new Vector<Figure>();
        while (it.hasNext()) {
            Figure fig = it.next();
            if (fig instanceof TransitionFigure) {
                if (figureIsNamed(fig, ">")) {
                    result.add(fig);
                    logger.debug("################## Start connectable figures "
                                 + fig);
                }
            }
        }

        return result;
    }

    protected Vector<Figure> findEndConnectionPlaces(Vector<Figure> figures) {
        Iterator<Figure> it = figures.iterator();
        Vector<Figure> result = new Vector<Figure>();
        while (it.hasNext()) {
            Figure fig = it.next();
            if (fig instanceof PlaceFigure) {
                if (figureIsNamed(fig, ">")) {
                    result.add(fig);
                    logger.debug("################## Start connectable figures "
                                 + fig);
                }
            }
        }

        return result;
    }

    protected TextFigure getSelectedInscription(Vector<Figure> figures,
                                                String inscription) {
        Iterator<Figure> it = figures.iterator();
        TextFigure result = null;
        while (it.hasNext()) {
            Figure fig = it.next();
            if (fig instanceof TransitionFigure || fig instanceof PlaceFigure) {
                if (figureIsNamed(fig, inscription)) {
                    result = getNamedChild(fig, inscription);
                    logger.debug("################## Start connectable figures "
                                 + fig);
                }
            }
        }

        return result;
    }

    static private TextFigure getNamedChild(Figure fig, String str) {
        if (!(fig instanceof AttributeFigure)) {
            return null;
        }
        AttributeFigure attfig = (AttributeFigure) fig;
        Enumeration<Figure> enumeration = attfig.children();
        TextFigure result = null;
        while (enumeration.hasMoreElements()) {
            ChildFigure child = (ChildFigure) enumeration.nextElement();
            if (child instanceof TextFigure) {
                TextFigure textFigure = (TextFigure) child;
                if (textFigure.getText().equals(str)) {
                    result = textFigure;
                }
            }
        }
        return result;
    }

    static private boolean figureIsNamed(Figure fig, String str) {
        if (!(fig instanceof AttributeFigure)) {
            return false;
        }
        AttributeFigure attfig = (AttributeFigure) fig;
        Enumeration<Figure> enumeration = attfig.children();
        boolean result = false;
        while (enumeration.hasMoreElements()) {
            ChildFigure child = (ChildFigure) enumeration.nextElement();
            if (child instanceof TextFigure) {
                TextFigure textFigure = (TextFigure) child;
                if (textFigure.getText().equals(str)) {
                    result = true;
                }
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see de.renew.diagram.peer.IDrawingPeer#getNextLocation(CH.ifa.draw.framework.Connector)
     */
    @Override
    public Point getNextLocation(Connector con) {
        // return (Point)getNextLocations().lastElement();
        Point result = null;
        Vector<Connector> v = this.getOwner().getDParentConnectors();
        int index = 0;
        if (!v.isEmpty()) {
            logger.debug("con " + con);


            // check whether the owner is connected to the right side of a
            // VSplitFigure
            if (con instanceof RightConnector) {
                index++;
            }
        }
        result = getNextLocations().elementAt(index);
        logger.debug("index " + index);
        logger.debug("result " + result);
        return result;


        // AttributeFigure interfaceState = null;
        // int x = XOFFSET;
        // int y = 0;
        // Vector v = owner.getDParentConnectors();
        // int index = 0;
        // if (v.size() > 0){
        // Connector con = (Connector) v.firstElement();
        // //check whether the owner is connected to the right side of a
        // VSplitFigure
        // y = - YOFFSET;
        // if (con instanceof RightConnector){
        // index++;
        // y = YOFFSET;
        // }
        // }
        // interfaceState = (AttributeFigure) getEndFigures().elementAt(index);
        // x += interfaceState.center().x;
        // y += interfaceState.center().y ;
        //        
        //        
        // return new Point(x,y);
    }

    /* (non-Javadoc)
     * @see de.renew.diagram.peer.IDrawingPeer#toString()
     */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public TailFigure getOwner() {
        return owner;
    }
}