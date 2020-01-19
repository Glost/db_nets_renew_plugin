/*
 * Created on Nov 22, 2004
 *
 */
package de.renew.engine.events;

import de.renew.net.NetInstance;


/**
 * @author Sven Offermann
 *
 */
public class NetInstantiation extends NetEvent {
    public NetInstantiation(NetInstance net) {
        super(net);
    }

    public String toString() {
        return "New net instance " + getNetInstance().toString() + " created.";
    }
}