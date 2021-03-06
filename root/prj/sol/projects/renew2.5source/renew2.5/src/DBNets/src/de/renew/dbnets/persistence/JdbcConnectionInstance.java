package de.renew.dbnets.persistence;

import de.renew.dbnets.datalogic.EditedFact;
import de.renew.expression.VariableMapper;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Variable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The database connection instance.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public interface JdbcConnectionInstance {

    /**
     * Initializes the database connection instance by the given JDBC URL
     * and creates the database schema using the given DDL query.
     *
     * @param jdbcUrl The JDBC URL for initializing the database connection.
     * @param ddlQueryString The DDL query for creating the database schema.
     * @throws Impossible If the database error occurred during the database connection initialization
     * or the database schema creation.
     */
    void init(String jdbcUrl, String ddlQueryString) throws Impossible;

    /**
     * Executes the query.
     *
     * @param sqlQueryString The SQL query string.
     * @param stateRecorder The state recorder instance.
     * @return The query results list.
     * @throws Impossible If the database error occurred during the query execution.
     */
    List<Variable> executeQuery(String sqlQueryString,
                                StateRecorder stateRecorder) throws Impossible;

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
    void performAction(Collection<EditedFact> addedFacts,
                       Collection<EditedFact> deletedFacts,
                       Map<String, Object> paramsValuesMap,
                       VariableMapper variableMapper) throws Impossible;

    /**
     * Gets the generated value for the param.
     *
     * @param tableName The param usage's table (relation) name.
     * @param stateRecorder The state recorder instance.
     * @return The generated value for the param.
     * @throws SQLException If the database error occurred during the value generating.
     */
    Variable mapParamToAutoincrementedValue(String tableName, StateRecorder stateRecorder) throws SQLException;

    /**
     * Closes the database connection.
     *
     * @throws SQLException If the database error occurred during the database connection closing.
     */
    void close() throws SQLException;
}
