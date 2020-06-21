package de.renew.dbnets.datalogic;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;
import de.renew.expression.LocalVariable;
import de.renew.expression.VariableMapper;
import de.renew.net.NetInstance;
import de.renew.net.TransitionInscription;
import de.renew.unify.Impossible;
import de.renew.unify.Variable;
import de.renew.util.Value;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The action call - the db-net's data logic layer's action's usage in the concrete db-net's transition.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class ActionCall implements TransitionInscription {

    /**
     * The action used in the transition.
     */
    private final Action action;

    /**
     * The values and/or replacements for the action's params.
     */
    private final List<Object> params;

    /**
     * The action call's constructor.
     *
     * @param action The action used in the transition.
     * @param params The values and/or replacements for the action's params.
     */
    public ActionCall(Action action, List<Object> params) {
        this.action = action;
        this.params = params;
    }

    /**
     * Returns the action used in the transition.
     *
     * @return The action used in the transition.
     */
    public Action getAction() {
        return action;
    }

    /**
     * The values and/or replacements for the action's params.
     *
     * @return Returns the values and/or replacements for the action's params.
     */
    public List<Object> getParams() {
        return Collections.unmodifiableList(params);
    }

    /**
     * Returns the empty list of the action call's occurrences since we do not need any of them.
     *
     * @param mapper The transition instance's variable mapper.
     *               Maps the net's variables' names into their values.
     * @param netInstance The db-net's control layer's instance.
     * @param searcher The searcher instance.
     * @return The empty list of the action call's occurrences.
     */
    @Override
    public Collection<Occurrence> makeOccurrences(VariableMapper mapper, NetInstance netInstance, Searcher searcher) {
        return Collections.emptySet();
    }

    /**
     * Performs the action.
     *
     * @param variableMapper The transition instance's variable mapper.
     *                       Maps the net's variables' names into their values.
     * @param connection The database connection instance.
     * @throws Impossible If the database or other error occurred during the action performing.
     */
    public void performAction(VariableMapper variableMapper, Connection connection) throws Impossible {
        Map<String, Object> paramsValuesMap = IntStream.range(0, action.getParams().size()).boxed()
                .collect(Collectors.toMap(action.getParams()::get, params::get));

        List<Optional<PreparedStatementWithParams>> preparedStatementsOptionals = Stream.concat(
                action.getDeletedFacts().stream()
                        .map(deletedFact -> mapDeletedFactToPreparedStatement(deletedFact, connection)),
                action.getAddedFacts().stream()
                        .map(addedFact -> mapAddedFactToPreparedStatement(addedFact, connection))
        ).collect(Collectors.toList());

        if (preparedStatementsOptionals.stream().anyMatch(optional -> !optional.isPresent())) {
            tryRollback(connection, new Impossible("Some of prepared statements were impossible to be formed"));
        }

        List<PreparedStatementWithParams> preparedStatements = preparedStatementsOptionals.stream()
                .map(Optional::get)
                .collect(Collectors.toList());

        for (PreparedStatementWithParams preparedStatement : preparedStatements) {
            try {
                executePreparedStatement(preparedStatement, paramsValuesMap, variableMapper);
            } catch (SQLException e) {
                tryRollback(connection, e);
            }
        }

        try {
            connection.commit();
        } catch (SQLException e) {
            tryRollback(connection, e);
        }
    }

    /**
     * Converts the added fact into the prepared statement (with params) based on the insert SQL/DML query.
     *
     * @param addedFact The added fact - the row of the table being added to the database during the action performing.
     * @param connection The database connection instance.
     * @return The optional instance of the prepared statement (with params) for the added fact.
     */
    private Optional<PreparedStatementWithParams> mapAddedFactToPreparedStatement(EditedFact addedFact,
                                                                                  Connection connection) {
        List<String> columnsNames = new ArrayList<>(addedFact.getColumnsToParams().keySet());
        String insertSql = getAddedFactInsertSql(addedFact, columnsNames);
        List<Object> params = columnsNames.stream()
                .map(columnName -> addedFact.getColumnsToParams().get(columnName))
                .collect(Collectors.toList());
        return mapEditActionSqlToPreparedStatement(insertSql, params, connection);
    }

    /**
     * Returns the parameterized insert SQL/DML query for the added fact.
     *
     * @param addedFact The added fact - the row of the table being added to the database during the action performing.
     * @param columnsNames The affected (by the added fact) table columns' names.
     * @return The parameterized insert SQL/DML query for the added fact.
     */
    private String getAddedFactInsertSql(EditedFact addedFact, List<String> columnsNames) {
        String insertSqlColumnsNames = "(" + String.join(", ", columnsNames) + ")";

        return  "INSERT INTO " + addedFact.getRelationName() + " " + insertSqlColumnsNames +
                " VALUES (" + String.join(", ", Collections.nCopies(columnsNames.size(), "?")) + ");";
    }

    /**
     * Converts the deleted fact into the prepared statement (with params) based on the insert SQL/DML query.
     *
     * @param deletedFact The deleted fact - the row of the table being added
     *                    to the database during the action performing.
     * @param connection The database connection instance.
     * @return The optional instance of the prepared statement (with params) for the deleted fact.
     */
    private Optional<PreparedStatementWithParams> mapDeletedFactToPreparedStatement(EditedFact deletedFact,
                                                                                    Connection connection) {
        List<String> columnsNames = new ArrayList<>(deletedFact.getColumnsToParams().keySet());
        String deleteSql = getDeletedFactDeleteSql(deletedFact, columnsNames);
        List<Object> params = columnsNames.stream()
                .map(columnName -> deletedFact.getColumnsToParams().get(columnName))
                .collect(Collectors.toList());
        return mapEditActionSqlToPreparedStatement(deleteSql, params, connection);
    }

    /**
     * Returns the parameterized insert SQL/DML query for the deleted fact.
     *
     * @param deletedFact The deleted fact - the row of the table being added
     *                    to the database during the action performing.
     * @param columnsNames The affected (by the added fact) table columns' names.
     * @return The parameterized insert SQL/DML query for the added fact.
     */
    private String getDeletedFactDeleteSql(EditedFact deletedFact, List<String> columnsNames) {
        String deleteSqlWhereCondition = "WHERE " + columnsNames.stream()
                .map(columnName -> columnName + " = ?")
                .collect(Collectors.joining(" AND "));

        return "DELETE FROM " + deletedFact.getRelationName() + " " + deleteSqlWhereCondition + ";";
    }

    /**
     * Returns the prepared statement (with params) based on the SQL/DML query for the edited (added/deleted) fact
     * or the empty optional instance if it is impossible to form the prepared statement.
     *
     * @param sql The SQL/DML query for the edited (added/deleted) fact.
     * @param params The params for the fact editing (adding/deleting).
     * @param connection The database connection instance.
     * @return The prepared statement (with params) based on the SQL/DML query for the edited (added/deleted) fact
     * or the empty optional instance if it is impossible to form the prepared statement.
     */
    private Optional<PreparedStatementWithParams> mapEditActionSqlToPreparedStatement(String sql,
                                                                                      List<Object> params,
                                                                                      Connection connection) {
        try {
            return Optional.ofNullable(connection.prepareStatement(sql))
                    .map(preparedStatement -> new PreparedStatementWithParams(preparedStatement, params));
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    /**
     * Executes the edited (added/deleted) fact's prepared statement.
     *
     * @param preparedStatement The edited (added/deleted) fact's prepared statement.
     * @param paramsValuesMap The map of the values for the edited (added/deleted) facts' params.
     * @param variableMapper The transition instance's variable mapper.
     *                       Maps the net's variables' names into their values.
     * @throws SQLException If the database error occurred during the prepared statement execution.
     * @throws Impossible If the param(-s) has(have) invalid type or null.
     */
    private void executePreparedStatement(PreparedStatementWithParams preparedStatement,
                                          Map<String, Object> paramsValuesMap,
                                          VariableMapper variableMapper) throws SQLException, Impossible {
        int i = 1;

        for (Object param : preparedStatement.params) {
            Variable paramValue;

            if (param instanceof Variable) {
                paramValue = (Variable) param;
            } else if (param instanceof String) {
                Object paramValueObject = paramsValuesMap.get(param);

                if (paramValueObject instanceof Variable) {
                    paramValue = (Variable) paramValueObject;
                } else if (paramValueObject instanceof String) {
                    paramValue = variableMapper.map(new LocalVariable((String) paramValueObject));

                    if (!paramValue.isBound() || !paramValue.isComplete()) {
                        paramValue = variableMapper.map(new LocalVariable((String) param));

                        if (!paramValue.isBound() || !paramValue.isComplete()) {
                            throw new Impossible("The param value " + paramValue + " for the param \"" + param +
                                    "\" should be bound and complete");
                        }
                    }
                } else {
                    throw new Impossible("The param value for the param \"" + param +
                            "\" should have type of String or Variable and be not null");
                }
            } else {
                throw new Impossible("The param should have type of String or Variable and be not null");
            }
            setPreparedStatementColumnValue(preparedStatement.preparedStatement, paramValue, i++);
        }

        preparedStatement.preparedStatement.executeUpdate();
    }

    /**
     * Sets the value for the given prepared statement's column.
     *
     * @param preparedStatement The prepared statement.
     * @param columnValue The value for setting.
     * @param i The column's number.
     * @throws SQLException If the database error occurred during setting the value.
     */
    private void setPreparedStatementColumnValue(PreparedStatement preparedStatement, Variable columnValue, int i)
            throws SQLException {
        Object value = columnValue.getValue();

        if (value instanceof Value) {
            value = ((Value) value).value;
        }

        if (Objects.isNull(value)) {
            preparedStatement.setNull(i, Types.NULL);
        } else if (value instanceof Integer) {
            preparedStatement.setInt(i, (int) value);
        } else if (value instanceof Long) {
            preparedStatement.setLong(i, (long) value);
        } else if (value instanceof Float) {
            preparedStatement.setFloat(i, (float) value);
        } else if (value instanceof Double) {
            preparedStatement.setDouble(i, (double) value);
        } else if (value instanceof Boolean) {
            preparedStatement.setBoolean(i, (boolean) value);
        } else if (value instanceof String) {
            preparedStatement.setString(i, (String) value);
        } else {
            preparedStatement.setObject(i, value);
        }
    }

    /**
     * Tries to rollback the last database failed transaction.
     *
     * @param connection The database connection instance.
     * @param t The throwable which is the reason for rollback.
     * @throws Impossible If the database error occurred during the transaction rollback.
     */
    private void tryRollback(Connection connection, Throwable t) throws Impossible {
        try {
            connection.rollback();

            throw new Impossible("The error occurred during performing the action and it is rollbacked: " +
                    t.getMessage(), t);
        } catch (SQLException e) {
            throw new Impossible("The database error occurred during performing the rollback: " + e.getMessage() +
                    " after the error during performing the action: " + t.getMessage(), e);
        }
    }

    /**
     * The edited (added/deleted) fact's prepared statement together with the edited fact's params.
     */
    private static class PreparedStatementWithParams {

        /**
         * The edited (added/deleted) fact's prepared statement.
         */
        final PreparedStatement preparedStatement;

        /**
         * The edited (added/deleted) fact's params.
         */
        final List<Object> params;

        /**
         * The constructor.
         *
         * @param preparedStatement The edited (added/deleted) fact's prepared statement.
         * @param params The edited (added/deleted) fact's params.
         */
        PreparedStatementWithParams(PreparedStatement preparedStatement, List<Object> params) {
            this.preparedStatement = preparedStatement;
            this.params = params;
        }
    }
}
