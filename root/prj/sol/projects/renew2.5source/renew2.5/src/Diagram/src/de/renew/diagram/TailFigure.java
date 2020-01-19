/*
 * Created on Apr 29, 2003
 *
 */
package de.renew.diagram;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;

import de.renew.diagram.drawing.DiagramDrawing;
import de.renew.diagram.peer.DrawingPeer;
import de.renew.diagram.peer.IDrawingPeer;

import java.awt.Point;

import java.util.Iterator;
import java.util.Vector;


/**
 * @author Lawrence Cabac
 *
 * A TailFigure Element of a DiagramDrawing is an element that can be added to a
 * tail; i.e. belongs to and is known by a RoleDescriptorFigure. It knows its
 * head and its parent (the element it is connected to that is closer to the
 * head).
 */
public class TailFigure extends DiagramFigure {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(TailFigure.class);
    protected Vector<LifeLineConnection> connections;
    static final Point START_LOCATION = new Point(50, 500);
    private RoleDescriptorFigure head;
    protected Vector<DrawingPeer> drawingPeers; ///%%%%%
    private Vector<String> peerNames;
    private Vector<Figure> dParents;
    private Vector<Connector> dParentConnectors;
    private Vector<Figure> dChildren;

    public TailFigure() {
        super();
        dParents = new Vector<Figure>();
        dChildren = new Vector<Figure>();
        peerNames = new Vector<String>();
        drawingPeers = new Vector<DrawingPeer>();
        dParentConnectors = new Vector<Connector>();
        connections = new Vector<LifeLineConnection>();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.diagram.TailFigure#getHead()
     */
    public RoleDescriptorFigure getDHead() {
        if (head == null) {
            Vector<Figure> v = getDParents();
            if (v.size() > 0) {
            } else {
            }

            DrawApplication gui = DrawPlugin.getGui();
            if (gui != null) {
                Locator loc = getLocator(gui.view());
                Figure fig = loc.getElementAtPosition(center().x, displayBox().y);
                if (fig instanceof TailFigure) {
                    TailFigure tail = (TailFigure) fig;
                    setDHead(tail.getDHead());
                }
            }
        }
        return head;
    }

    public void setDHead(RoleDescriptorFigure rd) {
        head = rd;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.diagram.TailFigure#setParent(de.renew.diagram.TailFigure)
     */
    public void addDParent(TailFigure parent) {
        dParents.add(parent);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.diagram.TailFigure#setParent(de.renew.diagram.TailFigure)
     */
    public boolean hasDParent() {
        return dParents != null && dParents.size() > 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.diagram.TailFigure#getParent()
     */
    public Vector<Figure> getDParents() {
        return dParents;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.diagram.TailFigure#setParent(de.renew.diagram.TailFigure)
     */
    public void addDParentConnector(Connector connector) {
        dParentConnectors.add(connector);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.diagram.TailFigure#getParent()
     */
    public Vector<Connector> getDParentConnectors() {
        return dParentConnectors;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.diagram.TailFigure#addChild(de.renew.diagram.TailFigure)
     */
    public void addDChild(TailFigure child) {
        dChildren.add(child);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.diagram.TailFigure#getCildren()
     */
    public Vector<Figure> getDCildren() {
        return dChildren;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.diagram.TailFigure#addPeer(de.renew.diagram.peer.DrawingPeer)
     */
    public void addPeer(DrawingPeer peer) {
        drawingPeers.add(peer);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.diagram.TailFigure#getPeers()
     */
    public Vector<DrawingPeer> getPeers() {
        logger.debug("drawingPeers are " + drawingPeers);

        return drawingPeers;
    }

    public void generatePeers() {
        updatePeers();


        // for all peers of one figure
        Iterator<DrawingPeer> it = getPeers().iterator();
        while (it.hasNext()) {
            IDrawingPeer peer = it.next();
            logger.debug("2222" + getDHead().getConnectedDrawing());
            peer.drawFigures(getDHead().getConnectedDrawingView());
        }
        it = getPeers().iterator();
        while (it.hasNext()) {
            IDrawingPeer peer = it.next();
            peer.connectFigures(getDHead().getConnectedDrawingView());
        }
    }

    // from the peer name list all peers are being produced.
    // some elements e.g. TaskFigures can hold more than one peer
    public void updatePeers() {
        drawingPeers = new Vector<DrawingPeer>();
        Iterator<String> it = peerNames.iterator();
        while (it.hasNext()) {
            String name = it.next();
            if (name.equals("start")) {
                addPeer(new DrawingPeer(START_LOCATION, name + ".rnw", this,
                                        this));
            } else {
                addPeer(name + ".rnw");
            }
        }
    }

    public Point getNextLocation() {
        Point result = null;
        DiagramFigure parent = (DiagramFigure) getDParents().firstElement();
        if (parent != null) {
            result = lastPeer().getNextLocations().firstElement();
            // fix this to be able to access other Elements
        }
        return result;
    }

    //    public Point getNextLocation() {
    //        Point result = null;
    //        AttributeFigure fig = null;
    //        DiagramFigure parent = (DiagramFigure) getDParents().firstElement();
    //        if (parent != null && parent instanceof VSplitFigure){
    //            Connector connector = (Connector) dParentConnectors.firstElement();
    //            Vector endFigures =
    // ((DrawingPeer)((TailFigure)parent).getPeers().firstElement()).getEndFigures();
    //            
    //            if (connector instanceof LeftBottomConnector){
    //                fig =(AttributeFigure)endFigures.firstElement();
    //           }
    //            else {
    //                fig =(AttributeFigure)endFigures.lastElement();
    //            }
    // 
    //        }else{
    //            Vector endFigures =
    // ((DrawingPeer)((TailFigure)parent).getPeers().firstElement()).getEndFigures();
    //            fig =(AttributeFigure)endFigures.firstElement();
    //        }
    //        result.x = fig.center().x + 100;
    //        result.y = fig.center().y;
    //
    //        //result = (Point) lastPeer().getNextLocations().firstElement(); // fix
    // this to be able to access other Elements
    //        return result;
    //    }


    /**
     * @return The last Peer that is
     */
    public IDrawingPeer lastPeer() {
        Vector<DrawingPeer> v = getPeers();
        logger.debug("2nd drawingPeers are " + v + "  " + this);

        if (v != null) {
            if (v.isEmpty()) {
                //     logger.debug("peerlist empty!");
                return ((TailFigure) getDParents().firstElement()).lastPeer();
            }
            logger.info(getPeers().lastElement());
            logger.info(getPeers().firstElement());
            return getPeers().lastElement();
        }
        return null;
    }

    public IDrawingPeer getPrecedingPeer(IDrawingPeer peer) {
        int index = drawingPeers.indexOf(peer);
        if (index == 0) {
            // first peer just return the last of the peceding tailable Figure
            return ((TailFigure) getDParents().firstElement()).lastPeer();
        }
        return drawingPeers.elementAt(index - 1);
    }

    // FIXME fix this later
    //NOTICEsignature
    public IDrawingPeer getSecondPrecedingPeer(IDrawingPeer peer) {
        TailFigure parent = ((TailFigure) getDParents().elementAt(1));
        IDrawingPeer dp = parent.lastPeer();
        return dp;

    }

    public void removePeers() {
        drawingPeers = new Vector<DrawingPeer>();

    }

    public void addPeerName(String name) {
        peerNames.add(name);
    }

    public void addPeer(String fileName) {
        Point loc = calculateLocation(); // has to be changed soon
        logger.debug("------------------------------------------------- ");
        logger.debug(" ");

        addPeer(new DrawingPeer(loc, fileName, this, this));
        logger.debug("(((((((((((((((((((((((((((((((((((((((((((((( ");
        logger.debug("TailableFigure " + this);
        logger.debug("Parent " + getDParents().firstElement());
        logger.debug("Head " + getDHead());
        //        logger.debug("Parent Connector " +
        // getDParentConnectors().firstElement());
        logger.debug(")))))))))))))))))))))))))))))))))))))))))))))) ");

    }

    /**
     *
     */
    public void flushPeers() {
        peerNames = new Vector<String>();
        drawingPeers = new Vector<DrawingPeer>();
    }

    /**
     *
     */
    public void flushRelatives() {
        dParents = new Vector<Figure>();
        dParentConnectors = new Vector<Connector>();
        dChildren = new Vector<Figure>();
    }

    protected Point calculateLocation() {
        if (this instanceof RoleDescriptorFigure) {
            return START_LOCATION;
        }
        Point p = null;
        try {
            Vector<Connector> dParentConnectors = getDParentConnectors();
            Vector<Figure> dPar = getDParents();
            logger.debug("this " + this);
            //        logger.debug("parentsConnectors "+v);
            logger.debug("parents " + dPar);
            logger.debug("parentsConnectors " + dParentConnectors);

            if (this instanceof VJoinFigure) {
                IDrawingPeer pd1 = ((TailFigure) dPar.firstElement()).lastPeer();
                IDrawingPeer pd2 = ((TailFigure) dPar.elementAt(1)).lastPeer();
                Point p1 = pd1.getNextLocations().firstElement();
                Point p2 = pd2.getNextLocations().firstElement();
                p = new Point(Math.max(p1.x, p2.x), (p1.y + p2.y) / 2);

            } else {
                Connector con = dParentConnectors.firstElement();
                int index = 0;
                if (con instanceof RightConnector) {
                    index++;
                }
                logger.debug("TailFigure::calculateLocation line 344: this = "
                             + this);
                IDrawingPeer lastPeer = lastPeer();

                if (lastPeer != null) {
                    Vector<Point> nextLocations = lastPeer.getNextLocations();
                    try {
                        p = nextLocations.elementAt(index); //TODO: fix
                                                            // this index


                    } catch (ArrayIndexOutOfBoundsException e) {
                        // logger.error(e.getMessage(), e);
                        p = nextLocations.elementAt(0);
                    }
                }

                //        if (this instanceof TaskFigure && dParents.size() > 0){
                //            // if TaskFigure then there is only one dParent
                //            DiagramFigure parent = (DiagramFigure)
                // dParents.firstElement();
                //            if (drawingPeers.size()==0 && parent instanceof
                // VSplitFigure){
                //                
                //            }
                //        }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error(e.getMessage(), e);
        }
        return p;
    }

    /**
     *
     */
    public void newPeerNames() {
        peerNames = new Vector<String>();
    }

    /**
     *
     */


    // Implementing classes override this method
    public void updatePeerNames() {
        //
    }

    /**
     * @param connection
     */
    public void addConnection(LifeLineConnection connection) {
        connections.add(connection);
    }

    /**
     * @param connection
     */
    protected Connector getConnectedConnector(LifeLineConnection connection) {
        Figure fig = connection.startFigure();
        Connector connector;
        if (!fig.equals(this)) {
            fig = connection.endFigure();
            connector = connection.end();
        } else {
            connector = connection.start();
        }
        return connector;
    }

    /**
     *
     */
    public void flushConnections() {
        connections = new Vector<LifeLineConnection>();
    }

    /**
     * A TailFigure should update the stucture of the peer components. and
     * notify its desendents to do likewise.
     *
     * @param parent
     * @param head
     */
    protected void notifyDesendent(TailFigure parent, RoleDescriptorFigure head) {
    }

    /**
     *
     */
    public void findAndNotifyDChildren() {
    }

    private Locator getLocator(DrawingView view) {
        Drawing drawing = view.drawing();
        if (!(drawing instanceof DiagramDrawing)) {
            return null;
        }

        DiagramDrawing ddrawing = (DiagramDrawing) drawing;
        return ddrawing.getLocator();

    }
}