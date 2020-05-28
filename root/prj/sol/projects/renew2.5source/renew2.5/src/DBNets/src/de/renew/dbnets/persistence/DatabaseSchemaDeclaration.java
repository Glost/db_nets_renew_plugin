package de.renew.dbnets.persistence;

public class DatabaseSchemaDeclaration {

    private final String ddlQueryString;

    public DatabaseSchemaDeclaration(String ddlQueryString) {
        this.ddlQueryString = ddlQueryString;
    }

    public String getDdlQueryString() {
        return ddlQueryString;
    }
}
