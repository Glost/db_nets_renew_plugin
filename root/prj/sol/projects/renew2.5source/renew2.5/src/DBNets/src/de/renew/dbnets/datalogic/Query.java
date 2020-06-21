package de.renew.dbnets.datalogic;

/**
 * The query for retrieving the persistence layer's data.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
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
