package de.renew.dbnets.persistence;

/**
 * The db-net's persistence layer's JDBC connection data.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class JdbcConnection {

    /**
     * The db-net's persistence layer's JDBC connection url.
     */
    private final String url;

    /**
     * The db-net's persistence layer's JDBC connection data's constructor.
     *
     * @param url The db-net's persistence layer's JDBC connection url.
     */
    public JdbcConnection(String url) {
        this.url = url;
    }

    /**
     * Returns the db-net's persistence layer's JDBC connection url.
     *
     * @return The db-net's persistence layer's JDBC connection url.
     */
    public String getUrl() {
        return url;
    }
}
