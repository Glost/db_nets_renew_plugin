/*
 * @(#)TaskFigure.java
 *
 */
package de.renew.diagram;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.FigureEnumerator;
import CH.ifa.draw.standard.MergedFigureEnumerator;
import CH.ifa.draw.standard.RelativeLocator;

import de.renew.dcdiagram.DCAnswerMessageConnection;
import de.renew.dcdiagram.DCDrawingPeer;
import de.renew.dcdiagram.DCTaskFigure;

import de.renew.diagram.peer.DrawingPeer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;


public class TaskFigure extends TailFigure implements java.util.Comparator<Figure> {
    private Vector<Figure> messages;

    /**
     * This figure will be highlighted in a instance drawing in the same manner
     * as the transition will be highlighted. May be <code>null</code>.
     *
     * @serial
     */
    private Figure hilightFig = null;
    private Vector<IDiagramElement> originators;

    public TaskFigure() {
        super();
        messages = new Vector<Figure>();
        originators = new Vector<IDiagramElement>();
    }

    public Rectangle displayBox() {
        return new Rectangle(fDisplayBox.x, fDisplayBox.y, fDisplayBox.width,
                             fDisplayBox.height);
    }

    protected void basicMoveBy(int x, int y) {
        fDisplayBox.translate(x, y);
    }

    /**
     * Changes the display box of a figure. Clients usually call this method. It
     * changes the display box and announces the corresponding change.
     *
     * @param origin the new origin
     * @param corner the new corner
     * @see #displayBox
     */
    public void displayBox(Point origin, Point corner) {
        willChange();
        basicDisplayBox(origin,
                        new Point((int) (defaultDimension().getWidth()),
                                  corner.y - origin.y));
        changed();
    }

    public void basicDisplayBox(Point origin, Point corner) {
        fDisplayBox = new Rectangle(origin.x, origin.y, corner.x, corner.y);
    }

    public Connector connectorAt(int x, int y) {
        Rectangle r = displayBox();
        int top = r.y;
        int bottom = r.y + r.height;
        int length = bottom - top;

        int fifth = (length / 5);
        if (y < top + fifth) {
            return new TopConnector(this);
        } else if (y > bottom - fifth) {
            return new BottomConnector(this);
        }

        return new HorizontalConnector(this);
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = super.handles();
        handles.addElement(new MessageConnectionHandle(this));
        handles.addElement(new LifeLineConnectionHandle(this,
                                                        new RelativeLocator(0.5,
                                                                            0.85)));
        return handles;
    }

    public void release() {
        super.release();

    }

    public void setHighlightFigure(Figure fig) {
        hilightFig = fig;
    }

    public Figure getHighlightFigure() {
        return hilightFig;
    }

    /**
     * Returns all figures with dependencies of the superclass plus an optional
     * hilight figure.
     */
    public FigureEnumeration getFiguresWithDependencies() {
        FigureEnumeration superDep = super.getFiguresWithDependencies();
        Vector<Figure> myDep = new Vector<Figure>(1);
        myDep.addElement(getHighlightFigure());
        return new MergedFigureEnumerator(superDep, new FigureEnumerator(myDep));
    }

    /**
     *
     */
    public void updatePeerNames() {
        if (getDParents().firstElement() instanceof RoleDescriptorFigure) {
            addPeerName("start");
            addOriginatorClass(getDHead());
        }
    }

