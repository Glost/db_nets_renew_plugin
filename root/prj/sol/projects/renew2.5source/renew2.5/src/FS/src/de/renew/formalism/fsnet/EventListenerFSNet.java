package de.renew.formalism.fsnet;

import de.renew.call.SynchronisationRequest;

import de.renew.net.NetInstance;

import de.renew.unify.Tuple;
import de.renew.unify.Variable;

import java.awt.AWTEvent;


/**
 * Class needed for an BDI example using FSNets by Frank Wienberg (in his dissertation models directory).
 */
public abstract class EventListenerFSNet {
    private NetInstance myInstance = null;
    Object myComponent = null;

    public EventListenerFSNet() {
    }

    public EventListenerFSNet(NetInstance instance) {
        myInstance = instance;
    }

    public void eventOccured(final AWTEvent event) {
        new Thread() {
                public void run() {
                    Variable var = new Variable();
                    SynchronisationRequest.synchronize(myInstance, "s",
                                                       new Tuple(new Object[] { event, var, var },
                                                                 null));
                }
            }.start();
    }

    public NetInstance getListener() {
        return myInstance;
    }

    public void setListener(NetInstance instance) {
        myInstance = instance;
    }

    public Object getComponent() {
        return myComponent;
    }
}