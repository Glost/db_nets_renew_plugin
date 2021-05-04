package de.renew.net;

import de.renew.dbnets.datalogic.ActionCall;
import de.renew.dbnets.pa.PerformanceAnalysisInfo;

/**
 * The db-net's transition.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class DBNetTransition extends Transition {

    /**
     * The db-net's transition's action call (the usage of the declared action
     * for modifying the persistence layer's data).
     */
    private ActionCall actionCall;

    /**
     * The metadata for performance analysis of modeled system on the current transition.
     */
    private PerformanceAnalysisInfo performanceAnalysisInfo;

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

    /**
     * Returns the metadata for performance analysis of modeled system on the current transition.
     *
     * @return The metadata for performance analysis of modeled system on the current transition.
     */
    public PerformanceAnalysisInfo getPerformanceAnalysisInfo() {
        return performanceAnalysisInfo;
    }

    /**
     * Sets the metadata for performance analysis of modeled system on the current transition.
     *
     * @param performanceAnalysisInfo The metadata for performance analysis of modeled system on the current transition.
     */
    public void setPerformanceAnalysisInfo(PerformanceAnalysisInfo performanceAnalysisInfo) {
        this.performanceAnalysisInfo = performanceAnalysisInfo;
    }
}
