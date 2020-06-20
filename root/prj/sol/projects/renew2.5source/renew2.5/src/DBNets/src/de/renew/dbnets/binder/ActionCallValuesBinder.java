package de.renew.dbnets.binder;

import de.renew.dbnets.datalogic.ActionCall;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.BindingBadness;
import de.renew.engine.searcher.Searcher;
import de.renew.expression.LocalVariable;
import de.renew.expression.VariableMapper;
import de.renew.net.DBNetTransitionInstance;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ActionCallValuesBinder implements Binder {

    private static final String DBN_AUTOINCREMENT_REGEX = "dbn_autoincrement_(?<tableName>\\w+)";

    private static final Pattern DBN_AUTOINCREMENT_PATTERN = Pattern.compile(DBN_AUTOINCREMENT_REGEX);

    private static final Random RANDOM = new Random();

    private final ActionCall actionCall;

    private final DBNetTransitionInstance transitionInstance;

    private final VariableMapper variableMapper;

    private final StateRecorder stateRecorder;

    private final Connection connection;

    private boolean isBound = false;

    public ActionCallValuesBinder(ActionCall actionCall,
                                  DBNetTransitionInstance transitionInstance,
                                  VariableMapper variableMapper,
                                  StateRecorder stateRecorder,
                                  Connection connection) {
        this.actionCall = actionCall;
        this.transitionInstance = transitionInstance;
        this.variableMapper = variableMapper;
        this.stateRecorder = stateRecorder;
        this.connection = connection;
    }

    @Override
    public int bindingBadness(Searcher searcher) {
        return isBound ? BindingBadness.max : 1;
    }

    @Override
    public void bind(Searcher searcher) {
//        transitionInstance.lock();

        transitionInstance.acquire();

        if (isBound) {
            searcher.search();

            return;
        }

        if (Objects.isNull(actionCall)) {
            isBound = true;
//            transitionInstance.setBound(true);

            searcher.search();

            return;
        }

        Map<String, Object> paramsNamesToParams = IntStream.range(0, actionCall.getAction().getParams().size()).boxed()
                .collect(Collectors.toMap(actionCall.getAction().getParams()::get, actionCall.getParams()::get));

        for (Map.Entry<String, Object> entry : paramsNamesToParams.entrySet()) {
            try {
                mapParam(entry.getKey(), entry.getValue());
            } catch (Impossible | SQLException e) {
                throw new RuntimeException(); // TODO: ...
            }
        }

        isBound = true;
//        transitionInstance.setBound(true);

        searcher.search();
    }

    private void mapParam(String paramName, Object param) throws Impossible, SQLException {
        if (param instanceof Variable) {
            Variable variable = variableMapper.map(new LocalVariable(paramName));
            Unify.unify(variable, (Variable) param, stateRecorder);
            return;
        }

        String paramString = (String) param;

        Variable randomValue = mapParamToRandomValue(paramString);

        if (randomValue.isComplete() && randomValue.isBound()) {
            Variable variable = variableMapper.map(new LocalVariable(paramName));
            Unify.unify(variable, randomValue, stateRecorder);
            return;
        }

        Matcher dbnAutoincrementMatcher = DBN_AUTOINCREMENT_PATTERN.matcher(paramString);

        if (dbnAutoincrementMatcher.matches()) {
            Variable generatedValue = mapParamToAutoincrementedValue(dbnAutoincrementMatcher.group("tableName"));
            Variable variable = variableMapper.map(new LocalVariable(paramName));
            Unify.unify(variable, generatedValue, stateRecorder);
        }
    }

    private Variable mapParamToRandomValue(String paramString) {
        switch (paramString) {
            case "dbn_rand_int": return new Variable(RANDOM.nextInt(), stateRecorder);
            case "dbn_rand_long": return new Variable(RANDOM.nextLong(), stateRecorder);
            case "dbn_rand_float": return new Variable(RANDOM.nextFloat(), stateRecorder);
            case "dbn_rand_double": return new Variable(RANDOM.nextDouble(), stateRecorder);
            case "dbn_rand_boolean": return new Variable(RANDOM.nextBoolean(), stateRecorder);
            case "dbn_rand_string": return new Variable(UUID.randomUUID().toString(), stateRecorder);
            default: return new Variable();
        }
    }

    private Variable mapParamToAutoincrementedValue(String tableName) throws SQLException {
        String sql = "SELECT seq FROM sqlite_sequence WHERE name = ?;";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, tableName);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.isClosed()) {
            String insertSql = "INSERT INTO sqlite_sequence (name, seq) VALUES (?, 2)";

            PreparedStatement insertPreparedStatement = connection.prepareStatement(insertSql);
            insertPreparedStatement.setString(1, tableName);

            return new Variable(1, stateRecorder);
        }

        int seq = resultSet.getInt(1);

        String updateSql = "UPDATE sqlite_sequence SET seq = ? WHERE name = ?;";

        PreparedStatement updatePreparedStatement = connection.prepareStatement(updateSql);
        updatePreparedStatement.setInt(1, seq + 1);
        updatePreparedStatement.setString(2, tableName);

        updatePreparedStatement.executeUpdate();

        return new Variable(seq, stateRecorder);
    }
}
