/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/
package de.renew.gui;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.UndoableCommand;

import de.renew.net.Net;
import de.renew.net.Place;
import de.renew.net.PlaceInstance;
import de.renew.net.Transition;
import de.renew.net.TransitionInstance;

import de.renew.remote.PlaceInstanceAccessor;
import de.renew.remote.PlaceInstanceAccessorImpl;
import de.renew.remote.TransitionInstanceAccessor;
import de.renew.remote.TransitionInstanceAccessorImpl;

import java.util.Vector;


/**
 * Command to add, remove or toggle simulation breakpoints
 * at transition figures, place figures or their instance
 * figures.
 * <p>
 * </p>
 * ToggleBreakpointCommand.java
 * Created: Tue May 23  2000
 * @author Michael Duvigneau
 */
public class ToggleBreakpointCommand extends UndoableCommand {

    /**
     * Operation mode:
     * Add breakpoints to the selected figures wether or not
     * there exist any breakpoints already.
     */
    public static final int ADD = 1;

    /**
     * Operation mode:
     * Remove all breakpoints from the selected figures which
     * there exist already.
     */
    public static final int REMOVE = 2;

    /**
     * Operation context:
     * Force the creation/removal of global breakpoints.
     * E.g. try to find the static transition or place behind
     * any selected figure and add the breakpoint there.
     * This mode also allows to create preset breakpoints
     * in 'normal' CPNDrawings.
     */
    public static final int GLOBAL = 10;

    /**
     * Operation context:
     * Force the creation/removal of local breakpoints.
     * E.g. try to find the transition or place instance behind
     * any selected figure and add the breakpoint there.
     * This mode restricts the breakpoint manipulation to
     * CPNInstanceDrawings.
     */
    public static final int LOCAL = 11;

    /**
     * Operation context:
     * This mode allows to create preset breakpoints
     * in 'normal' CPNDrawings (marking node figures
     * with a breakpoint attribute).
     */
    public static final int PRESET = 12;
    private BreakpointManager manager;

    // private DrawingEditor editor;
    private int op;
    private int context;
    private int breakpointMode;

    /**
     * Constructs an add, remove or toggle breakpoint command
     * manipulating breakpoints with default mode.
     *
     * @param name    the command name
     * @param manager the manager to add the breakpoint to
     *                and to request necessary information from.
     * @param op      one of ADD, REMOVE
     * @param context one of GLOBAL, LOCAL, PRESET
     *
     * @see #ADD
     * @see #REMOVE
     * @see #GLOBAL
     * @see #LOCAL
     */
    public ToggleBreakpointCommand(String name, BreakpointManager manager,
                                   int op, int context) {
        this(name, manager, op, context, Breakpoint.DEFAULT);
    }

    /**
     * Constructs an add, remove or toggle breakpoint command
     * concerning breakpoints with the specified mode.
     * <p>
     * If Breakpoint removal is selected, the mode will be
     * ignored.
     * </p>
     *
     * @param name    the command name
     * @param manager the manager to add the breakpoint to
     *                and to request necessary information from.
     * @param op      one of ADD, REMOVE
     * @param context one of GLOBAL, LOCAL, PRESET
     * @param mode    one of the breakpoint modes defined in
     *                the interface <code>Breakpoint</code>.
     *                The mode INSCRIPTION is not supported.
     *
     * @see #ADD
     * @see #REMOVE
     * @see #GLOBAL
     * @see #LOCAL
     * @see Breakpoint
     */
    public ToggleBreakpointCommand(String name, BreakpointManager manager,
                                   int op, int context, int mode) {
        super(name);
        this.manager = manager;
        // this.editor = editor;
        this.op = op;
        this.context = context;
        this.breakpointMode = mode;
    }


    /**
     * @return true, if at least one figure is selected and
     *               the drawing type matches the context
     *               mode (CPNDrawing for PRESET mode,
     *               CPNInstanceDrawing for all other modes).
     *               If exactly one figure is selected,
     *               also checks if the type of the figure
     *               matches the chosen breakpoint mode. <br>
     *         false, otherwise.
     * @see Breakpoint
     */
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        Drawing drawing = getEditor().drawing();
        DrawingView view = getEditor().view();

