package de.renew.net.event;

import de.renew.engine.simulator.SimulationThreadPool;


public abstract class NetEvent // extends java.util.EventObject
        implements java.io.Serializable {

    /** This is a class for general net events. We do not use or inherit
     *  from java.util.EventObject, since it contains a memory leak bug
     *  in JDK 1.1.
     */


    /** The event source object. */
    protected final Object source;

    /** Constructs a new NetEvent with the given event source. */
    protected NetEvent(Object source) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.source = source;
    }

    /** Returns the event source object. */
    public Object getSource() {
        return source;
    }
}