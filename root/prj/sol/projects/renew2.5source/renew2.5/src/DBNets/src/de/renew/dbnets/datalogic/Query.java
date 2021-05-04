package de.renew.dbnets.datalogic;

/**
 * The db-net's data logic layer's query for retrieving the persistence layer's data.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class Query {

    /**
     * The query's name.
     */
    private final String name;

    /**
     * The SQL query string.
     */
    private final String queryString;

    /**
     * The query's constructor.
     *
     * @param name The query's name.
     * @param queryString The SQL query string.
     */
    public Query(String name, String queryString) {
        this.name = name;
        this.queryString = queryString;
    }

    /**
     * Returns the query's name.
     *
     * @return The query's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the SQL query string.
     *
     * @return The SQL query string.
     */
    public String getQueryString() {
        return queryString;
    }
}
