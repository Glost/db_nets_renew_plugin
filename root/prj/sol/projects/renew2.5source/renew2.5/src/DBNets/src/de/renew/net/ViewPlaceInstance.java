package de.renew.net;

import de.renew.dbnets.datalogic.Query;
import de.renew.unify.Impossible;
import org.apache.log4j.Logger;

public class ViewPlaceInstance extends MultisetPlaceInstance {

    private static final Logger logger = Logger.getLogger(ViewPlaceInstance.class);

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
        logger.info("Executing query...");
        // TODO: ...
        return 42; // TODO: implement.
    }
}
