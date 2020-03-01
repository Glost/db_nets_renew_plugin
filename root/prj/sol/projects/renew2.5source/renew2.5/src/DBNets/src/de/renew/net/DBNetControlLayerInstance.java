package de.renew.net;

import de.renew.unify.Impossible;

public class DBNetControlLayerInstance extends NetInstanceImpl {

    public DBNetControlLayerInstance() {
    }

    public DBNetControlLayerInstance(DBNetControlLayer net) throws Impossible {
        super(net);
    }

    public DBNetControlLayerInstance(DBNetControlLayer net, boolean wantInitialTokens) throws Impossible {
        super(net, wantInitialTokens);
    }
}
