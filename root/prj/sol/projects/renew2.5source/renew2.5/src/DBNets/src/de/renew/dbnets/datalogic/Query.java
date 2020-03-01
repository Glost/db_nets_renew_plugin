package de.renew.dbnets.datalogic;

public class Query {

    private final String name;

    private final String queryString;

    public Query(String name, String queryString) {
        this.name = name;
        this.queryString = queryString;
    }

    public String getName() {
        return name;
    }

    public String getQueryString() {
        return queryString;
    }
}
