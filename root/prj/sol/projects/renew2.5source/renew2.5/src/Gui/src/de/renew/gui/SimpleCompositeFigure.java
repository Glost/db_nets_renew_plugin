package de.renew.gui;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.CompositeFigure;

import CH.ifa.draw.util.StorableOutput;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.Enumeration;
import java.util.Vector;


abstract public class SimpleCompositeFigure extends CompositeFigure {

    /**
     * Determines (reflects?) position and size of the
     * composite figure.
     * @serial
     **/
    protected Rectangle fDisplayBox = new Rectangle(0, 0, 0, 0);

    /**
     * Cache for handles of all included figures.
     * Initially <code>null</code>.
     * <p>
     * This field is transient, because its contents will
     * automatically be recalculated by {@link #handles()}.
     * </p>
     **/
    private transient Vector<Handle> fHandles = null;

    /**
     * Additional ClickHandles.
     * @serial
     **/
    protected Vector<ClickHandle> fClickHandles = new Vector<ClickHandle>();

    public void addClickHandle(ClickHandle handle) {
        fClickHandles.addElement(handle);
        fHandles = null;
        handlesChanged();
    }

    /*
        public void draw(Graphics g) {
           g.setColor(Color.red);
           g.drawRect(fDisplayBox.x,fDisplayBox.y,fDisplayBox.width,fDisplayBox.height);
           super.draw(g);
        }
    */
    protected void basicMoveBy(int x, int y) {
        fDisplayBox.translate(x, y);
        super.basicMoveBy(x, y);
    }

    public void moveBy(int x, int y) {
        willChange();
        basicMoveBy(x, y);
        changed();
    }

    public Rectangle displayBox() {
        return new Rectangle(fDisplayBox.x, fDisplayBox.y, fDisplayBox.width,
                             fDisplayBox.height);
    }

    public void basicDisplayBox(Point origin, Point corner) {
        fDisplayBox = new Rectangle(origin);
        fDisplayBox.add(corner);
        layout();
    }

    protected Vector<Handle> basicHandles() {
        return new Vector<Handle>();
    }

    public Vector<Handle> handles() {
        if (fHandles == null) {
            fHandles = basicHandles();
            FigureEnumeration k = figures();
            while (k.hasMoreElements()) {
                Figure figure = k.nextFigure();
                Enumeration<Handle> kk = figure.handles().elements();
                while (kk.hasMoreElements()) {
                    Handle handle = kk.nextElement();
                    if (handle instanceof TokenHandle) {
                        fHandles.addElement(handle);
                    }
                }
            }
            Enumeration<ClickHandle> clickHandles = fClickHandles.elements();
            while (clickHandles.hasMoreElements()) {
                fHandles.addElement(clickHandles.nextElement());
            }
        }
        return fHandles;
    }

    protected void figureSetChanged() {
        super.figureSetChanged();
        fHandles = null;
        handlesChanged();
        // logger.debug("Resetting handles of "+this.getClass());
    }

    protected void layout() {
        // update ClickHandles:
        for (int i = 0; i < fClickHandles.size(); ++i) {
            ClickHandle handle = fClickHandles.elementAt(i);
            Dimension size = handle.owner().size();
            handle.setBox(new Rectangle(0, 0, size.width, size.height));
        }
    }

    abstract protected boolean needsLayout();

    /**
     *
     * @param e [{@link FigureChangeEvent}] UNUSED
     *
     * @author Eva Mueller
     * @date Dec 3, 2010
     * @version 0.1
     */
    public void update(FigureChangeEvent e) {
        if (needsLayout()) {
            willChange();
            layout();
            changed();
        }
    }

    public void figureChanged(FigureChangeEvent e) {
        update(e);
    }

    public void figureHandlesChanged(FigureChangeEvent e) {
        fHandles = null;
        handlesChanged();
    }

    public void figureRemoved(FigureChangeEvent e) {
        update(e);
    }

    public boolean inspect(DrawingView view, boolean alternate) {
        Point click = view.lastClick();
        Figure inside = findFigureInside(click.x, click.y);
        if (inside != null && inside.inspect(view, alternate)) {
            return true;
        }
        if (alternate) {
            return false; // don't use default alternate behaviour
        } else {
            return super.inspect(view, false); // do use default normal behaviour
        }
    }

    public void setAttribute(String name, Object value) {
        FigureEnumeration figenumeration = figures();
        while (figenumeration.hasMoreElements()) {
            (figenumeration.nextElement()).setAttribute(name, value);
        }
    }

    public Object getAttribute(String name) {
        FigureEnumeration figenumeration = figures();
        if (figenumeration.hasMoreElements()) {
            return (figenumeration.nextElement()).getAttribute(name);
        }
        return null;
    }

    /**
     * Although inheriting the {@link CH.ifa.draw.util.Storable} interface from its
     * superclass, this class is <b>not</b> storable.
     * (Just because no one made the effort - and it currently
     * doesn't need to be storable. But if you want to implement
     * storability, please take a look at all its subclasses, too.)
     * @throws RuntimeException always.
     */
    public void write(StorableOutput dw) {
        throw new RuntimeException("SimpleCompositeFigure is not storable!");
    }
}