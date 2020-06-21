package de.renew.dbnets.persistence;

/**
 * The db-net's persistence layer's JDBC connection data.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
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
