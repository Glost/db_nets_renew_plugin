/*
 * Created on 07.12.2004
 *
 */
package de.renew.engine.events;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.Net;
import de.renew.net.NetInstance;


/**
 * @author Sven Offermann
 *
 */
public abstract class NetEvent implements SimulationEvent {
    private NetInstance net;

    public NetEvent(NetInstance net) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.net = net;
    }

    /**
      * @return Returns the net instance.
      */
    public NetInstance getNetInstance() {
        return this.net;
    }

    /**
     * @return Returns the net.
     */
    public Net getNet() {
        return this.net.getNet();
    }
}