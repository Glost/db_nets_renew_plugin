package de.renew.dbnets.datalogic;

import de.renew.unify.Variable;

public class QueryCall {

    private String queryName;

    private Query query;

    public QueryCall(String queryName) {
        this.queryName = queryName;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Variable executeQuery() {
//        query.getQueryString()
        // TODO: ...
        return null;
    }
}
