/*
 * Created on 22.10.2004
 *
 */
package de.renew.gui;



/**
 * Describes the listener interface that must be implemented
 * by objects which wants to be informed, if a breakboint
 * is hit in a simulation.
 *
 * @author Sven Offermann
 *
 */
public interface BreakpointHitListener {
    public void hitBreakpoint(BreakpointHitEvent event);
}