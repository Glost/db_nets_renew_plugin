package de.renew.dbnets.datalogic;

import de.renew.expression.LocalVariable;
import de.renew.expression.VariableMapper;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Variable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryCall {

    private static final String PLACEHOLDER_REGEX = "\\$\\{(?<variableName>\\w+)\\}";

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REGEX);

    private final Query query;

    public QueryCall(Query query) {
        this.query = query;
    }

    public List<Variable> executeQuery(Connection connection,
                                       VariableMapper variableMapper,
                                       StateRecorder stateRecorder) throws Impossible {
        try {
            String sqlQueryString = formSqlQueryString(variableMapper);

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQueryString);
            int columnsCount = resultSet.getMetaData().getColumnCount();

            List<Variable> queryResult = new ArrayList<>(columnsCount);

            for (int i = 1; i <= columnsCount; i++) {
                queryResult.add(new Variable(resultSet.getObject(i), stateRecorder));
            }

            return queryResult;
        } catch (SQLException e) {
            throw new Impossible("Error while executing the query to the database: " + e.getMessage(), e);
        }
    }

    private String formSqlQueryString(VariableMapper variableMapper) {
        String sqlQueryString = query.getQueryString();

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(sqlQueryString);

        StringBuffer stringBuffer = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group("variableName");
            Variable variable = variableMapper.map(new LocalVariable(variableName)); // TODO: if unknown?..

            matcher.appendReplacement(stringBuffer, String.valueOf(variable.getValue()));
        }

        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }
}
