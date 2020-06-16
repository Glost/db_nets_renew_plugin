package de.renew.net;

import de.renew.dbnets.datalogic.QueryCall;
import de.renew.unify.Impossible;

public class ViewPlace extends Place {

    private QueryCall queryCall;

    public ViewPlace(DBNetControlLayer net, String name, NetElementID id) {
        super(net, name, id);
    }

    public QueryCall getQueryCall() {
        return queryCall;
    }

    public void setQueryCall(QueryCall queryCall) {
        this.queryCall = queryCall;
    }

    @Override
    PlaceInstance makeInstance(NetInstance netInstance, boolean wantInitialTokens) throws Impossible {
        if (!(netInstance instanceof DBNetControlLayerInstance)) {
            Impossible.THROW();
        }

        return new ViewPlaceInstance((DBNetControlLayerInstance) netInstance, this, wantInitialTokens);
    }
}
