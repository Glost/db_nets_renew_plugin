/*
 * @(#)AbstractFigure.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.DrawingContext;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureChangeListener;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.framework.Locator;
import CH.ifa.draw.framework.ParentFigure;

import CH.ifa.draw.util.Geom;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import java.util.Vector;


/**
 * AbstractFigure provides default implementations for
 * the Figure interface.
 *
 * <hr>
 * <b>Design Patterns</b><P>
 * <img src="images/red-ball-small.gif" width=6 height=6 alt=" o ">
 * <b><a href=../pattlets/sld036.htm>Template Method</a></b><br>
 * Template Methods implement default and invariant behavior for
 * figure subclasses.
 * <hr>
 *
 * @see Figure
 * @see Handle
 */
public abstract class AbstractFigure implements ParentFigure {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(AbstractFigure.class);
    public static org.apache.log4j.Logger guilogger = org.apache.log4j.Logger
                                                          .getLogger("GuiLogger");

    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -10857585979273442L;

    /**
     * The listeners for a figure's changes.
     * <p>
     * This field is transient, all listeners must register
     * themselves again via {@link #addFigureChangeListener}
     * after the figure was restored from a stream.
     * </p>
     *
     * @see #invalidate
     * @see #changed
     * @see #willChange
     */
    private transient FigureChangeListener fListener;
    @SuppressWarnings("unused")
    private int abstractFigureSerializedDataVersion = 1;

    /**
     * Determines whether the figure gets displayed or not.
     * <p>
     * This field gets serialized, if the figure is
     * written to a <code>ObjectOutputStream</code>.
     * However, if the figure is written to a
     * <code>StorableOutput</code>, the contents of this field
     * is omitted. Instead, the subclass has to
     * <UL>
     * <LI>forward <code>setAttribute()</code> calls to this
     *     class, but also to</LI>
     *
     * <LI>store the value as an attribute (the attribute
     *     is named "Visibility") and</LI>
     *
     * <LI>make a <code>setAttribute</code> call to this class,
     *     after it has restored the attribute's value from a
     *     <code>StorableInput</code>.</LI>
     * </UL>
     * </p>
     * @serial
     **/
    private boolean fVisible = true;

    /**
     * Contains all {@link ChildFigure}s which have this
     * figure as {@link ParentFigure}.
     * <p>
     * This field transient to reproduce the behaviour as
     * it is implemented in the Storable mechanism: If the
     * figure is written to a <code>StorableOutput</code>,
     * the contents of this field is omitted. Instead,
     * the children figures must add themselves via
     * {@link #addChild} again, when they are restored
     * from the <code>StorableInput</code>.
     * </p>
     **/
    protected transient Vector<Figure> children = new Vector<Figure>();

    protected AbstractFigure() {
        if (guilogger.isTraceEnabled()) {
            guilogger.trace("AbstractFigure: created " + this);
        }
    }

    /**
     * Draws the figure in a hilighted appearance.
     * @param g the Graphics to draw into
     */
    public void draw(Graphics g, DrawingContext dc) {
        if (dc.isVisible(this)) {
            draw(g);
        }
    }

    /**
     * Moves the figure by the given offset.
     */
    public void moveBy(int dx, int dy) {
        willChange();
        basicMoveBy(dx, dy);
        FigureEnumeration children = children();
        while (children.hasMoreElements()) {
            ((ChildFigure) children.nextElement()).updateLocation();
        }
        changed();
    }

