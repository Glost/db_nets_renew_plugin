package de.renew.dbnets.datalogic;

import de.renew.dbnets.persistence.JdbcConnectionInstance;
import de.renew.expression.LocalVariable;
import de.renew.expression.VariableMapper;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Variable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The query call - the db-net's data logic layer's query's usage in the concrete db-net's view place.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class QueryCall {

    /**
     * The regular expression for substituting the params in the query.
     */
    private static final String PLACEHOLDER_REGEX = "\\$\\{(?<variableName>\\w+)\\}";

    /**
     * The pattern for the regular expression for substituting the params in the query.
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REGEX);

    /**
     * The query used in the view place.
     */
    private final Query query;

    /**
     * The query call's constructor.
     *
     * @param query The query used in the view place.
     */
    public QueryCall(Query query) {
        this.query = query;
    }

    /**
     * Executes the query.
     *
     * @param connectionInstance The database connection instance.
     * @param variableMapper The transition instance's variable mapper.
     *                       Maps the net's variables' names into their values.
     * @param stateRecorder The state recorder instance.
     * @return The query results list.
     * @throws Impossible If the database error occurred during the query execution.
     */
    public List<Variable> executeQuery(JdbcConnectionInstance connectionInstance,
                                       VariableMapper variableMapper,
                                       StateRecorder stateRecorder) throws Impossible {
        String sqlQueryString = formSqlQueryString(variableMapper);
        return connectionInstance.executeQuery(sqlQueryString, stateRecorder);
    }

    /**
     * Checks whether the query's params which need to be substituted with the values are bound or not
     * (whether they values are known by the variable mapper or not).
     *
     * @param variableMapper The transition instance's variable mapper.
     *                       Maps the net's variables' names into their values.
     * @return The true if the query's params are bound (if they values are known by the variable mapper),
     * false otherwise.
     */
    public boolean checkBoundedness(VariableMapper variableMapper) {
        String sqlQueryString = query.getQueryString();

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(sqlQueryString);

        while (matcher.find()) {
            String variableName = matcher.group("variableName");
            Variable variable = variableMapper.map(new LocalVariable(variableName));

            if (!variable.isBound() || !variable.isComplete()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Forms the SQL query string with substituted query params.
     *
     * @param variableMapper The transition instance's variable mapper.
     *                       Maps the net's variables' names into their values.
     * @return The SQL query string with substituted query params.
     */
    private String formSqlQueryString(VariableMapper variableMapper) {
        String sqlQueryString = query.getQueryString();

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(sqlQueryString);

        StringBuffer stringBuffer = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group("variableName");
            Variable variable = variableMapper.map(new LocalVariable(variableName));

            matcher.appendReplacement(stringBuffer, String.valueOf(variable.getValue()));
        }

        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }
}