        if (view.selectionCount() <= 0) {
            return false;
        } else if (view.selectionCount() > 1) {
            if (context == PRESET) {
                return (drawing instanceof CPNDrawing);
            } else {
                return (drawing instanceof CPNInstanceDrawing)
                       && ((CPNInstanceDrawing) drawing).isLocal();
            }
        } else {
            Figure figure = view.selectionElements().nextElement();

            if (context == PRESET) {
                if (figure instanceof TransitionFigure) {
                    return BreakpointManager.isValidTransitionMode(breakpointMode);
                } else if (figure instanceof PlaceFigure) {
                    return BreakpointManager.isValidPlaceMode(breakpointMode);
                }
            } else if ((drawing instanceof CPNInstanceDrawing)
                               && ((CPNInstanceDrawing) drawing).isLocal()) {
                if (figure instanceof TransitionInstanceFigure) {
                    return BreakpointManager.isValidTransitionMode(breakpointMode);
                } else if ((figure instanceof PlaceInstanceFigure)
                                   || (figure instanceof TokenBagFigure)) {
                    return BreakpointManager.isValidPlaceMode(breakpointMode);
                }
            }
            return false;
        }
    }

    public boolean executeUndoable() {
        DrawingView view = getEditor().view();
        FigureEnumeration figures;
        Figure figure;
        int count = 0;
        Breakpoint bp;
        Vector<PlaceInstanceAccessor> memory = new Vector<PlaceInstanceAccessor>();

        if (isExecutable()) {
            if (!super.isExecutable()) {
                return false;
            }
            figures = view.selectionElements();
            while (figures.hasMoreElements()) {
                figure = figures.nextFigure();
                if (context == PRESET) {
                    if (figure instanceof TransitionFigure) {
                        switch (op) {
                        case ADD:
                            if (BreakpointManager.isValidTransitionMode(breakpointMode)) {
                                figure.setAttribute(Breakpoint.ATTRIBUTENAME,
                                                    new Integer(breakpointMode));
                                count++;
                            }
                            break;
                        case REMOVE:
                            if (figure.getAttribute(Breakpoint.ATTRIBUTENAME) != null) {
                                figure.setAttribute(Breakpoint.ATTRIBUTENAME,
                                                    null);
                                count++;
                            }
                            break;
                        }
                    } else if (figure instanceof PlaceFigure) {
                        switch (op) {
                        case ADD:
                            if (BreakpointManager.isValidPlaceMode(breakpointMode)) {
                                figure.setAttribute(Breakpoint.ATTRIBUTENAME,
                                                    new Integer(breakpointMode));
                                count++;
                            }
                            break;
                        case REMOVE:
                            if (figure.getAttribute(Breakpoint.ATTRIBUTENAME) != null) {
                                figure.setAttribute(Breakpoint.ATTRIBUTENAME,
                                                    null);
                                count++;
                            }
                            break;
                        }
                    }
                } else {
                    if (figure instanceof TransitionInstanceFigure) {
                        TransitionInstanceAccessor transitionInstAccessor = ((TransitionInstanceFigure) figure)
                                                                            .getInstance();
                        if (!(transitionInstAccessor instanceof TransitionInstanceAccessorImpl)) {
                            throw new IllegalStateException("Only the local simulation may set breakpoints");
                        }
                        TransitionInstance transitionInst = ((TransitionInstanceAccessorImpl) transitionInstAccessor)
                                                            .getTransitionInstance();
                        Transition transition = transitionInst.getTransition();
                        Net net = transitionInst.getNetInstance().getNet();

                        switch (op) {
                        case ADD:
                            if (context == GLOBAL) {
                                bp = manager.createTransitionBreakpoint(transition,
                                                                        breakpointMode,
                                                                        net);
                            } else {
                                bp = manager.createTransitionInstanceBreakpoint(transitionInst,
                                                                                breakpointMode);
                            }
                            if (bp != null) {
                                count++;
                            }
                            break;
                        case REMOVE:
                            if (context == GLOBAL) {
                                count += manager.deleteBreakpointsAt(transition);
                            } else {
                                count += manager.deleteBreakpointsAt(transitionInst);
                            }
                            break;
                        }
                    } else {
                        PlaceInstanceAccessor placeInstAccessor = null;

                        if (figure instanceof PlaceInstanceFigure) {
                            placeInstAccessor = ((PlaceInstanceFigure) figure)
                                                    .getInstance();
                        } else if (figure instanceof TokenBagFigure) {
                            placeInstAccessor = ((TokenBagFigure) figure)
                                                    .getPlaceInstance();


                            // Check if we already met this place instance
                            // while executing this command.
                            // It is possible to meet a place instance twice
                            // because its token bag figure may be selected
                            // in addition to its place instance figure.
                        }
                        if (memory.contains(placeInstAccessor)) {
                            placeInstAccessor = null;

                        }
                        if (placeInstAccessor != null) {
                            if (!(placeInstAccessor instanceof PlaceInstanceAccessorImpl)) {
                                throw new IllegalStateException("Only the local simulation may set breakpoints");
                            }
                            PlaceInstance placeInst = ((PlaceInstanceAccessorImpl) placeInstAccessor)
                                                      .getPlaceInstance();


                            // augment our memory
                            memory.addElement(placeInstAccessor);

                            Place place = placeInst.getPlace();
                            Net net = placeInst.getNetInstance().getNet();

                            switch (op) {
                            case ADD:
                                if (context == GLOBAL) {
                                    bp = manager.createPlaceBreakpoint(place,
                                                                       breakpointMode,
                                                                       net);
                                } else {
                                    bp = manager.createPlaceInstanceBreakpoint(placeInst,
                                                                               breakpointMode);
                                }
                                if (bp != null) {
                                    count++;
                                }
                                break;
                            case REMOVE:
                                if (context == GLOBAL) {
                                    count += manager.deleteBreakpointsAt(place);
                                } else {
                                    count += manager.deleteBreakpointsAt(placeInst);
                                }
                                break;
                            }
                        }
                    }
                }
            }
            view.checkDamage();
            StringBuffer message = new StringBuffer();

            if (count == 1) {
                message.append("1 breakpoint");
            } else {
                message.append(count + " breakpoints");
            }
            switch (op) {
            case ADD:
                message.append(" set.");
                break;
            case REMOVE:
                message.append(" cleared.");
                break;
            }
            getEditor().showStatus(message.toString());
            if ((context == PRESET) && (count > 0)) {
                return true;
            }
        }
        return false;
    }
}