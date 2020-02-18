package de.renew.dbnets.controllayer;

import de.renew.net.NetElementID;
import de.renew.net.Transition;

public class DBNetTransition extends Transition {

    public DBNetTransition(DBNetControlLayer net, String name, NetElementID id) {
        super(net, name, id);
    }
}
