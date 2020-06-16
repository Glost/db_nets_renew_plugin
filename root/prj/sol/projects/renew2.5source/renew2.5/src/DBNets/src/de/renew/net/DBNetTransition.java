package de.renew.net;

import de.renew.dbnets.datalogic.ActionCall;

public class DBNetTransition extends Transition {

    private ActionCall actionCall;

    public DBNetTransition(DBNetControlLayer net, String name, NetElementID id) {
        super(net, name, id);
    }

    public ActionCall getActionCall() {
        return actionCall;
    }

    public void setActionCall(ActionCall actionCall) {
        this.actionCall = actionCall;
    }

    //    public void performAction() {
//        action.performAction();
//    }
//
//    public void rollbackAction() {
//        action.rollbackAction();
//    }
}
