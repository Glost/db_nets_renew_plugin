package de.renew.dbnets.persistence;

/**
 * The db-net's persistence layer's relational database schema declaration.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class DatabaseSchemaDeclaration {

    /**
     * The SQL/DDL query string declaring the db-net's persistence layer's relational database schema.
     */
    private final String ddlQueryString;

    /**
     * The db-net's persistence layer's relational database schema declaration's constructor.
     *
     * @param ddlQueryString The SQL/DDL query string declaring the db-net's persistence layer's
     *                       relational database schema.
     */
    public DatabaseSchemaDeclaration(String ddlQueryString) {
        this.ddlQueryString = ddlQueryString;
    }

    /**
     * Returns the SQL/DDL query string declaring the db-net's persistence layer's relational database schema.
     *
     * @return The SQL/DDL query string declaring the db-net's persistence layer's relational database schema.
     */
    public String getDdlQueryString() {
        return ddlQueryString;
    }
}
