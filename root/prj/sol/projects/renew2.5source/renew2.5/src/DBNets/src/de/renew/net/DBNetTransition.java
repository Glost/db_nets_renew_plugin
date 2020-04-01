package de.renew.net;

import de.renew.dbnets.datalogic.Action;

public class DBNetTransition extends Transition {

    private final Action action;

    public DBNetTransition(DBNetControlLayer net, String name, NetElementID id, Action action) {
        super(net, name, id);
        this.action = action;
    }

    public void performAction() {
        action.performAction();
    }

    public void rollbackAction() {
        action.rollbackAction();
    }
}
