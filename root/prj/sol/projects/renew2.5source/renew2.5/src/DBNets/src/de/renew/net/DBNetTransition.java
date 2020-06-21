package de.renew.net;

import de.renew.dbnets.datalogic.ActionCall;

/**
 * The db-net's transition.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class DBNetTransition extends Transition {

    /**
     * The db-net's transition's action call (the usage of the declared action
     * for modifying the persistence layer's data).
     */
    private ActionCall actionCall;

    /**
     * The db-net's transition's constructor.
     *
     * @param net The db-net's control layer.
     * @param name The db-net's transition's name.
     * @param id The db-net's transition's id.
     */
    public DBNetTransition(DBNetControlLayer net, String name, NetElementID id) {
        super(net, name, id);
    }

    /**
     * Returns the db-net's transition's action call (the usage of the declared action
     * for modifying the persistence layer's data).
     *
     * @return The db-net's transition's action call.
     */
    public ActionCall getActionCall() {
        return actionCall;
    }

    /**
     * Sets the db-net's transition's action call (the usage of the declared action
     * for modifying the persistence layer's data).
     *
     * @param actionCall The db-net's transition's action call.
     */
    public void setActionCall(ActionCall actionCall) {
        this.actionCall = actionCall;
    }
}
