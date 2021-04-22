package de.renew.net;

import de.renew.dbnets.datalogic.QueryCall;
import de.renew.dbnets.pa.ViewPlacePerformanceAnalysisInfo;
import de.renew.unify.Impossible;

/**
 * The db-net's view place.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class ViewPlace extends Place {

    /**
     * The db-net's view place's query call (the usage of the declared query
     * for retrieving the persistence layer's data).
     */
    private QueryCall queryCall;

    /**
     * The metadata for performance analysis of modeled system on the view place.
     */
    private ViewPlacePerformanceAnalysisInfo viewPlacePerformanceAnalysisInfo;

    /**
     * The db-net's view place's constructor.
     *
     * @param net The db-net's control layer.
     * @param name The db-net's view place's name.
     * @param id The db-net's view place's id.
     */
    public ViewPlace(DBNetControlLayer net, String name, NetElementID id) {
        super(net, name, id);
    }

    /**
     * Returns the db-net's view place's query call (the usage of the declared query
     * for retrieving the persistence layer's data).
     *
     * @return The db-net's view place's query call.
     */
    public QueryCall getQueryCall() {
        return queryCall;
    }

    /**
     * Sets the db-net's view place's query call (the usage of the declared query
     * for retrieving the persistence layer's data).
     *
     * @param queryCall The db-net's view place's query call.
     */
    public void setQueryCall(QueryCall queryCall) {
        this.queryCall = queryCall;
    }

    public ViewPlacePerformanceAnalysisInfo getViewPlacePerformanceAnalysisInfo() {
        return viewPlacePerformanceAnalysisInfo;
    }

    public void setViewPlacePerformanceAnalysisInfo(ViewPlacePerformanceAnalysisInfo viewPlacePerformanceAnalysisInfo) {
        this.viewPlacePerformanceAnalysisInfo = viewPlacePerformanceAnalysisInfo;
    }

    /**
     * Makes the db-net's view place's instance.
     *
     * @param netInstance The db-net's control layer's instance.
     * @param wantInitialTokens Not used, always true.
     * @return The db-net's view place's instance.
     * @throws Impossible If the error occurred during the db-net's view place's instance creation,
     */
    @Override
    PlaceInstance makeInstance(NetInstance netInstance, boolean wantInitialTokens) throws Impossible {
        if (!(netInstance instanceof DBNetControlLayerInstance)) {
            Impossible.THROW();
        }

        return new ViewPlaceInstance((DBNetControlLayerInstance) netInstance, this, wantInitialTokens);
    }
}
