package de.renew.dbnets.persistence;

import de.renew.dbnets.datalogic.EditedFact;
import de.renew.expression.VariableMapper;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Variable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The database connection instance based on the Wireshark PCAP file with the captured FIX Protocol messages.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class FixPcapJdbcConnectionInstance implements JdbcConnectionInstance {

    /**
     * The regular expression for the FIX PCAP JDBC URL.
     */
    private static final String JDBC_URL_REGEX = "virtual:fixpcap:(?<fixPcapFilePath>.+):supp:(?<sqliteJdbcUrl>.+)";

    /**
     * The pattern for the regular expression for the FIX PCAP JDBC URL.
     */
    private static final Pattern JDBC_URL_PATTERN = Pattern.compile(JDBC_URL_REGEX);

    /**
     * The regular expression for the FIX tag-value pair.
     */
    private static final String FIX_TAG_VALUE_REGEX = "(?<tag>\\d+)=(?<value>\\d)";

    /**
     * The pattern for the regular expression for the FIX tag-value pair.
     */
    private static final Pattern FIX_TAG_VALUE_PATTERN = Pattern.compile(FIX_TAG_VALUE_REGEX);

    /**
     * The delimiter of the FIX tag-value pairs.
     */
    private static final char FIX_TAGS_DELIMITER = '\1';

    /**
     * The number of the FIX message type (MsgType) tag.
     */
    private static final String FIX_MSG_TYPE_TAG = "35";

    /**
     * The JDBC instance of the SQLite connection.
     */
    private Connection connection;

    /**
     * The stream of lines of the FIX PCAP file.
     */
    private Stream<String> fixPcapFileLinesStream;

    /**
     * The iterator of the stream of lines of the FIX PCAP file.
     */
    private Iterator<String> fixPcapFileLinesStreamIterator;

    /**
     * Initializes the database connection instance by the given JDBC URL
     * and creates the database schema using the given DDL query.
     *
     * @param jdbcUrl The JDBC URL for initializing the database connection.
     * @param ddlQueryString The DDL query for creating the database schema.
     * @throws Impossible If the database error occurred during the database connection initialization
     * or the database schema creation.
     */
    @Override
    public void init(String jdbcUrl, String ddlQueryString) throws Impossible {
        Matcher matcher = JDBC_URL_PATTERN.matcher(jdbcUrl);

        if (!matcher.matches()) {
            throw new Impossible("JDBC URL for the FIX PCAP virtual JDBC connection is unparseable: " + jdbcUrl);
        }

        String fixPcapFilePath = matcher.group("fixPcapFilePath");
        String sqliteJdbcUrl = matcher.group("sqliteJdbcUrl");

        try {
            fixPcapFileLinesStream = Files.lines(Paths.get(fixPcapFilePath));
            fixPcapFileLinesStreamIterator = fixPcapFileLinesStream.iterator();
        } catch (IOException e) {
            throw new Impossible("Cannot open file (" + fixPcapFilePath + "): " + e.getMessage(), e);
        }

        createDatabaseConnection(sqliteJdbcUrl);
    }

    /**
     * Executes the query.
     *
     * @param sqlQueryString The SQL query string.
     * @param stateRecorder The state recorder instance.
     * @return The query results list.
     * @throws Impossible If the database error occurred during the query execution.
     */
    @Override
    public List<Variable> executeQuery(String sqlQueryString, StateRecorder stateRecorder) throws Impossible {
        // TODO: Implement the method.
        return null;
    }

    /**
     * Performs the action.
     *
     * @param addedFacts The rows of the tables being added to the database during the action performing.
     * @param deletedFacts The rows of the tables being deleted to the database during the action performing.
     * @param paramsValuesMap The map of the values for the edited (added/deleted) facts' params.
     * @param variableMapper The transition instance's variable mapper.
     *                       Maps the net's variables' names into their values.
     * @throws Impossible If the database or other error occurred during the action performing.
     */
    @Override
    public void performAction(Collection<EditedFact> addedFacts,
                              Collection<EditedFact> deletedFacts,
                              Map<String, Object> paramsValuesMap,
                              VariableMapper variableMapper) throws Impossible {
        // TODO: Implement the method.
    }

    /**
     * Gets the generated value for the param.
     *
     * @param tableName The param usage's table (relation) name.
     * @return The generated value for the param.
     * @throws SQLException If the database error occurred during the value generating.
     */
    @Override
    public Variable mapParamToAutoincrementedValue(String tableName, StateRecorder stateRecorder) throws SQLException {
        // TODO: Implement the method.
        return null;
    }

    /**
     * Closes the FIX PCAP file stream and the SQLite database connection.
     *
     * @throws SQLException If the database error occurred during the SQLite database connection closing.
     */
    @Override
    public void close() throws SQLException {
        if (Objects.nonNull(connection)) {
            connection.close();
        }

        if (Objects.nonNull(fixPcapFileLinesStream)) {
            fixPcapFileLinesStream.close();
        }
    }

    /**
     * Closes the FIX PCAP file stream and the SQLite database connection.
     *
     * @throws Throwable If any error occurred.
     */
    @Override
    protected void finalize() throws Throwable {
        close();
    }

    /**
     * Create the SQLite JDBC connection.
     *
     * @param jdbcUrl The JDBC URL of the SQLite database.
     * @throws Impossible If the SQL error occurred during the SQLite database connection creation.
     */
    private void createDatabaseConnection(String jdbcUrl) throws Impossible {
        try {
            connection = DriverManager.getConnection(jdbcUrl);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new Impossible("Error while connecting to the database: " + e.getMessage(), e);
        }
    }
}
