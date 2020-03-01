package de.renew.net;

import de.renew.unify.Impossible;

public class ViewPlaceInstance extends MultisetPlaceInstance {

    public ViewPlaceInstance(DBNetControlLayerInstance netInstance,
                             ViewPlace place,
                             boolean wantInitialTokens) throws Impossible {
        super(netInstance, place, wantInitialTokens);
    }
}
