package de.renew.database;

import de.renew.net.NetInstance;

import java.util.Hashtable;


/**
 * This is a type safe wrapper around a hashtable.
 */
public class NetInstanceMap {
    private Hashtable<String, NetInstance> table = new Hashtable<String, NetInstance>();

    public void put(String netID, NetInstance instance) {
        table.put(netID, instance);
    }

    public NetInstance get(String netID) {
        return table.get(netID);
    }
}