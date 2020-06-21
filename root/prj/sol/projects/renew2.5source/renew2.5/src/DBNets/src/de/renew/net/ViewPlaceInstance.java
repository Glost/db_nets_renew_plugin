package de.renew.net;

import de.renew.unify.Impossible;

/**
 * The db-net's view place's instance.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class ViewPlaceInstance extends MultisetPlaceInstance {

    /**
     * The db-net's view place's instance's constructor.
     *
     * @param netInstance The db-net's control layer's instance.
     * @param place The db-net's view place.
     * @param wantInitialTokens Not used, always true.
     * @throws Impossible If the error occurred during the db-net's view place's instance creation.
     */
    public ViewPlaceInstance(DBNetControlLayerInstance netInstance,
                             ViewPlace place,
                             boolean wantInitialTokens) throws Impossible {
        super(netInstance, place, wantInitialTokens);
    }

    /**
     * Returns the db-net's view place corresponding to this instance.
     *
     * @return The db-net's view place corresponding to this instance.
     */
    @Override
    public ViewPlace getPlace() {
        return (ViewPlace) super.getPlace();
    }
}
