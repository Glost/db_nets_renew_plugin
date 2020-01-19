package de.renew.gui;

import CH.ifa.draw.figures.FigureAttributes;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.framework.ParentFigure;

import CH.ifa.draw.standard.AbstractFigure;
import CH.ifa.draw.standard.FigureEnumerator;
import CH.ifa.draw.standard.MergedFigureEnumerator;
import CH.ifa.draw.standard.NullHandle;
import CH.ifa.draw.standard.OffsetLocator;
import CH.ifa.draw.standard.RelativeLocator;

import CH.ifa.draw.util.AWTSynchronizedUpdate;

import de.renew.remote.EventListener;
import de.renew.remote.MarkingAccessor;
import de.renew.remote.ObjectAccessor;
import de.renew.remote.PlaceInstanceAccessor;
import de.renew.remote.RemoteEventForwarder;
import de.renew.remote.TokenCountsAccessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;

import java.rmi.RemoteException;

import java.util.Enumeration;
import java.util.Vector;


public class TokenBagFigure extends SimpleCompositeFigure implements ChildFigure,
                                                                     EventListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(TokenBagFigure.class);
    private static final int BORDER = 3;
    private static final int SEPARATOR = 1;
    private OffsetLocator fLocator = null;
    private Drawing lockDrawing;
    private AbstractFigure figure;
    private PlaceInstanceAccessor placeInstance;
    private RemoteEventForwarder forwarder;
    private int cacheMult = -1;
    private int cacheTestMult = -1;
    private TextFigure cardFigure;
    private FigureAttributes attributes;
    private int markingAppearance = -1; // force first-time update
    private AWTSynchronizedUpdate updateTask = null;

    public TokenBagFigure() {
        super();
    }

    public TokenBagFigure(Drawing lockDrawing, AbstractFigure figure,
                          PlaceInstanceAccessor pi) {
        this(lockDrawing, figure, pi, PlaceFigure.CARDINALITY);
    }

    public TokenBagFigure(Drawing lockDrawing, AbstractFigure figure,
                          PlaceInstanceAccessor pi, int markingAppearance) {
        placeInstance = pi;
        // this.mode = mode;
        this.lockDrawing = lockDrawing;
        cardFigure = new TextFigure();
        cardFigure.setReadOnly(true);
        cardFigure.setAttribute("FontStyle", new Integer(Font.ITALIC));
        attributes = new FigureAttributes();
        if (figure != null) {
            setParent(figure);
        }
        setMarkingAppearance(markingAppearance);

        try {
            forwarder = new RemoteEventForwarder(this);
            placeInstance.addRemoteEventListener(forwarder);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected Vector<Handle> basicHandles() {
        // public Vector handles() {
        Vector<Handle> handles = super.basicHandles();

        handles.addElement(new NullHandle(this, RelativeLocator.northWest()));
        handles.addElement(new NullHandle(this, RelativeLocator.northEast()));
        handles.addElement(new NullHandle(this, RelativeLocator.southWest()));
        handles.addElement(new NullHandle(this, RelativeLocator.southEast()));
        return handles;
    }

    /*
         * public void draw(Graphics g) { if (expanded) { g.setColor(Color.red);
         * Rectangle box=displayBox();
         * g.drawRect(box.x,box.y,box.width-1,box.height-1); } super.draw(g); }
         */
    public boolean canBeParent(ParentFigure p) {
        return (p instanceof AbstractFigure && (figure == null || figure == p));
    }

    private void unsetParent() {
        if (figure != null) {
            figure.removeChild(this);
            figure = null;
        }
    }

    public boolean setParent(ParentFigure figure) {
        if (!canBeParent(figure)) {
            return false;
        }
        if (this.figure == figure) {
            return true;
        }
        unsetParent();
        this.figure = (AbstractFigure) figure;
        if (figure == null) {
            fLocator = null;
        } else {
            figure.addChild(this);
            if (fLocator != null) {
                fLocator.setBase(figure.connectedTextLocator(this));
            } else {
                fLocator = new OffsetLocator(figure.connectedTextLocator(this));
            }
            updateLocation();
        }
        return true;
    }

    public ParentFigure parent() {
        return figure;
    }

    protected void layout() {
        Point partOrigin = displayBox().getLocation();

        partOrigin.translate(BORDER, BORDER);
        Dimension extent = new Dimension(0, 0);

        FigureEnumeration k = figures();

        while (k.hasMoreElements()) {
            Figure f = k.nextFigure();

            Dimension partExtent = f.size();
            Point corner = new Point(partOrigin.x + partExtent.width,
                                     partOrigin.y + partExtent.height);

            f.basicDisplayBox(partOrigin, corner);

            extent.width = Math.max(extent.width, partExtent.width);
            extent.height += partExtent.height + SEPARATOR;
            partOrigin.y += partExtent.height + SEPARATOR;
        }
        fDisplayBox.width = extent.width + 2 * BORDER;
        fDisplayBox.height = extent.height - SEPARATOR + 2 * BORDER;
        updateLocation();
        super.layout();
    }

    protected boolean needsLayout() {
        Dimension extent = new Dimension(0, -SEPARATOR + 2 * BORDER);

        FigureEnumeration k = figures();

        while (k.hasMoreElements()) {
            Figure f = k.nextFigure();

            extent.width = Math.max(extent.width, f.size().width);
            extent.height += f.size().height + SEPARATOR;
        }
        extent.width += 2 * BORDER;
        return !extent.equals(fDisplayBox.getSize());
    }

    public void moveBy(int x, int y) {
        super.moveBy(x, y);
        if (fLocator != null) {
            fLocator.moveBy(x, y);
        }
    }

    /**
         * Updates the location relative to the connected figure. The Figure is
         * centered around the located point.
         */
    public void updateLocation() {
        if (fLocator != null) {
            Point p = fLocator.locate(figure);
            Rectangle box = displayBox();

            p.x -= box.x + box.width / 2;
            p.y -= box.y + box.height / 2;

            if (p.x != 0 || p.y != 0) {
                willChange();
                basicMoveBy(p.x, p.y);
                changed();
            }
        }
    }

    /**
         * Receives the {@link EventListener} events.
         * <p>
         * This method is naturally called asynchronously to the AWT event
         * queue, therefore the real update is scheduled to be executed within
         * the AWT thread. If multiple update notifications occur before the
         * update execution, those are ignored.
         * </p>
         */
    public void update() {
        if (updateTask == null) {
            updateTask = new AWTSynchronizedUpdate(new Runnable() {
                    public void run() {
                        executeUpdate();
                    }
                });
        }
        updateTask.scheduleUpdate();
    }

    private void executeUpdate() {
        Drawing theLockDrawing = lockDrawing;

        if (theLockDrawing == null) {
            // This figure has already been released.
            return;
        }

        if (lockDrawing == null) {
            // The figure was just now concurrently released (such
            // a check would not be needed if all modifications
            // occur in synchronization with the EventQueue).
            return;
        }

        if (markingAppearance == PlaceFigure.CARDINALITY) {
            updateCardFigure();
        } else {
            updateTokenFigures();
        }

        if (listener() != null) {
            listener().figureRequestUpdate(new FigureChangeEvent(this));

            // Is it wise to lock the drawing while doing the
            // notifications? Probably yes.
        }
    }

    protected void updateCardFigure() {
        try {
            TokenCountsAccessor tokenCounts = placeInstance.getTokenCounts();
            int newTestMult = tokenCounts.getTestedTokenCount();
            int newMult = tokenCounts.getFreeTokenCount() + newTestMult;

            if ((newMult != cacheMult) || (newTestMult != cacheTestMult)) {
                cacheMult = newMult;
                cacheTestMult = newTestMult;
                if (newTestMult > 0) {
                    if (newMult > 0) {
                        cardFigure.setText(newMult + "(" + newTestMult + ")");
                    } else {
                        cardFigure.setText("(" + newTestMult + ")");
                    }
                } else if (newMult > 0) {
                    cardFigure.setText(String.valueOf(newMult));
                } else {
                    cardFigure.setText(" ");
                }
            }
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void updateTokenFigures() {
        /*
         * Vector tempTokens = new Vector(); Enumeration tokens =
         * placeInstance.getDistinctTestableTokens();
         *
         * while (tokens.hasMoreElements()) {
         * tempTokens.addElement(tokens.nextElement()); } Object[] newTokens =
         * new Object[tempTokens.size()]; TimeSet[] newTimeSets = new
         * TimeSet[newTokens.length]; boolean[] newIsTested = new
         * boolean[newTokens.length];
         *
         * tempTokens.copyInto(newTokens); for (int i = 0; i < newTokens.length;
         * i++) { newTimeSets[i] = placeInstance.getFreeTimeSet(newTokens[i]);
         * newIsTested[i] = placeInstance.containsTestedToken(newTokens[i]); }
         */


        // Render marking information.
        removeAll();
        willChange();

        try {
            MarkingAccessor marking = placeInstance.getMarking();

            boolean expanded = markingAppearance == PlaceFigure.EXPANDED_TOKENS;
            int distinctTokenCount = marking.getDistinctTokenCount();
            for (int i = 0; i < distinctTokenCount; i++) {
                ObjectAccessor token = marking.getToken(i);
                boolean isTested = marking.getTokenTested(i);
                double[] times = marking.getTokenTimes(i);
                int[] mults = marking.getTokenTimeMultiplicities(i);

                for (int j = 0; j < times.length; j++) {
                    double time = times[j];
                    int mult = mults[j];
                    boolean showTested = isTested
                                         && time == marking.getCurrentTime();

                    Figure tokenFigure = new MultipleTokenFigure(mult,
                                                                 showTested,
                                                                 time, token,
                                                                 expanded);
                    Enumeration<String> attrenumeration = attributes
                                                              .definedAttributes();
                    while (attrenumeration.hasMoreElements()) {
                        String attribute = attrenumeration.nextElement();
                        tokenFigure.setAttribute(attribute,
                                                 attributes.get(attribute));
                    }
                    add(tokenFigure);
                }
            }
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        }

        layout();
        changed();
    }

    public void release() {
        // Later on, I will not even know my place,
        // let alone the drawing. So keep a note.
        Drawing theDrawing = lockDrawing;

        if (theDrawing == null) {
            // This figure is released a second time.
            return;
        }

        //theDrawing.lock();
        try {
            if (lockDrawing == null) {
                // This figure has been released concurrently.
            }

            if (placeInstance != null) {
                try {
                    placeInstance.removeRemoteEventListener(forwarder);
                } catch (RemoteException e) {
                    logger.error(e.getMessage(), e);
                }

                // OK: Release reference to place instance so that the place
                // instance can be garbage collected.
                placeInstance = null;
            }

            // OK: Forget about the place.
            unsetParent();

            // Indicate release.
            lockDrawing = null;

            // OK: Forget about all tokens.
            removeAll();

            super.release();
        } finally {
            //theDrawing.unlock();
        }
    }

    public String toString() {
        if (placeInstance == null) {
            return "Detached TokenBagFigure@" + System.identityHashCode(this);
        }

        StringBuffer output = new StringBuffer().append('{');

        try {
            MarkingAccessor marking = placeInstance.getMarking();

            int distinctTokenCount = marking.getDistinctTokenCount();
            for (int i = 0; i < distinctTokenCount; i++) {
                ObjectAccessor token = marking.getToken(i);
                int mult = marking.getTokenFreeCount(i);
                boolean isTested = marking.getTokenTested(i);

                output.append(MultipleTokenFigure.getMultString(mult, isTested));
                output.append(((token == null) ? "null" : token.asString()));

                if (i < distinctTokenCount - 1) {
                    output.append(", ");
                }
            }

            output.append('}');
            return output.toString();
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }

    public boolean wantSynchronousNotification() {
        return false;
    }

    public void setMarkingAppearance(int markingAppearance) {
        if (this.markingAppearance != markingAppearance
                    && markingAppearance != PlaceFigure.HIGHLIGHT) {
            willChange();
            this.markingAppearance = markingAppearance;
            if (markingAppearance == PlaceFigure.CARDINALITY) {
                removeAll();
                add(cardFigure);
            }
            update();
            layout();
            changed();
        }
    }

    /**
         * Returns whether this figure can be selected.
         */


    // public boolean isSelectable() { return false; }
    public boolean inspect(DrawingView view, boolean alternate) {
        // undo support can be implemented here
        // (currently disabled for instance drawings)
        boolean done = super.inspect(view, alternate);

        if (alternate && !done) {
            view.clearSelection();
            int newMA;

            if (markingAppearance == PlaceFigure.CARDINALITY) {
                if (figure == null) {
                    // in seperate window:
                    newMA = PlaceFigure.EXPANDED_TOKENS;
                } else {
                    newMA = PlaceFigure.TOKENS;
                }
            } else {
                newMA = PlaceFigure.CARDINALITY;
            }
            setAttribute("MarkingAppearance", new Integer(newMA));
            view.addToSelection(this);
        }
        return done;
    }

    protected PlaceInstanceAccessor getPlaceInstance() {
        return placeInstance;
    }

    public void update(FigureChangeEvent e) {
        updateLocation();
        super.update(e);
    }

    public void setAttribute(String name, Object value) {
        if (name.equals("MarkingAppearance")) {
            if (figure != null) {
                figure.setAttribute(name, value);
            }
            // if we want TokenBagFigures to also
            // be switchable if there is no PlaceInstanceFigure
            // assigned, we need:
            else {
                setMarkingAppearance(((Integer) value).intValue());
            }
        } else {
            if (markingAppearance != PlaceFigure.CARDINALITY) {
                attributes.set(name, value);
            }
            super.setAttribute(name, value);
        }
    }

    /**
         * Returns the figures with dependencies of superclasses plus the parent
         * figure of this figure.
         */
    public FigureEnumeration getFiguresWithDependencies() {
        FigureEnumeration superDep = super.getFiguresWithDependencies();
        Vector<Figure> myDep = new Vector<Figure>(1);

        myDep.addElement(figure);
        return new MergedFigureEnumerator(superDep, new FigureEnumerator(myDep));
    }

    /**
     * This class is not serializable, although inheriting serializability
     * from its superclass.
     * <p>
     * Reason: Serialization is currently used for Copy&Paste and the Undo
     * machanism only. As such modifications don't make sense for instance
     * drawings, this figure, which is a part of such drawings, doesn't need
     * to be serializable.
     * </p>
     * <p>
     * Second reason: Some fields of this class would lead to a
     * serialization of the compiled net, which is not desirable.
     * </p>
     *
     * @throws java.io.NotSerializableException
     *                 always.
     */
    private void writeObject(java.io.ObjectOutputStream out)
            throws java.io.IOException {
        throw new java.io.NotSerializableException("TokenBagFigure is not serializable!");
    }

    /**
         * Deserialization method, behaves like default readObjec method except
         * restoring the connection to the parent of this child figure.
         */
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (figure != null) {
            figure.addChild(this);
        }
    }

    protected TextFigure getCardFigure() {
        return cardFigure;
    }

    protected int getCachedMultiplicity() {
        return cacheMult;
    }

    protected int getCachedTestMultiplicty() {
        return cacheTestMult;
    }

    protected void setCachedMultiplicity(int cacheMult) {
        this.cacheMult = cacheMult;
    }

    protected void setCachedTestMultiplicty(int cacheTestMult) {
        this.cacheTestMult = cacheTestMult;
    }
}