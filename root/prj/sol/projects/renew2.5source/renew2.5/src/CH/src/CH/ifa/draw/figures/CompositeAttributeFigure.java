package CH.ifa.draw.figures;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureChangeListener;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.FigureEnumerator;
import CH.ifa.draw.standard.MergedFigureEnumerator;
import CH.ifa.draw.standard.RelativeLocator;

import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;


/**
 * The CompositeAttributeFigure aggregates a collection of figures.
 * The set can be conveniently moved and selected (double-click) by mouse.
 * Most of its functionality has been extracted from the NetComponentFigure.
 *
 * @author Lawrence Cabac
 * @author David Mosteller
 *
 */
public class CompositeAttributeFigure extends AttributeFigure
        implements FigureChangeListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(CompositeAttributeFigure.class);
    protected Vector<Figure> attached = new Vector<Figure>();
    protected boolean removeWhenEmpty = true;


    /**
     * Create a new CompositeAttributeFigure.
     */
    public CompositeAttributeFigure() {
        setFillColor(ColorMap.NONE);
        setFrameColor(ColorMap.NONE);
    }

    /**
     * Determines whether the figure is contained in this figure collection.
     */
    @Override
    public boolean includes(Figure figure) {
        return getAttached().contains(figure);
    }

    /**
     * Draws a frame around this figure collection.
     */
    public void drawFrame(Graphics g) {
        Rectangle r = displayBox();
        Graphics2D g2 = (Graphics2D) g;
        Shape s = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
        g2.draw(s);
    }

    /**
     * Draw a background behind this figure collection.
     */
    public void drawBackground(Graphics g) {
        Rectangle r = displayBox();
        Graphics2D g2 = (Graphics2D) g;
        Shape s = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
        g2.fill(s);
    }

    /**
     * Move this figure collection along the x-axis by dx and along the y-axis by dy.
     */
    protected void basicMoveBy(int dx, int dy) {
        DrawingView view = getView();
        if (view == null) {
            return;
        }

        // do not trigger invalidation of the net component.
        // do not do this in one loop because of the transitive listener 
        // dependencies (moving a transition causes the connected arcs to 
        // change)
        for (Figure af : attached) {
            af.removeFigureChangeListener(this);
        }
        Vector<Figure> selected = view.selection();
        for (Figure af : attached) {
            if (!selected.contains(af)) {
                af.moveBy(dx, dy);
            }
        }
        for (Figure af : attached) {
            af.addFigureChangeListener(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see CH.ifa.draw.standard.AbstractFigure#basicDisplayBox(java.awt.Point,
     *      java.awt.Point)
     */
    public void basicDisplayBox(Point origin, Point corner) {
        // TODO Auto-generated method stub
    }

    /**
     * Gets the display box. The display box is defined as the union of the
     * contained figures.
     */
    public Rectangle displayBox() {
        Enumeration<Figure> k = attached.elements();
        Rectangle r = new Rectangle();
        if (k.hasMoreElements()) {
            r = (k.nextElement()).displayBox();
        }
        while (k.hasMoreElements()) {
            r.add((k.nextElement()).displayBox());
        }
        if (r == null) {
            return new Rectangle(0, 0);
        }
        return r;
    }

    /**
     * Gets the handles for the CompositeAttributeFigure.
     * @see CH.ifa.draw.standard.AbstractFigure#handles()
     */
    public Vector<Handle> handles() {
        Vector<Handle> handles = new Vector<Handle>();
        handles.addElement(new GroupHandle(this, RelativeLocator.northWest()));
        handles.addElement(new GroupHandle(this, RelativeLocator.northEast()));
        handles.addElement(new GroupHandle(this, RelativeLocator.southWest()));
        handles.addElement(new GroupHandle(this, RelativeLocator.southEast()));
        return handles;
    }

    /**
     * Returns whether this figure can be selected.
     */
    public boolean isSelectable() {
        return true;
    }

    /**
     * Get the DrawingView of the currently active gui, if present.
     * @return
     */
    protected DrawingView getView() {
        DrawApplication gui = DrawPlugin.getGui();
        if (gui == null) {
            return null;
        }
        return gui.getView(gui.drawing());

    }

    /**
     * Stores the connector and its owner to a StorableOutput.
     */
    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeInt(attached.size());
        Iterator<Figure> it = attached.iterator();
        while (it.hasNext()) {
            Figure figure = it.next();
            dw.writeStorable(figure);

        }
    }

    /**
     * Reads the connector and its owner from a StorableInput.
     */
    public void read(StorableInput dr) throws IOException {
        if (dr.getVersion() > 9) {
            super.read(dr);
        }
        attached = new Vector<Figure>();
        int size = dr.readInt();
        if (size != 0) {
            for (int i = 1; i <= size; i++) {
                Figure figure = (Figure) dr.readStorable();
                attached.add(figure);
                figure.addFigureChangeListener(this);
            }
        }


        // manage unset colors -- default for NetComponentFigure is ColoMap.NONE
        Enumeration<String> attributeKeys = getAttributeKeys();
        boolean isFillColorSet = false;
        boolean isFrameColorSet = false;
        while (attributeKeys.hasMoreElements()) {
            String string = (String) attributeKeys.nextElement();
            if (string.equals("FillColor")) {
                isFillColorSet = true;
            }
            if (string.equals("FrameColor")) {
                isFrameColorSet = true;
            }
        }
        if (!isFillColorSet) {
            setFillColor(ColorMap.NONE);
        }
        if (!isFrameColorSet) {
            setFrameColor(ColorMap.NONE);
            setAttribute("LineStyle", LineConnection.LINE_STYLE_DOTTED);
        }
    }

    /**
     * @return a <code>Vector</code> of Figures that are attached to this
     *         <code>NetComponentFigure</code>.
     */
    public Vector<Figure> getAttached() {
        return attached;
    }

    /**
     * Inspect this CompositeAttributeFigure (usually by right-click).
     */
    public boolean inspect(DrawingView view, boolean alternate) {
        if (alternate) {
            return super.inspect(view, alternate);
        } else {
            view.clearSelection();
            view.addToSelectionAll(attached);
            return true;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see CH.ifa.draw.framework.FigureChangeListener#figureInvalidated(CH.ifa.draw.framework.FigureChangeEvent)
     */
    public void figureInvalidated(FigureChangeEvent e) {
        invalidate();
    }

    /*
     * (non-Javadoc)
     *
     * @see CH.ifa.draw.framework.FigureChangeListener#figureChanged(CH.ifa.draw.framework.FigureChangeEvent)
     */
    public void figureChanged(FigureChangeEvent e) {
        changed();
    }

    /*
     * (non-Javadoc)
     *
     * @see CH.ifa.draw.framework.FigureChangeListener#figureRemoved(CH.ifa.draw.framework.FigureChangeEvent)
     */
    public void figureRemoved(FigureChangeEvent e) {
        willChange();
        Figure removed = e.getFigure();
        attached.remove(removed);
        if (removeWhenEmpty && attached.isEmpty() && listener() != null) {
            listener().figureRequestRemove(new FigureChangeEvent(this));
        }
        changed();
    }

    /*
     * (non-Javadoc)
     *
     * @see CH.ifa.draw.framework.FigureChangeListener#figureRequestRemove(CH.ifa.draw.framework.FigureChangeEvent)
     */
    public void figureRequestRemove(FigureChangeEvent e) {
        willChange();
        attached.remove(e.getSource());
        changed();
    }

    /*
     * (non-Javadoc)
     *
     * @see CH.ifa.draw.framework.FigureChangeListener#figureRequestUpdate(CH.ifa.draw.framework.FigureChangeEvent)
     */
    public void figureRequestUpdate(FigureChangeEvent e) {
        System.out.println("NCF: figureRequestUpdate");
        // nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see CH.ifa.draw.framework.FigureChangeListener#figureHandlesChanged(CH.ifa.draw.framework.FigureChangeEvent)
     */
    public void figureHandlesChanged(FigureChangeEvent e) {
        // nothing to do
    }

    /**
     * Deserialization method, behaves like default readObject
     * method, but additionally restores the association from
     * contained figures to this composite figure.
     **/
    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException {
        s.defaultReadObject();

        Enumeration<Figure> k = attached.elements();
        while (k.hasMoreElements()) {
//            // remove dependency to gui, breaks cloning if GUI is not present
//            // should also work directly. What was the reason for this indirection?
//            FigureEnumeration en = DrawPlugin.getGui().drawing().figures();
            Figure figure = k.nextElement();
//            while (en.hasMoreElements()) {
//                Figure f = en.nextElement();
//                if (f.equals(figure)) {
            figure.addToContainer(this);
//                }
//            }
        }
    }

    /**
     * Get a FigureEnumeration containing all figures that are associated with this collection.
     */
    public FigureEnumeration getFiguresWithDependencies() {
        return new MergedFigureEnumerator(super.getFiguresWithDependencies(),
                                          getAttachedFigures());
    }


    /**
     * Get a FigureEnumeration containing all figures of this collection.
     * @return
     */
    private FigureEnumeration getAttachedFigures() {
        return new FigureEnumerator(attached);
    }


    /**
     * Invoke an update of the listeners of all associated figures.
     */
    public void updateListeners() {
        FigureEnumeration fenum = getFiguresWithDependencies();
        while (fenum.hasMoreElements()) {
            Figure figure = fenum.nextFigure();
            figure.addToContainer(this);
        }
    }
}