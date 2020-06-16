package de.renew.dbnets.datalogic;

import de.renew.unify.Variable;

public class QueryCall {

    private final Query query;

    public QueryCall(Query query) {
        this.query = query;
    }

    public Variable executeQuery() {
//        query.getQueryString()
        // TODO: ...
        return new Variable(42, null);
    }
}
