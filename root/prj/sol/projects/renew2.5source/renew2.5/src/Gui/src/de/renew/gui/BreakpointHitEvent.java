/*
 * Created on 22.10.2004
 *
 */
package de.renew.gui;



/**
 * The BreakpointHitEvent is used to inform possible BreakpointHitListeners
 * about the hit of a breakpoint.
 *
 * @author Sven Offermann
 */
public class BreakpointHitEvent {
    public boolean consumed = false;
    public Breakpoint bp;

    public BreakpointHitEvent(Breakpoint bp) {
        this.bp = bp;
    }

    /**
     * @return Returns if the event was consumed.
     */
    public boolean isConsumed() {
        return consumed;
    }

    /**
     * Sets the event state to consumed.
     */
    public void consume() {
        this.consumed = true;
    }

    /**
     * @return Returns the breakpoint of this event.
     */
    public Breakpoint getBp() {
        return bp;
    }
}