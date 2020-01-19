package de.renew.database;

import de.renew.net.NetInstance;


public class NetAction {
    private NetInstance instance;

    NetAction(NetInstance instance) {
        this.instance = instance;
    }

    public NetInstance getNetInstance() {
        return instance;
    }

    public String getName() {
        return instance.getNet().getName();
    }

    public String getNetID() {
        return instance.getID();
    }
}