    /**
     * Moves the figure. This is the
     * method that subclassers override. Clients usually
     * call displayBox.
     * @see #moveBy
     */
    protected abstract void basicMoveBy(int dx, int dy);

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
        basicDisplayBox(origin, corner);
        changed();
    }

    /**
     * Sets the display box of a figure. This is the
     * method that subclassers override. Clients usually
     * call displayBox.
     * @see #displayBox
     */
    public abstract void basicDisplayBox(Point origin, Point corner);

    /**
     * Gets the display box of a figure.
     */
    public abstract Rectangle displayBox();

    /**
     * Returns the handles of a Figure that can be used
     * to manipulate some of its attributes.
     * @return a Vector of handles
     * @see Handle
     */
    public abstract Vector<Handle> handles();

    /**
     * Returns an Enumeration of the figures contained in this figure.
     * @see CompositeFigure
     */
    public FigureEnumeration figures() {
        Vector<Figure> figures = new Vector<Figure>(1);
        figures.addElement(this);
        return new FigureEnumerator(figures);
    }

    /**
     * Gets the size of the figure. A convenience method.
     */
    public Dimension size() {
        Rectangle box = displayBox(); // more efficient to use a local variable!
        return new Dimension(box.width, box.height);
        //return new Dimension(displayBox().width, displayBox().height);
    }

    /**
     * Checks if the figure is empty. The default implementation returns
     * true if the width or height of its display box is < 3
     * @see Figure#isEmpty
     */
    public boolean isEmpty() {
        return (size().width < 3) || (size().height < 3);
    }

    /**
     * Returns the figure that contains the given point.
     * In contrast to containsPoint it returns its
     * innermost figure that contains the point.
     *
     * @see #containsPoint
     */
    public Figure findFigureInside(int x, int y) {
        if (containsPoint(x, y)) {
            return this;
        }
        return null;
    }

    /**
     * Checks if a point is inside the figure.
     */
    public boolean containsPoint(int x, int y) {
        if (isVisible()) {
            return displayBox().contains(x, y);
        } else {
            return false;
        }
    }

    /**
     * Changes the display box of a figure. This is a
     * convenience method. Implementors should only
     * have to override basicDisplayBox
     * @see #displayBox
     */
    public void displayBox(Rectangle r) {
        displayBox(new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height));
    }

    /**
     * Checks whether the given figure is contained in this figure.
     */
    public boolean includes(Figure figure) {
        return figure == this;
    }

    /**
     * Decomposes a figure into its parts. It returns a Vector
     * that contains itself.
     * @return an Enumeration for a Vector with itself as the
     * only element.
     */
    public FigureEnumeration decompose() {
        Vector<Figure> figures = new Vector<Figure>(1);
        figures.addElement(this);
        return new FigureEnumerator(figures);
    }

    /**
     * Sets the Figure's container and registers the container
     * as a figure change listener. A figure's container can be
     * any kind of FigureChangeListener. A figure is not restricted
     * to have a single container.
     */
    public void addToContainer(FigureChangeListener c) {
        addFigureChangeListener(c);
        invalidate();
    }

    /**
     * Removes a figure from the given container and unregisters
     * it as a change listener.
     */
    public void removeFromContainer(FigureChangeListener c) {
        invalidate();
        removeFigureChangeListener(c);
        changed();
    }

    /**
     * Adds a listener for this figure.
     */
    public void addFigureChangeListener(FigureChangeListener l) {
        fListener = FigureChangeEventMulticaster.add(fListener, l);
    }

    /**
     * Removes a listener for this figure.
     */
    public void removeFigureChangeListener(FigureChangeListener l) {
        fListener = FigureChangeEventMulticaster.remove(fListener, l);
    }

    /**
     * Gets the figure's listners.
     */
    public FigureChangeListener listener() {
        return fListener;
    }

    /**
     * A figure is released from the drawing. You never call this
     * method directly. Release notifies its listeners.
     * @see Figure#release
     */
    public void release() {
        if (fListener != null) {
            fListener.figureRemoved(new FigureChangeEvent(this));

            if (guilogger.isTraceEnabled()) {
                guilogger.trace("AbstractFigure: removed " + this);
            }
        }
    }

    /**
     * Invalidates the figure. This method informs the listeners
     * that the figure's current display box is invalid and should be
     * refreshed.
     */
    public void invalidate() {
        if (fListener != null) {
            Rectangle r = displayBox();

            // calculating the actual needed space based on the displayBoxes 
            // of the handles is too expensive and causes a noticeable 
            // performance decrease.  
            Dimension additionalSpace = additionalInvalidationSpace();
            r.grow(Handle.HANDLESIZE + 10 + additionalSpace.width,
                   Handle.HANDLESIZE + 10 + additionalSpace.height);
            fListener.figureInvalidated(new FigureChangeEvent(this, r));
        }
    }

    /**
     * If a subclass uses handles, that are outside the figure, this method
     * can be overridden to increase the redraw area to prevent redraw
     * problems.
     *
     * The invalidate() method uses the size of the display box for
     * invalidation but does not take the actual handle size into account.
     */
    protected Dimension additionalInvalidationSpace() {
        return new Dimension(0, 0);
    }


    /**
     * Informs that a figure is about to change something that
     * affects the contents of its display box.
     *
     * @see Figure#willChange
     */
    public void willChange() {
        invalidate();
    }

    /**
     * Informs that a figure changed the area of its display box.
     *
     * @see FigureChangeEvent
     * @see Figure#changed
     */
    public void changed() {
        invalidate();
        if (fListener != null) {
            fListener.figureChanged(new FigureChangeEvent(this));
        }
    }

    /**
     * Informs that a figure's handles have changed.
     *
     * @see FigureChangeEvent
     * @see Figure#changed
     */
    public void handlesChanged() {
        if (fListener != null) {
            fListener.figureHandlesChanged(new FigureChangeEvent(this));
        }
    }

    /**
     * Gets the center of a figure. A convenience
     * method that is rarely overridden.
     */
    public Point center() {
        return Geom.center(displayBox());
    }

    /**
     * Checks if this figure can be connected. By default
     * AbstractFigures can be connected.
     */
    public boolean canConnect() {
        return true;
    }

    /**
     * Returns the connection inset. The connection inset
     * defines the area where the display box of a
     * figure can't be connected. By default the entire
     * display box can be connected.
     *
     */
    public Insets connectionInsets() {
        return new Insets(0, 0, 0, 0);
    }

    /**
     * Returns the Figures connector for the specified location.
     * By default a ChopBoxConnector is returned.
     * @see ChopBoxConnector
     */
    public Connector connectorAt(int x, int y) {
        return new ChopBoxConnector(this);
    }

    /**
     * Same as connectorAt(p.x, p.y).
     * If in doubt overwrite the other method.
     */
    public Connector connectorAt(Point p) {
        return connectorAt(p.x, p.y);
    }

    /**
     * Sets whether the connectors should be visible.
     * By default they are not visible and
     */
    public void connectorVisibility(boolean isVisible) {
    }

    /**
     * Returns the locator used to located connected text.
     */
    public Locator connectedTextLocator(Figure text) {
        return RelativeLocator.center();
    }

    /**
     * Returns the named attribute or null if a
     * a figure doesn't have an attribute.
     * By default
     * figures don't have any attributes getAttribute
     * returns null.
     */
    public Object getAttribute(String name) {
        if ("Visibility".equals(name)) {
            return new Boolean(isVisible());
        }
        return null;
    }

    /**
     * Sets the named attribute to the new value. By default
     * figures don't have any attributes and the request is ignored.
     */
    public void setAttribute(String name, Object value) {
        if ("Visibility".equals(name)) {
            setVisible(((Boolean) value).booleanValue());
        }
    }

    /**
     * Clones a figure. Creates a clone by using the storable
     * mechanism to flatten the Figure to stream followed by
     * resurrecting it from the same stream.
     *
     * @see Figure#clone
     */
    public Object clone() {
        Object clone = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream(200);
        try {
            ObjectOutput writer = new ObjectOutputStream(output);
            writer.writeObject(this);
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        InputStream input = new ByteArrayInputStream(output.toByteArray());
        try {
            ObjectInput reader = new ObjectInputStream(input);
            clone = reader.readObject();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }

        if (guilogger.isTraceEnabled()) {
            guilogger.trace("AbstractFigure: created " + clone);
        }

        return clone;
    }

    /**
     * Stores the Figure to a StorableOutput.
     */
    public void write(StorableOutput dw) {
        // It is assume by PolyLineFigure that AbstractFigure
        // does not write to the storable output.
    }

    /**
     * Reads the Figure from a StorableInput.
     */
    public void read(StorableInput dr) throws IOException {
        // It is assume by PolyLineFigure that AbstractFigure
        // does not read from the storable output.
    }

    /**
     * Deserialization method, behaves like default readObject
     * method except setting a default value for the transient
     * field <code>children</code>.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        children = new Vector<Figure>();
    }

    public FigureEnumeration children() {
        return new FigureEnumerator(children);
    }

    public void addChild(ChildFigure child) {
        children.addElement(child);
        addFigureChangeListener(child);
    }

    public void removeChild(ChildFigure child) {
        children.removeElement(child);
        removeFigureChangeListener(child);
    }

    private boolean selectAllChildren(DrawingView view) {
        FigureEnumeration childenumeration = children();
        if (childenumeration.hasMoreElements()) {
            view.clearSelection();
            view.addToSelectionAll(childenumeration);
            return true;
        } else {
            return false;
        }
    }

    public boolean inspect(DrawingView view, boolean alternate) {
        if (alternate) {
            return false;
        } else {
            if (this instanceof ChildFigure) { // somewhat a hack...
                Figure parent = ((ChildFigure) this).parent();
                if (parent != null) {
                    view.clearSelection();
                    view.addToSelection(parent);
                    return true;
                }
            } else {
                return selectAllChildren(view);
            }
            return false;
        }
    }

    public FigureEnumeration getFiguresWithDependencies() {
        return children();
    }

    public boolean isVisible() {
        return fVisible;
    }

    public void setVisible(boolean visible) {
        willChange();
        fVisible = visible;
        changed();
    }

    /** Returns whether this figure can be selected.
     */
    public boolean isSelectable() {
        return true;
    }
}