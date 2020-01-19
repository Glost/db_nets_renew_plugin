package de.renew.call;

import de.renew.net.Net;
import de.renew.net.NetInstanceImpl;
import de.renew.net.NetNotFoundException;

import de.renew.unify.Impossible;


public class OwnedNetInstance extends NetInstanceImpl {
    public final Object owner;

    public OwnedNetInstance(String netName, Object owner)
            throws Impossible, NetNotFoundException {
        super(Net.forName(netName));
        this.owner = owner;
    }
}