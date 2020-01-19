/*
 * @(#)RoleDescriptionFigure.java
 *
 */
package de.renew.diagram;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.FigureEnumerator;
import CH.ifa.draw.standard.MergedFigureEnumerator;

import de.renew.diagram.peer.DrawingPeer;

import de.renew.gui.CPNDrawing;
import de.renew.gui.GuiPlugin;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.Iterator;
import java.util.Vector;


public class RoleDescriptorFigure extends TailFigure {
    //private int transFigureSerializedDataVersion = 1;
    //private Rectangle fDisplayBox;

    /**
     * This figure will be highlighted in a instance
     * drawing in the same manner as the transition
     * will be highlighted. May be <code>null</code>.
     * @serial
     **/
    private Figure hilightFig = null;
    private Drawing connectedDrawing;
    private DrawingView connectedDrawingView;
    private Vector<Figure> tail;
    private String name;
    private static int number = 0;

    static String getDefaultName() {
        return "Role" + (++number);
    }

    public RoleDescriptorFigure() {
        super();
        tail = new Vector<Figure>();
        setName(getDefaultName());
    }

    public Rectangle displayBox() {
        return new Rectangle(fDisplayBox.x, fDisplayBox.y, fDisplayBox.width,
                             fDisplayBox.height);
    }

    protected void basicMoveBy(int x, int y) {
        fDisplayBox.translate(x, y);
    }

    /**
     * Changes the display box of a figure. Clients usually
     * call this method. It changes the display box
     * and announces the corresponding change.
     * @param origin the new origin
     * @param corner the new corner
     * @see #displayBox
     */
    public void displayBox(Point origin, Point corner) {
        willChange();


        //basicDisplayBox(origin,new Point ( (int)(defaultDimension().getWidth() ),corner.y - origin.y));
        basicDisplayBox(origin,
                        new Point(corner.x - origin.x, corner.y - origin.y));
        changed();
    }

    public void basicDisplayBox(Point origin, Point corner) {
        fDisplayBox = new Rectangle(origin.x, origin.y, corner.x, corner.y);
    }

    public Connector connectorAt(int x, int y) {
        return new VerticalConnector(this);
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = super.handles();
        handles.addElement(new LifeLineConnectionHandle(this));
        return handles;
    }

    public boolean getTraceMode() {
        Object value = getAttribute("TraceMode");
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        return true;
    }

    public void release() {
        super.release();

    }

    public static Dimension defaultDimension() {
        return new Dimension(72, 36);
    }

    public void setHighlightFigure(Figure fig) {
        hilightFig = fig;
    }

    public Figure getHighlightFigure() {
        return hilightFig;
    }

    /**
     * Returns all figures with dependencies of the superclass
     * plus an optional hilight figure.
     **/
    public FigureEnumeration getFiguresWithDependencies() {
        FigureEnumeration superDep = super.getFiguresWithDependencies();
        Vector<Figure> myDep = new Vector<Figure>(1);
        myDep.addElement(getHighlightFigure());
        return new MergedFigureEnumerator(superDep, new FigureEnumerator(myDep));
    }

    //    //-- store / load ----------------------------------------------
    //    public void write(StorableOutput dw) {
    //        super.write(dw);
    //        dw.writeInt(fDisplayBox.x);
    //        dw.writeInt(fDisplayBox.y);
    //        dw.writeInt(fDisplayBox.width);
    //        dw.writeInt(fDisplayBox.height);
    //    }
    //
    //    public void read(StorableInput dr) throws IOException {
    //        super.read(dr);
    //        fDisplayBox = new Rectangle(dr.readInt(), dr.readInt(), dr.readInt(), 
    //                                    dr.readInt());
    //    }

    /* (non-Javadoc)
     * @see de.renew.diagram.IDiagramElement#executeUpdate()
     */
    public void createNewDrawingAndGeneratePeers() {
        updateName();
        if (!getName().substring(0, 1).equals("!")) {
            createNewConnectedDrawing();
            if (getConnectedDrawing() != null) {
                startGeneratingPeers();
            } else {
                logger.error("Couldn't generate peers because no drawing exists!");
            }
        }
    }

    protected void createNewConnectedDrawing() {
        GuiPlugin starter = GuiPlugin.getCurrent();
        if (starter != null) {
            Drawing drawing = new CPNDrawing();
            updateName();
            drawing.setName(getName());
            drawing = starter.getGui().openDrawing(drawing);
            if (drawing != null) {
                setConnectedDrawing(drawing);
                setConnectedDrawingView(starter.getView(drawing));
            }
        }
    }

