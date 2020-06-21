package de.renew.dbnets.persistence;

/**
 * The db-net's persistence layer's relational database schema declaration.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
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
