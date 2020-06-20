package de.renew.dbnets.datalogic;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;
import de.renew.expression.LocalVariable;
import de.renew.expression.VariableMapper;
import de.renew.net.NetInstance;
import de.renew.net.TransitionInscription;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
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

public class ActionCall implements TransitionInscription {

    private final Action action;

    private final List<Object> params;

    public ActionCall(Action action, List<Object> params) {
        this.action = action;
        this.params = params;
    }

    public Action getAction() {
        return action;
    }

    public List<Object> getParams() {
        return Collections.unmodifiableList(params);
    }

    @Override
    public Collection<Occurrence> makeOccurrences(VariableMapper mapper, NetInstance netInstance, Searcher searcher) {
        // TODO: Check whether occurrences are necessary here or not.
        return Collections.emptySet();
    }

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
            // TODO: mark for rollback?..
        }

        List<PreparedStatementWithParams> preparedStatements = preparedStatementsOptionals.stream()
                .map(Optional::get)
                .collect(Collectors.toList());

        boolean isSuccess = true;

        for (PreparedStatementWithParams preparedStatement : preparedStatements) {
            try {
                executePreparedStatement(preparedStatement, paramsValuesMap, variableMapper);
            } catch (SQLException e) {
                isSuccess = false;
                break;
            }
        }

        if (isSuccess) {
            try {
                connection.commit();
            } catch (SQLException e) {
                isSuccess = false;
            }
        }

        if (!isSuccess) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                // TODO: ...
            }

            // TODO: mark for rollback?..
        }
    }

    private Optional<PreparedStatementWithParams> mapAddedFactToPreparedStatement(EditedFact addedFact,
                                                                                  Connection connection) {
        List<String> columnsNames = new ArrayList<>(addedFact.getColumnsToParams().keySet());
        String insertSql = getAddedFactInsertSql(addedFact, columnsNames);
        List<Object> params = columnsNames.stream()
                .map(columnName -> addedFact.getColumnsToParams().get(columnName))
                .collect(Collectors.toList());
        return mapEditActionSqlToPreparedStatement(insertSql, params, connection);
    }

    private String getAddedFactInsertSql(EditedFact addedFact, List<String> columnsNames) {
        String insertSqlColumnsNames = "(" + String.join(", ", columnsNames) + ")";

        return  "INSERT INTO " + addedFact.getRelationName() + " " + insertSqlColumnsNames +
                " VALUES (" + String.join(", ", Collections.nCopies(columnsNames.size(), "?")) + ");";
    }

    private Optional<PreparedStatementWithParams> mapDeletedFactToPreparedStatement(EditedFact deletedFact,
                                                                                    Connection connection) {
        List<String> columnsNames = new ArrayList<>(deletedFact.getColumnsToParams().keySet());
        String deleteSql = getDeletedFactDeleteSql(deletedFact, columnsNames);
        List<Object> params = columnsNames.stream()
                .map(columnName -> deletedFact.getColumnsToParams().get(columnName))
                .collect(Collectors.toList());
        return mapEditActionSqlToPreparedStatement(deleteSql, params, connection);
    }

    private String getDeletedFactDeleteSql(EditedFact deletedFact, List<String> columnsNames) {
        String deleteSqlWhereCondition = "WHERE " + columnsNames.stream()
                .map(columnName -> columnName + " = ?")
                .collect(Collectors.joining(" AND "));

        return "DELETE FROM " + deletedFact.getRelationName() + " " + deleteSqlWhereCondition + ";";
    }

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
                    }
                } else {
                    throw new Impossible(); // TODO: ...
                }
            } else {
                throw new Impossible(); // TODO: ...
            }
            setPreparedStatementColumnValue(preparedStatement.preparedStatement, paramValue, i++);
        }

        preparedStatement.preparedStatement.executeUpdate();
    }

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

    private static class PreparedStatementWithParams {

        final PreparedStatement preparedStatement;

        final List<Object> params;

        PreparedStatementWithParams(PreparedStatement preparedStatement, List<Object> params) {
            this.preparedStatement = preparedStatement;
            this.params = params;
        }
    }
}
