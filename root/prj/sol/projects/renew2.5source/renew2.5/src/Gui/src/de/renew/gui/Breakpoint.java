package de.renew.gui;

import de.renew.net.Net;


/**
 * Breakpoints observe the simulation of a net and stop
 * its execution if a certain event occurs.
 * An overview of different observation techniques is
 * given in the <code>BreakpointManager</code>.
 * <p>
 * This interface has to be implemented by all breakpoints
 * manageable by the <code>BreakpointManager</code>.
 * It also supplies some constants needed by most breakpoints.
 * </p>
 * Breakpoint.java
 * Created: Mon Jun 05  2000
 * @author Michael Duvigneau
 * @see BreakpointManager
 */
public interface Breakpoint {

    /**
     * The name of the attribute at place or transition figures
     * in which the information concerning breakpoints is stored.
     * <p>
     * Figures marked in this way will get an attached breakpoint
     * when the next simulation starts.
     * The attribute contains an <code>Integer</code> object
     * representing the mode of the breakpoint to create.
     * Created breakpoints will always be global, e.g. hittable
     * at all instances of the corresponding net element.
     * </p>
     **/
    static final String ATTRIBUTENAME = "BreakpointMode";

    /**
     * Breakpoint mode:
     * Default mode, depends on the observed net element.
     *
     * The default mode is
     *   FIRE for transition instances,
     *   MARKINGCHANGE for place instances,
     *   ...
     * @see #FIRE
     * @see #MARKINGCHANGE
     **/
    static final int DEFAULT = 0;

    /**
     * Breakpoint mode:
     * Stops execution if a transition instance starts firing.
     * @see TransitionInstanceBreakpoint
     **/
    static final int FIRE = 1;

    /**
     * Breakpoint mode:
     * Stops execution if a transition instance completes firing.
     * @see TransitionInstanceBreakpoint
     **/
    static final int FIRECOMPLETE = 2;

    /**
     * Breakpoint mode:
     * Stops execution if a place instance's marking changes,
     * e.g. one or more tokens are added, removed or tested.
     * <p>
     * Testing and untesting tokens via test arcs is considered
     * as a marking change in this mode. If you want to ignore
     * test arcs, use <code>MARKINGCHANGENOTEST</code>.
     * </p>
     *
     * @see PlaceInstanceBreakpoint
     * @see #MARKINGCHANGENOTEST
     **/
    static final int MARKINGCHANGE = 3;

    /**
     * Breakpoint mode (deprecated):
     * Stops execution if any instance of a transition fires.
     * @see GlobalTransitionBreakpoint
     *
     * @deprecated
     * This mode is no longer in use, as the mode <code>FIRE</code>
     * offers the same functionality when breakpoints with this
     * mode are attached to transitions instead of transition
     * instances.
     * @see #FIRE
     **/
    static final int INSCRIPTION = 4;

    /**
     * Breakpoint mode:
     * Stops execution if a place instance's marking changes,
     * e.g. one or more tokens are added or removed.
     * <p>
     * Testing and untesting tokens via test arcs is ignored
     * in this mode. If you want to pay attention to such
     * changes, too, use <code>MARKINGCHANGE</code>.
     * </p>
     *
     * @see PlaceInstanceBreakpoint
     * @see #MARKINGCHANGE
     **/
    static final int MARKINGCHANGENOTEST = 5;

    /**
     * Breakpoint mode:
     * Stops execution if a place instance's marking changes
     * by adding exactly one token.
     *
     * @see PlaceInstanceBreakpoint
     **/
    static final int TOKENADDED = 6;

    /**
     * Breakpoint mode:
     * Stops execution if a place instance's marking changes
     * by removing exactly one token.
     *
     * @see PlaceInstanceBreakpoint
     **/
    static final int TOKENREMOVED = 7;

    /**
     * Breakpoint mode:
     * Stops execution if a place instance's marking changes
     * the test status of one or more tokens.
     *
     * @see PlaceInstanceBreakpoint
     **/
    static final int TOKENTESTCHANGE = 8;

    /**
     * Returns the transition, place or the place or transition
     * instance which is observed by the Breakpoint.
     * <p>
     * If a static transition or place is returned, all its
     * instances are observed.
     * The instance which was most recently hit will be
     * returned via <code>getHitElement()</code>.
     * To get the net the transition or place belongs to,
     * use <code>getTaggedNet()</code>.
     * </p>
     * @see #getHitElement
     * @see #getTaggedNet
     **/
    Object getTaggedElement();

    /**
     * Returns the transition or place instance at which the
     * breakpoint was most recently hit.
     * Returns <code>null</code> if this breakpoint was never
     * hit since its creation.
     * <p>
     * To get the net element the breakpoint was originally
     * attached to, use <code>getTaggedElement()</code>.
     * </p>
     * @see #getTaggedElement
     **/
    Object getHitElement();

    /**
     * Returns the net to which the observed place or
     * transition belongs.
     * <p>
     * This method is needed to be able to show the tagged
     * element because places and transitions do not know
     * their net by themselves.
     * </p>
     **/
    Net getTaggedNet();

    /**
     * Releases all resources, unregisters from any event
     * producers and undoes all modifications to the net
     * eventually done at the breakpoint's creation.
     **/
    void release();
}