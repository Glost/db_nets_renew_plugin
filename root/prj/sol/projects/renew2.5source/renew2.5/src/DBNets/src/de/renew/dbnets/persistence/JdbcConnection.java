package de.renew.dbnets.persistence;

public class JdbcConnection {

    private final String url;

    public String getUrl() {
        return url;
    }

    public JdbcConnection(String url) {
        this.url = url;
    }
}
