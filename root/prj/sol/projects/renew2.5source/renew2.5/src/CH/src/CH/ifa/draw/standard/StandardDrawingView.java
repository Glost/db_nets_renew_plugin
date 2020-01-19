/*
 * @(#)StandardDrawingView.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingChangeEvent;
import CH.ifa.draw.framework.DrawingChangeListener;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureChangeListener;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureSelection;
import CH.ifa.draw.framework.FigureWithDependencies;
import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.framework.Painter;
import CH.ifa.draw.framework.PointConstrainer;
import CH.ifa.draw.framework.Tool;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.ContextGraphics;
import CH.ifa.draw.util.GUIProperties;
import CH.ifa.draw.util.Geom;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.PrintGraphics;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * The standard implementation of DrawingView.
 * @see DrawingView
 * @see Painter
 * @see Tool
 */
public class StandardDrawingView extends JPanel implements DrawingView,
                                                           FigureChangeListener,
                                                           MouseListener,
                                                           MouseMotionListener,
                                                           KeyListener {
    /*
     * Serialization support. In JavaDraw only the Drawing is serialized.
     * However, for beans support StandardDrawingView supports
     * serialization
     */
    private static final long serialVersionUID = -3878153366174603336L;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(StandardDrawingView.class);

    /**
     * The DrawingEditor of the view.
     * @see #tool
     * @see #setStatus
     */
    transient private DrawingEditor fEditor;

    /**
     * The shown drawing.
     */
    private Drawing fDrawing;

    /**
     * the accumulated damaged area
     */
    private transient Rectangle fDamage = null;

    /**
     * The list of currently selected figures.
     */
    private transient Vector<Figure> fSelection;

    /**
     * Caches the shown selection handles.
     * Do not access (not even for reading) this variable
     * directly, always use {@link #selectionHandles} and
     * {@link #selectionInvalidateHandles}!
     **/
    transient private Vector<Handle> fSelectionHandles;

    /**
     * This object's monitor is used to synchronize the update of
     * the {@link #fSelectionHandles} cache.
     **/
    transient private Object selectionHandlesLock = new Object();

    /**
     * The preferred size of the view
     */
    private Dimension fViewSize;

    /**
     * The position of the last mouse click
     * inside the view.
     */
    private Point fLastClick;

    /**
     * A vector of optional backgrounds. The vector maintains
     * a list a view painters that are drawn before the contents,
     * that is in the background.
     */
    private Vector<Painter> fBackgrounds = null;

    /**
     * A vector of optional foregrounds. The vector maintains
     * a list a view painters that are drawn after the contents,
     * that is in the foreground.
     */
    private Vector<Painter> fForegrounds = null;

    /**
     * The update strategy used to repair the view.
     */
    private Painter fUpdateStrategy;

    /**
     * The grid used to constrain points for snap to
     * grid functionality.
     */
    private PointConstrainer fConstrainer;
    @SuppressWarnings("unused")
    private int drawingViewSerializedDataVersion = 1;

    /**
     * The monitor of this object is used to synchronize
     * the merging of overlapping repaint events.
     **/
    private transient Object repaintLock = new Object();

    /**
     * Constructs the view.
     */
    public StandardDrawingView(DrawingEditor editor, int width, int height) {
        fEditor = editor;
        fViewSize = new Dimension(width, height);
        fLastClick = new Point(0, 0);
        fConstrainer = null;
        fSelection = new Vector<Figure>();
        if (GUIProperties.specialUpdate()) {
            setDisplayUpdate(new SimpleUpdateStrategy());
        } else {
            setDisplayUpdate(new BufferedUpdateStrategy());
        }
        setBackground(Color.lightGray);
        setOpaque(true);
        setLayout(null);


        // The following calls are now made from the outside
        // (see CH.ifa.draw.application.DrawingViewFrame).
        //
        //addMouseListener(this);
        //addMouseMotionListener(this);
        //addKeyListener(this);
    }

    /**
     * Sets the view's editor.
     */
    public void setEditor(DrawingEditor editor) {
        fEditor = editor;
    }

    /**
     * Gets the current tool.
     */
    public Tool tool() {
        return fEditor.tool();
    }

    /**
     * Gets the drawing.
     */
    public Drawing drawing() {
        return fDrawing;
    }

    /**
     * Sets and installs another drawing in the view.
     */
    public void setDrawing(Drawing d) {
        if (fDrawing != null) {
            clearSelection();
            fDrawing.removeDrawingChangeListener(this);
        }

        fDrawing = d;
        fDrawing.addDrawingChangeListener(this);
        checkMinimumSize(null);
        repaint();
    }

    /**
     * Gets the editor.
     */
    public DrawingEditor editor() {
        return fEditor;
    }

    /**
     * Adds a figure to the drawing.
     * @return the added figure.
     */
    public Figure add(Figure figure) {
        return drawing().add(figure);
    }

    /**
     * Removes a figure from the drawing.
     * @return the removed figure
     */
    public Figure remove(Figure figure) {
        return drawing().remove(figure);
    }

    /**
     * Adds a vector of figures to the drawing.
     */
    public void addAll(Vector<Figure> figures) {
        FigureEnumeration k = new FigureEnumerator(figures);
        while (k.hasMoreElements()) {
            add(k.nextFigure());
        }
    }

    /**
     * Removes a vector of figures from the drawing.
     */
    public void removeAll(Vector<Figure> figures) {
        FigureEnumeration k = new FigureEnumerator(figures);
        while (k.hasMoreElements()) {
            remove(k.nextFigure());
        }
    }

    /**
     * Gets the minimum dimension of the drawing.
     */
    public Dimension getMinimumSize() {
        return fViewSize;
    }

    /**
     * Gets the preferred dimension of the drawing..
     */
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    /**
     * Sets the current display update strategy.
     * @see Painter
     */
    public void setDisplayUpdate(Painter updateStrategy) {
        fUpdateStrategy = updateStrategy;
    }

    /**
     * Gets the currently selected figures.
     * @return a vector with the selected figures. The vector
     * is a copy of the current selection.
     */
    public Vector<Figure> selection() {
        //protect the vector with the current selection
        return new Vector<Figure>(fSelection);
    }

    /**
     * Gets an enumeration over the currently selected figures.
     */
    public FigureEnumeration selectionElements() {
        // protect the vector with the current selection
        return new FigureEnumerator(selection());
    }

    /**
     * Sorts the given vector of figures into Z-order.
     * @return a vector with the ordered figures.
     */
    public Vector<Figure> inZOrder(Vector<Figure> selection) {
        Vector<Figure> result = new Vector<Figure>(selection.size());
        FigureEnumeration figures = drawing().figures();

        while (figures.hasMoreElements()) {
            Figure f = figures.nextFigure();
            if (selection.contains(f)) {
                result.addElement(f);
            }
        }
        return result;
    }

    /**
     * Gets the currently selected figures in Z order.
     * @see #selection
     * @return a vector with the selected figures. The vector
     * is a copy of the current selection.
     */
    public Vector<Figure> selectionZOrdered() {
        return inZOrder(fSelection);
    }

    /**
     * Gets the number of selected figures.
     */
    public int selectionCount() {
        return fSelection.size();
    }

    /**
     * Adds a figure to the current selection.
     */
    public void addToSelection(Figure figure) {
        if (addToSelectionInternal(figure)) {
            selectionChanged();
        }
    }

    /**
     * Adds a figure to the current selection.
     * Is used by addToSelection, addToSelectionAll.
     * Does not send selectionChanged messages.
     * @return true, if selection is changed,
     *         false, otherwise.
     */
    protected boolean addToSelectionInternal(Figure figure) {
        if (!fSelection.contains(figure)) {
            if (figure.isSelectable()) {
                fSelection.addElement(figure);
                figure.addFigureChangeListener(this);
                selectionInvalidateHandles();
                figure.invalidate();
                return true;
            }

            //else if (figure instanceof CompositeFigure) {
        }
        return false;
    }

    /**
     * Adds a vector of figures to the current selection.
     */
    public void addToSelectionAll(Vector<Figure> figures) {
        addToSelectionAll(new FigureEnumerator(figures));
    }

    public void addToSelectionAll(FigureEnumeration figures) {
        boolean changed = false;
        while (figures.hasMoreElements()) {
            changed = changed | addToSelectionInternal(figures.nextFigure());
        }
        if (changed) {
            selectionChanged();
        }
    }

    /**
     * Removes a figure from the selection.
     */
    public void removeFromSelection(Figure figure) {
        if (removeFromSelectionInternal(figure)) {
            selectionChanged();
        }
    }

    /**
     * Removes a figure from the selection.
     * Is used by removeFromSelection, removeFromSelectionAll.
     * Does not send selectionChanged messages.
     * @return true, if selection is changed,
     *         false, otherwise.
     */
    protected boolean removeFromSelectionInternal(Figure figure) {
        if (fSelection.contains(figure)) {
            fSelection.removeElement(figure);
            figure.removeFigureChangeListener(this);
            selectionInvalidateHandles();
            figure.invalidate();
            return true;
        }
        return false;
    }

    /**
     * Removes a vector of figures from the current selection.
     */
    public void removeFromSelectionAll(Vector<Figure> figures) {
        removeFromSelectionAll(new FigureEnumerator(figures));
    }

    /**
     * Removes an enumeration of figures from the current selection.
     */
    public void removeFromSelectionAll(FigureEnumeration figures) {
        boolean changed = false;
        while (figures.hasMoreElements()) {
            changed = changed
                      | removeFromSelectionInternal(figures.nextFigure());
        }
        if (changed) {
            selectionChanged();
        }
    }

    /**
     * If a figure isn't selected it is added to the selection.
     * Otherwise it is removed from the selection.
     */
    public void toggleSelection(Figure figure) {
        if (toggleSelectionInternal(figure)) {
            selectionChanged();
        }
    }

    /**
     * If a figure isn't selected it is added to the selection.
     * Otherwise it is removed from the selection.
     * Is used by toggleSelection, toggleSelectionAll.
     * Does not send selectionChanged messages.
     * @return true, if selection is changed,
     *         false, otherwise.
     */
    public boolean toggleSelectionInternal(Figure figure) {
        if (fSelection.contains(figure)) {
            return removeFromSelectionInternal(figure);
        } else {
            return addToSelectionInternal(figure);
        }
    }

    /**
     * Toggles a vector of figures.
     * If a figure isn't selected it is added to the selection.
     * Otherwise it is removed from the selection.
     */
    public void toggleSelectionAll(Vector<Figure> figures) {
        toggleSelectionAll(new FigureEnumerator(figures));
    }

    /**
     * Toggles an enumeration of figures.
     * If a figure isn't selected it is added to the selection.
     * Otherwise it is removed from the selection.
     */
    public void toggleSelectionAll(FigureEnumeration figures) {
        boolean changed = false;
        while (figures.hasMoreElements()) {
            changed = changed | toggleSelectionInternal(figures.nextFigure());
        }
        if (changed) {
            selectionChanged();
        }
    }

    /**
     * Clears the current selection.
     */
    public void clearSelection() {
        FigureEnumeration k = selectionElements();
        while (k.hasMoreElements()) {
            Figure fig = k.nextFigure();
            fig.removeFigureChangeListener(this);
            fig.invalidate();
        }
        fSelection = new Vector<Figure>();
        selectionInvalidateHandles();
        selectionChanged();
    }

    public void figureInvalidated(FigureChangeEvent e) {
    }

    public void figureChanged(FigureChangeEvent e) {
    }

    public void figureRemoved(FigureChangeEvent e) {
        removeFromSelection(e.getFigure());
    }

    public void figureRequestRemove(FigureChangeEvent e) {
    }

    public void figureRequestUpdate(FigureChangeEvent e) {
    }

    /**
     * Invalidates the handles of the current selection.
     * This means that the chached set of handles will be
     * re-calculated next time the selection's handles are
     * queried.
     */
    public void selectionInvalidateHandles() {
        // logger.debug("Resetting handles of "+this.getClass());
        synchronized (selectionHandlesLock) {
            fSelectionHandles = null;
        }
    }

    public void figureHandlesChanged(FigureChangeEvent e) {
        selectionInvalidateHandles();
    }

    /**
     * Gets an enumeration of the currently active handles.
     */
    private Enumeration<Handle> selectionHandles() {
        synchronized (selectionHandlesLock) {
            if (fSelectionHandles == null) {
                fSelectionHandles = new Vector<Handle>();
                FigureEnumeration k = selectionElements();
                while (k.hasMoreElements()) {
                    Figure figure = k.nextFigure();
                    Enumeration<Handle> kk = figure.handles().elements();
                    while (kk.hasMoreElements()) {
                        fSelectionHandles.addElement(kk.nextElement());
                    }
                }
            }
            return fSelectionHandles.elements();
        }
    }

    private static void tryAdd(Vector<Figure> vec, Figure obj) {
        if (obj != null && !vec.contains(obj)) {
            vec.addElement(obj);
        }
    }

    /**
     * Include into a vector of figures all those figures that are
     * referenced by the figures, also indirectly.
     */
    public static Vector<Figure> expandFigureVector(Vector<Figure> orgFigures) {
        // Clone the vector so that the existing data is not corrupted.
        Vector<Figure> figures = new Vector<Figure>(orgFigures);

        // Add neighbors of all elements of the vector.
        // Newly added elements will be processed the same way at the end.
        // This looks like a for-loop but it behaves like a while-loop 
        // because of the concurrent modification of figures.size()
        for (int i = 0; i < figures.size(); i++) {
            Figure figure = figures.elementAt(i);
            if (figure instanceof FigureWithDependencies) {
                FigureWithDependencies df = (FigureWithDependencies) figure;
                FigureEnumeration relatedFigures = df.getFiguresWithDependencies();
                while (relatedFigures.hasMoreElements()) {
                    tryAdd(figures, relatedFigures.nextFigure());
                }
            }
        }
        return figures;
    }

    /**
     * Gets the current selection as a FigureSelection. A FigureSelection
     * can be cut, copied, pasted.
     */
    public FigureSelection getFigureSelection() {
        return new FigureSelection(inZOrder(expandFigureVector(fSelection)));
    }

    /**
     * Finds a handle at the given coordinates.
     * @return the hit handle, null if no handle is found.
     */
    public Handle findHandle(int x, int y) {
        Handle handle;

        Enumeration<Handle> k = selectionHandles();
        while (k.hasMoreElements()) {
            handle = k.nextElement();
            if (handle.containsPoint(x, y)) {
                return handle;
            }
        }
        return null;
    }

    /**
     * Informs that the current selection changed.
     * By default this event is forwarded to the
     * drawing editor.
     */
    protected void selectionChanged() {
        fEditor.selectionChanged(this);
    }

    /**
     * Gets the position of the last click inside the view.
     */
    public Point lastClick() {
        return fLastClick;
    }

    /**
     * Sets the grid spacing that is used to constrain points.
     */
    public void setConstrainer(PointConstrainer c) {
        fConstrainer = c;
    }

    /**
     * Gets the current constrainer.
     */
    public PointConstrainer getConstrainer() {
        return fConstrainer;
    }

    /**
     * Constrains a point to the current grid.
     */
    protected Point constrainPoint(Point p) {
        // constrain to view size
        Dimension size = getSize();


        //p.x = Math.min(size.width, Math.max(1, p.x));
        //p.y = Math.min(size.height, Math.max(1, p.y));
        p.x = Geom.range(1, size.width, p.x);
        p.y = Geom.range(1, size.height, p.y);

        if (fConstrainer != null) {
            return fConstrainer.constrainPoint(p);
        }
        return p;
    }

    /**
     * Handles mouse down events. The event is delegated to the
     * currently active tool.
     * @return whether the event was handled.
     */
    public void mousePressed(MouseEvent e) {
        boolean rightclick = (e.getModifiers()
                             & (InputEvent.BUTTON2_MASK
                               | InputEvent.BUTTON3_MASK)) != 0;
        if (tool() != editor().defaultTool() && rightclick
                    && e.getClickCount() == 1) {
            editor().setStickyTools(false);
            editor().toolDone();
        } else {
            requestFocus(); // JDK1.1
            Point p = constrainPoint(new Point(e.getX(), e.getY()));
            fLastClick = new Point(e.getX(), e.getY());
            tool().mouseDown(e, p.x, p.y);
            checkDamage();
        }
    }

    /**
     * Handles mouse drag events. The event is delegated to the
     * currently active tool.
     * @return whether the event was handled.
     */
    public void mouseDragged(MouseEvent e) {
        Point p = constrainPoint(new Point(e.getX(), e.getY()));
        tool().mouseDrag(e, p.x, p.y);
        checkDamage();
    }

    /**
     * Handles mouse move events. The event is delegated to the
     * currently active tool.
     * @return whether the event was handled.
     */
    public void mouseMoved(MouseEvent e) {
        tool().mouseMove(e, e.getX(), e.getY());
    }

    /**
     * Handles mouse up events. The event is delegated to the
     * currently active tool.
     * @return whether the event was handled.
     */
    public void mouseReleased(MouseEvent e) {
        Point p = constrainPoint(new Point(e.getX(), e.getY()));
        tool().mouseUp(e, p.x, p.y);
        checkDamage();
    }

    /**
     * Handles key down events. Cursor keys are handled
     * by the view the other key events are delegated to the
     * currently active tool.
     * @return whether the event was handled.
     */
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if ((code == KeyEvent.VK_BACK_SPACE) || (code == KeyEvent.VK_DELETE)) {
            if (logger.isDebugEnabled()) {
                logger.debug(StandardDrawingView.class.getName()
                             + ": KeyEvent e: Delete pressed");
            }
            Command cmd = new DeleteCommand("Delete");
            cmd.execute();
        } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_UP
                           || code == KeyEvent.VK_RIGHT
                           || code == KeyEvent.VK_LEFT) {
            if (logger.isDebugEnabled()) {
                logger.debug(StandardDrawingView.class.getName()
                             + ": KeyEvent e: Arrow pressed");
            }
            handleCursorKey(code, e.getModifiers());


            // if we have a state with selected figures, pressing a cursor
            // key should only move the figures. If there are no selected 
            // figures, the view of the viewport should scroll.
            if (this.fSelection.size() > 0) {
                // consume key event. This prevent scrolling of the ScrollPane.
                e.consume();
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(StandardDrawingView.class.getName()
                             + ": KeyEvent e: Pushing KeyEvent to tool e: "
                             + code + " , " + e.getKeyChar());
            }
            tool().keyDown(e, code);
        }
        checkDamage();
    }

    /**
     * Handles cursor keys by moving all the selected figures
     * one grid point in the cursor direction.
     */
    protected void handleCursorKey(int key) {
        this.handleCursorKey(key, 0);
    }

    /**
     * Handles cursor keys by moving all the selected figures
     * one grid point in the cursor direction.
     *
     * If InputEvent.SHIFT_MASK is set in modifiers, take a bigger leap.
     */
    protected void handleCursorKey(int key, int modifiers) {
        int dx = 0;
        int dy = 0;
        int stepX = 1;
        int stepY = 1;

        // should consider Null Object.
        if (fConstrainer != null) {
            stepX = fConstrainer.getStepX();
            stepY = fConstrainer.getStepY();
        }

        // if shift is pressed, move 10 times as far
        if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
            stepX *= 10;
            stepY *= 10;
        }

        switch (key) {
        case KeyEvent.VK_DOWN:
            dy = stepY;
            break;
        case KeyEvent.VK_UP:
            dy = -stepY;
            break;
        case KeyEvent.VK_RIGHT:
            dx = stepX;
            break;
        case KeyEvent.VK_LEFT:
            dx = -stepX;
            break;
        }
        moveSelection(dx, dy);
    }

    // A call to this method must always be followed by
    // a call to checkDamage() for the affected views.
    public static void moveFigures(Vector<Figure> figureVector, int dx, int dy) {
        Figure figure;
        Figure parent;

        FigureEnumeration figures = new FigureEnumerator(figureVector);
        while (figures.hasMoreElements()) {
            figure = figures.nextFigure();
            parent = figure;
            while (parent != null) {
                if (parent instanceof ChildFigure) {
                    parent = ((ChildFigure) parent).parent();
                    if (parent != null && figureVector.contains(parent)) {
                        break;
                    }
                } else {
                    parent = null;
                }
            }
            if (parent == null) {
                figure.moveBy(dx, dy);
            }
        }
    }

    public void moveSelection(int dx, int dy) {
        moveFigures(fSelection, dx, dy);
        checkDamage();
    }

    /**
     * Checks whether the drawing has some accumulated damage
     * and informs all views about the required update,
     * if neccessary.
     */
    public synchronized void checkDamage() {
        Enumeration<DrawingChangeListener> each = drawing()
                                                      .drawingChangeListeners();
        while (each.hasMoreElements()) {
            Object l = each.nextElement();
            if (l instanceof DrawingView) {
                ((DrawingView) l).repairDamage();
            }
        }
    }

    public void repairDamage() {
        synchronized (repaintLock) {
            if (fDamage != null) {
                if ((fDamage.x + fDamage.width > fViewSize.width)
                            || (fDamage.y + fDamage.height > fViewSize.height)) {
                    checkMinimumSize(fDamage);
                }
                repaint(0L, fDamage.x, fDamage.y, fDamage.width, fDamage.height);
                fDamage = null;
            }
        }
    }

    public void drawingInvalidated(DrawingChangeEvent e) {
        synchronized (repaintLock) {
            Rectangle r = e.getInvalidatedRectangle();
            if (r != null) {
                if (fDamage == null) {
                    fDamage = r;
                } else {
                    fDamage.add(r);
                }
            }
        }
    }

    public void drawingRequestUpdate(DrawingChangeEvent e) {
        repairDamage();
    }

    /**
     * Paints the drawing view. The actual drawing is delegated to
     * the current update strategy.
     * @see Painter
     */
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        // We are currently updating the view, so further
        // updates should wait a while.
        // chronized (repaintLock) {
        //    repaintActive = true;
        // }
        fUpdateStrategy.draw(g2, this);
        synchronized (repaintLock) {
            // Any comitted damage?
            if (fDamage != null) {
                // Yes, handle the requested repaint in a separate
                // request. We cannot do the repaint directly, because
                // our graphics context might have an inappropriate
                // clip area.
                repaint(0L, fDamage.x, fDamage.y, fDamage.width, fDamage.height);
                fDamage = null;
            }
        }
    }

    /**
     * Draws the contents of the drawing view.
     * The view has three layers: background, drawing, handles.
     * The layers are drawn in back to front order.
     */
    public void drawAll(Graphics g) {
        boolean isPrinting = g instanceof PrintGraphics;
        drawBackground(g);
        if (fBackgrounds != null && !isPrinting) {
            drawPainters(g, fBackgrounds);
        }
        ContextGraphics cg = new ContextGraphics((Graphics2D) g, getBackground());
        //Double the output goes like that:
        //cg.setTransform(AffineTransform.getScaleInstance(2, 2));
        //We need to change the mouse listeners as well if we want zoom
        drawDrawing(cg);
        if (fForegrounds != null && !isPrinting) {
            drawPainters(g, fForegrounds);
        }
        if (!isPrinting) {
            drawHandles(g);
        }
        if (fDrawing == fEditor.drawing()) {
            tool().draw(g);
        }
    }

    /**
     * Draws the currently active handles.
     */
    public void drawHandles(Graphics g) {
        Enumeration<Handle> k = selectionHandles();
        while (k.hasMoreElements()) {
            k.nextElement().draw(g);
        }
    }

    /**
     * Draws the drawing.
     */
    public void drawDrawing(Graphics g) {
        fDrawing.draw(g);
    }

    /**
     * Draws the background. If a background pattern is set it
     * is used to fill the background. Otherwise the background
     * is filled in the background color.
     */
    public void drawBackground(Graphics g) {
        g.setColor(getBackground());
        Rectangle bounds = getBounds();
        g.fillRect(0, 0, bounds.width, bounds.height);
    }

    private void drawPainters(Graphics g, Vector<Painter> v) {
        for (int i = 0; i < v.size(); i++) {
            v.elementAt(i).draw(g, this);
        }
    }

    /**
     * Adds a background.
     */
    public void addBackground(Painter painter) {
        if (fBackgrounds == null) {
            fBackgrounds = new Vector<Painter>(3);
        }
        fBackgrounds.addElement(painter);
        repaint();
    }

    /**
     * Removes a background.
     */
    public void removeBackground(Painter painter) {
        if (fBackgrounds != null) {
            fBackgrounds.removeElement(painter);
        }
        repaint();
    }

    /**
     * Removes a foreground.
     */
    public void removeForeground(Painter painter) {
        if (fForegrounds != null) {
            fForegrounds.removeElement(painter);
        }
        repaint();
    }

    /**
     * Adds a foreground.
     */
    public void addForeground(Painter painter) {
        if (fForegrounds == null) {
            fForegrounds = new Vector<Painter>(3);
        }
        fForegrounds.addElement(painter);
        repaint();
    }

    /**
     * Freezes the view by acquiring the drawing lock.
     * @see Drawing#lock
     */
    public void freezeView() {
        //drawing().lock();
    }

    /**
     * Unfreezes the view by releasing the drawing lock.
     * @see Drawing#unlock
     */
    public void unfreezeView() {
        //drawing().unlock();
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException {
        s.defaultReadObject();

        fSelection = new Vector<Figure>(); // could use lazy initialization instead
        selectionHandlesLock = new Object();
        repaintLock = new Object();

        if (fDrawing != null) {
            fDrawing.addDrawingChangeListener(this);
        }
    }

    /**
     * Recalculates the size of the drawing and adapts
     * the view size, if the drawing size increased.
     * Does not adapt to a shrinking drawing size.
     *
     * @param area If <code>null</code>, all figures in
     *             the drawing will be inspected to determine
     *             the size of the drawing.
     *             <br>
     *             If a rectangle is given, its lower
     *             right corner will be used to determine
     *             the size. This is much faster, if the
     *             area is already known to the caller.
     **/
    private void checkMinimumSize(Rectangle area) {
        // Calculate (or estimate) the space occupied
        // by all figures in the drawing.
        if (area == null) {
            area = fDrawing.getBounds();
        }
        Dimension d = new Dimension(area.x + area.width, area.y + area.height);


        // Adapt the size of the view to the size
        // of the drawing.
        if (fViewSize.height < d.height) {
            fViewSize.height = d.height + 10;
        }
        if (fViewSize.width < d.width) {
            fViewSize.width = d.width + 10;
        }


        // Also look at the bounds of the gui panel to
        // avoid grey areas outside of the drawing.
        // But the gui bounds should not affect the
        // logical view size.
        Dimension guiSize = getSize();
        boolean changed = false;
        if (guiSize.width < fViewSize.width) {
            guiSize.width = fViewSize.width;
            changed = true;
        }
        if (guiSize.height < fViewSize.height) {
            guiSize.height = fViewSize.height;
            changed = true;
        }

        // Apply the changes, if neccessary.
        if (changed) {
            setSize(guiSize.width, guiSize.height);
        }
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    // listener methods we are not interested in
    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void showElement(Figure fig) {
        Component c = getParent();
        while ((c != null) && !(c instanceof JScrollPane)) {
            c = c.getParent();
        }

        if (c != null) {
            JScrollPane pane = (JScrollPane) c;
            Point p = fig.center();
            int x = p.x - (pane.getWidth() / 2);
            int y = p.y - (pane.getHeight() / 2);
            int vpWidth = pane.getViewport().getWidth();
            int vpHeight = pane.getViewport().getHeight();
            int vWidth = pane.getViewport().getView().getWidth();
            int vHeight = pane.getViewport().getView().getHeight();
            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }
            if (x + vpWidth > vWidth) {
                x = vWidth - vpWidth;
            }
            if (y + vpHeight > vHeight) {
                y = vHeight - vpHeight;
            }

            pane.getViewport().setViewPosition(new Point(x, y));
        }
    }

    /**
     * @see Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
            throws PrinterException {
        if (pageIndex >= 1) {
            return Printable.NO_SUCH_PAGE;
        }
        if (graphics instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) graphics;

            //set the upper left corner to imageable corner on printer page
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            //Scaling
            double sH = pageFormat.getImageableHeight() / getHeight();
            double sW = pageFormat.getImageableWidth() / getWidth();
            double scale = (sH > sW) ? sW : sH;

            // do we need to scale?
            if (scale < 1) {
                g2.scale(scale, scale);
            }

            drawAll(graphics);
            return Printable.PAGE_EXISTS;
        }
        return Printable.NO_SUCH_PAGE;
    }
}