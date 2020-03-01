package de.renew.net;

import de.renew.dbnets.datalogic.Query;

public class ViewPlace extends Place {

    private final Query query;

    public ViewPlace(DBNetControlLayer net, String name, NetElementID id, Query query) {
        super(net, name, id);
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }
}
