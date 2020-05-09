package de.renew.net;

import de.renew.dbnets.datalogic.Query;
import de.renew.unify.Impossible;

public class ViewPlace extends Place {

    private final Query query;

    public ViewPlace(DBNetControlLayer net, String name, NetElementID id, Query query) {
        super(net, name, id);
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }

    @Override
    PlaceInstance makeInstance(NetInstance netInstance, boolean wantInitialTokens) throws Impossible {
        if (!(netInstance instanceof DBNetControlLayerInstance)) {
            Impossible.THROW();
        }

        return new ViewPlaceInstance((DBNetControlLayerInstance) netInstance, this, wantInitialTokens);
    }
}