    /**
     * here the mapping of Figures and peer names takes place.
     * Example: an ActionTextFigure is represented by its peer sequence.
     */
    public void handleMessages() {
        updatePeerNames();


        // also add actions...
        Iterator<Figure> iter = children.iterator();
        while (iter.hasNext()) {
            Figure fig = iter.next();
            if (fig instanceof DiagramTextFigure) {
                messages.add(fig);
            }
        }
        sortByYPosition(messages, this);
        Iterator<Figure> it = messages.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof ActionTextFigure) {
                logger.debug("start (Place) " + this);
                addPeerName("sequence-template");
                addOriginatorClass((IDiagramElement) o);
            } else if (o instanceof DCServiceTextFigure) {
                logger.debug("Exchange: start (Place) " + this);
                addPeerName("exchange");
                addOriginatorClass((IDiagramElement) o);
            } else if (o instanceof MessageConnection) {
                MessageConnection message = (MessageConnection) o;
                DiagramFigure start = (DiagramFigure) message.startFigure();
                DiagramFigure end = (DiagramFigure) message.endFigure();

                if (start.equals(this)) {
                    logger.debug("start (Place) " + this);
                    addPeerName("out");
                    addOriginatorClass((IDiagramElement) o);
                }

                if (end.equals(this)) {
                    logger.debug("end (Place) " + this);
                    addPeerName("in");
                    addOriginatorClass((IDiagramElement) o);
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.warn(TaskFigure.class.getName() + ": "
                                + "Some (text) figure is "
                                + "attached that is not an Action, DC exchange nor a Message.\n"
                                + "This could be a mistake.");
                    if (logger.isDebugEnabled()) {
                        logger.debug("   class is of type: "
                                     + o.getClass().getName());
                        if (o instanceof TextFigure) {
                            TextFigure textfigure = (TextFigure) o;
                            logger.debug("   text is         : "
                                         + textfigure.getText());
                        }
                    }
                }
            }
        }
    }

    public void addOriginatorClass(IDiagramElement o) {
        originators.add(o);
    }

    /**
     * @param messages
     */
    static public void sortByYPosition(Vector<Figure> messages,
                                       java.util.Comparator<Figure> comp) {
        Collections.sort(messages, comp);


        // while (!messages.isEmpty()) {
        //            
        // MessageConnection mess = (MessageConnection) messages.remove(0);//
        // firstElement();
        // while (it.hasNext()) {
        // Iterator it2 = messages.iterator();
        // while (it.hasNext()) {
        //
        // }
        //
        // }
        // }
    }

    /**
     * @param connection
     */
    public void addMessage(MessageConnection connection) {
        messages.add(connection);
    }

    // -------------------- TailFigure
    // //-- store / load ----------------------------------------------
    // public void write(StorableOutput dw) {
    // super.write(dw);
    // dw.writeInt(fDisplayBox.x);
    // dw.writeInt(fDisplayBox.y);
    // dw.writeInt(fDisplayBox.width);
    // dw.writeInt(fDisplayBox.height);
    // }
    //
    // public void read(StorableInput dr) throws IOException {
    // super.read(dr);
    // fDisplayBox = new Rectangle(dr.readInt(), dr.readInt(), dr.readInt(),
    // dr.readInt());
    // }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Figure o1, Figure o2) {
        int y1 = 0;
        int y2 = 0;
        if (o1 instanceof MessageConnection) {
            MessageConnection m1 = (MessageConnection) o1;
            y1 = m1.getConnectedPoint(this).y;
        }
        if (o2 instanceof MessageConnection) {
            MessageConnection m2 = (MessageConnection) o2;
            y2 = m2.getConnectedPoint(this).y;
        }
        if (o1 instanceof DiagramTextFigure) {
            DiagramTextFigure m1 = (DiagramTextFigure) o1;
            y1 = m1.center().y;
        }
        if (o2 instanceof DiagramTextFigure) {
            DiagramTextFigure m2 = (DiagramTextFigure) o2;
            y2 = m2.center().y;
        }

        return y1 - y2;
    }

    /**
     *
     */
    public void flushMessages() {
        messages = new Vector<Figure>();
    }

    /**
     *
     */
    public void findAndNotifyDChildren() {
        Iterator<LifeLineConnection> it = connections.iterator();
        while (it.hasNext()) {
            LifeLineConnection connection = it.next();
            TailFigure dChild = connection.otherEnd(this);


            logger.debug("=========>TaskFigure found!");
            Connector connector = getConnectedConnector(connection);

            if (connector instanceof BottomConnector) {
                if (!dChild.equals(this)) {
                    addDChild(dChild);
                    dChild.addDParent(this);
                    dChild.addDParentConnector(connector);
                    dChild.notifyDesendent(this, getDHead());
                }
            }
        }
    }

    /**
     * @see de.renew.diagram.TailFigure
     * @param parent
     * @param head
     */
    protected void notifyDesendent(TailFigure parent, RoleDescriptorFigure head) {
        setDHead(head);


        getDHead().addToTail(this);
        (this).handleMessages();
        findAndNotifyDChildren();

    }

    public static Dimension defaultDimension() {
        return new Dimension(20, 20);
    }

    /* (non-Javadoc)
      * @see de.renew.diagram.TailFigure#flushPeers()
      */
    @Override
    public void flushPeers() {
        originators = new Vector<IDiagramElement>();
        super.flushPeers();
    }

    // @Override
    public void addPeer(String fileName) {
        Point loc = calculateLocation();
        addPeer(new DrawingPeer(loc, fileName, this, this));
        if (logger.isDebugEnabled()) {
            logger.debug("------------------------------------------------- ");
            logger.debug(" ");
            logger.debug("(((((((((((((((((((((((((((((((((((((((((((((( ");
            logger.debug("TailableFigure " + this);
            logger.debug("Parent " + getDParents().firstElement());
            logger.debug("Head " + getDHead());
            logger.debug(")))))))))))))))))))))))))))))))))))))))))))))) ");
        }
    }

    //@Override
    public void updatePeers() {
        drawingPeers = new Vector<DrawingPeer>();
        Iterator<IDiagramElement> it = originators.iterator();
        while (it.hasNext()) {
            IDiagramElement figure = it.next();

            //String name = (String) it.next();
            if (figure instanceof RoleDescriptorFigure) {
                addPeer(new DrawingPeer(START_LOCATION, "start.rnw", this, this));
            } else {
                if (figure instanceof ActionTextFigure) {
                    Point loc = calculateLocation();
                    if (((ActionTextFigure) figure).getText().equals(":stop()")
                                || ((ActionTextFigure) figure).getText()
                                            .equals("stop")) {
                        addPeer(new DrawingPeer(loc, "stop.rnw", this, figure));
                    } else if (((ActionTextFigure) figure).getText()
                                        .equals("manual")
                                       || ((ActionTextFigure) figure).getText()
                                                   .equals("manual;")) {
                        addPeer(new DrawingPeer(loc, "manual.rnw", this, figure));
                    } else {
                        addPeer(new DrawingPeer(loc, "sequence-template.rnw",
                                                this, figure));
                    }
                } else if (figure instanceof DCServiceTextFigure) {
                    DCServiceTextFigure textFigure = (DCServiceTextFigure) figure;
                    String rnwFile = "exchange.rnw";
                    if (textFigure.getText().startsWith("simple")) {
                        rnwFile = "simple-exchange.rnw";
                    }
                    Point loc = calculateLocation();
                    addPeer(new DrawingPeer(loc, rnwFile, this, figure));
                } else {
                    MessageConnection message = (MessageConnection) figure;
                    DiagramFigure start = (DiagramFigure) message.startFigure();
                    DiagramFigure end = (DiagramFigure) message.endFigure();

                    if (message instanceof DCAnswerMessageConnection) {
                        if (end instanceof DCTaskFigure) {
                            Point loc = calculateLocation();
                            addPeer(new DCDrawingPeer(loc,
                                                      "exchange-with-id.rnw",
                                                      this, figure));
                        } else if (start.equals(this)) {
                            Point loc = calculateLocation();
                            addPeer(new DrawingPeer(loc, "out.rnw", this, figure));
                        } else if (!(start instanceof DCTaskFigure)) {
                            Point loc = calculateLocation();
                            addPeer(new DrawingPeer(loc, "in.rnw", this, figure));
                        }
                    } else if (end instanceof DCTaskFigure) {
                        String text = message.getMessageText();
                        Point loc = calculateLocation();

                        if (text != null) {
                            if (text.startsWith("simple")) {
                                addPeer(new DCDrawingPeer(loc,
                                                          "simple-exchange.rnw",
                                                          this, figure));
                            } else {
                                addPeer(new DCDrawingPeer(loc, "exchange.rnw",
                                                          this, figure));
                            }
                        } else {
                            addPeer(new DCDrawingPeer(loc, "exchange.rnw",
                                                      this, figure));
                        }
                    } else if (start instanceof DCTaskFigure) {
                        String text = message.getMessageText();
                        Point loc = calculateLocation();
                        addPeer(new DCDrawingPeer(loc, "exchange.rnw", this,
                                                  figure));
                    } else {
                        if (start.equals(this)) {
                            Point loc = calculateLocation();
                            addPeer(new DrawingPeer(loc, "out.rnw", this, figure));
                        }

                        if (end.equals(this)) {
                            Point loc = calculateLocation();
                            addPeer(new DrawingPeer(loc, "in.rnw", this, figure));
                        }
                    }
                }
            }
        }
        logger.debug("Peers = " + getPeers());
    }
}