    /**
         *
         */
    private void updateName() {
        Iterator<Figure> it = children.iterator();
        while (it.hasNext()) {
            Figure fig = it.next();
            if (fig instanceof DiagramTextFigure) {
                setName(((DiagramTextFigure) fig).getText());
                return;
            }
        }
    }

    protected void startGeneratingPeers() {
        // fifo queue for putting all children in to generate peers
        // be careful not to put duplicates into the queue
        Vector<Figure> fifo = new Vector<Figure>();
        fifo.addAll(getDCildren());

        while (!fifo.isEmpty()) {
            Object o = fifo.remove(0);
            TailFigure child = (TailFigure) o;
            Vector<Figure> children = child.getDCildren();

            //ensure that all duplicates are removed
            children.removeAll(fifo);
            fifo.addAll(children);
            child.generatePeers();
        }


        //        Iterator it = tail();
        //        while (it.hasNext()) {
        //            TailFigure fig = (TailFigure) it.next();
        //            fig.generatePeers();
        //        }
    }

    public Drawing getConnectedDrawing() {
        return connectedDrawing;
    }

    public void setConnectedDrawing(Drawing drawing) {
        connectedDrawing = drawing;
    }

    public void addToTail(Figure fig) {
        tail.add(fig);
    }

    public void removeFromTail(Figure fig) {
        tail.remove(fig);
    }

    //    /* (non-Javadoc)
    //     * @see CH.ifa.draw.framework.Figure#moveBy(int, int)
    //     */
    //    public void moveBy(int dx, int dy) {
    //        
    //        super.moveBy(dx, dy);
    //        Iterator it = tail.iterator();
    //        
    //        while (it.hasNext()) {
    //            ((Figure) it.next()).moveBy(dx, dy);
    //        }
    //    }
    public Iterator<Figure> tail() {
        return tail.iterator();
    }

    // ------------------ TailFigure --------------------------------

    /* (non-Javadoc)
     * @see de.renew.diagram.TailFigure#getHead()
     */
    public RoleDescriptorFigure getDHead() {
        return this;
    }

    /* (non-Javadoc)
     * @see de.renew.diagram.TailFigure#setHead(de.renew.diagram.RoleDescriptorFigure)
     */
    public void setDHead(RoleDescriptorFigure rd) {
    }

    /* (non-Javadoc)
     * @see de.renew.diagram.TailFigure#setParent(de.renew.diagram.TailFigure)
     */
    public void addDParent(TailFigure parent) {
        // impl (or maybe not, couse ther is no parent to this element)
    }


    // -------------------

    /* (non-Javadoc)
     * @see de.renew.diagram.TailFigure#addPeer(de.renew.diagram.peer.DrawingPeer)
     */


    /*    public void addPeer(DrawingPeer peer) {
        }

        /* (non-Javadoc)
         * @see de.renew.diagram.TailFigure#getPeers()
         */
    public Vector<DrawingPeer> getPeers() {
        return null;
    }

    public DrawingView getConnectedDrawingView() {
        return connectedDrawingView;
    }

    public void setConnectedDrawingView(DrawingView view) {
        connectedDrawingView = view;
    }

    public String getName() {
        return name;
    }

    public void setName(String string) {
        name = string;
    }

    /**
     *
     */
    public void updatePeerNames() {
        // nothing to do
    }

    /**
     *
     */
    public void flushTail() {
        tail = new Vector<Figure>();
    }

    /**
     *
     */
    public void findAndNotifyDChildren() {
        Iterator<LifeLineConnection> it = connections.iterator();
        while (it.hasNext()) {
            LifeLineConnection connection = it.next();
            TailFigure dChild = connection.otherEnd(this);


            logger.debug("=========>RoleDescriptor found!");

            addDChild(dChild);
            dChild.addDParent(this);
            dChild.addDParentConnector(getConnectedConnector(connection));
            dChild.notifyDesendent(this, getDHead());

        }
    }

    /**
     * @see de.renew.diagram.TailFigure
     * @param parent
     * @param head
     */
    protected void notifyDesendent(TailFigure parent, RoleDescriptorFigure head) {
        setDHead(head);

        findAndNotifyDChildren();
        getDHead().addToTail(this);

    }
}