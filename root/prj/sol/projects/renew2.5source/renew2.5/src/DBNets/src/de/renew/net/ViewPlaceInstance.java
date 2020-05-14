package de.renew.net;

import de.renew.dbnets.datalogic.Query;
import de.renew.unify.Impossible;

public class ViewPlaceInstance extends MultisetPlaceInstance {

    public ViewPlaceInstance(DBNetControlLayerInstance netInstance,
                             ViewPlace place,
                             boolean wantInitialTokens) throws Impossible {
        super(netInstance, place, wantInitialTokens);
    }

    @Override
    public ViewPlace getPlace() {
        return (ViewPlace) super.getPlace();
    }

    public Object executeQuery() {
        Query query = ((ViewPlace) place).getQuery();
        // TODO: ...
        return 42; // TODO: implement.
    }
}
