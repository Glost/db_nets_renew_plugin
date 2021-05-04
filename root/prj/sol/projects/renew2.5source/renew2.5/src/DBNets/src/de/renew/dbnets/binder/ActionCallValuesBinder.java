package de.renew.dbnets.binder;

import de.renew.dbnets.datalogic.ActionCall;
import de.renew.dbnets.persistence.JdbcConnectionInstance;
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

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Binder which maps the variables to the action call's generated values and literals.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class ActionCallValuesBinder implements Binder {

    /**
     * The regular expression of the generated values.
     */
    private static final String DBN_AUTOINCREMENT_REGEX = "dbn_autoincrement_(?<tableName>\\w+)";

    /**
     * The pattern for the regular expression of the generated values.
     */
    private static final Pattern DBN_AUTOINCREMENT_PATTERN = Pattern.compile(DBN_AUTOINCREMENT_REGEX);

    /**
     * The action call - the action's usage in the concrete db-net's transition.
     */
    private final ActionCall actionCall;

    /**
     * The db-net's transition's instance.
     */
    private final DBNetTransitionInstance transitionInstance;

    /**
     * The transition instance's variable mapper. Maps the net's variables' names into their values.
     */
    private final VariableMapper variableMapper;

    /**
     * The state recorder instance.
     */
    private final StateRecorder stateRecorder;

    /**
     * The database connection instance.
     */
    private final JdbcConnectionInstance connectionInstance;

    /**
     * Stores whether this binder was bound or not.
     */
    private boolean isBound = false;

    /**
     * The binder's constructor.
     *
     * @param actionCall The action call - the action's usage in the concrete db-net's transition.
     * @param transitionInstance The db-net's transition's instance.
     * @param variableMapper The transition instance's variable mapper.
     *                       Maps the net's variables' names into their values.
     * @param stateRecorder The state recorder instance.
     * @param connectionInstance The database connection instance.
     */
    public ActionCallValuesBinder(ActionCall actionCall,
                                  DBNetTransitionInstance transitionInstance,
                                  VariableMapper variableMapper,
                                  StateRecorder stateRecorder,
                                  JdbcConnectionInstance connectionInstance) {
        this.actionCall = actionCall;
        this.transitionInstance = transitionInstance;
        this.variableMapper = variableMapper;
        this.stateRecorder = stateRecorder;
        this.connectionInstance = connectionInstance;
    }

    /**
     * Returns how possible the binder can be bound.
     * The higher badness - the worse. The binders with the max badness cannot be bound.
     *
     * @param searcher The searcher instance.
     * @return How possible the binder can be bound.
     * The higher badness - the worse. The binders with the max badness cannot be bound.
     */
    @Override
    public int bindingBadness(Searcher searcher) {
        return isBound ? BindingBadness.max : 1;
    }

    /**
     * Tries to bind the action call's generated values and literals.
     *
     * @param searcher The searcher instance.
     */
    @Override
    public void bind(Searcher searcher) {
        transitionInstance.acquire();

        if (isBound) {
            searcher.search();

            return;
        }

        if (Objects.isNull(actionCall)) {
            isBound = true;

            searcher.search();

            return;
        }

        Map<String, Object> paramsNamesToParams = IntStream.range(0, actionCall.getAction().getParams().size()).boxed()
                .collect(Collectors.toMap(actionCall.getAction().getParams()::get, actionCall.getParams()::get));

        for (Map.Entry<String, Object> entry : paramsNamesToParams.entrySet()) {
            try {
                mapParam(entry.getKey(), entry.getValue());
            } catch (Impossible | SQLException e) {
                throw new RuntimeException("The error occurred during binding the action call's " +
                        "generated value or literal: " + e.getMessage(), e);
            }
        }

        isBound = true;

        searcher.search();
    }

    /**
     * Maps the action's param's name to its action call's generated value or literal, if there is any.
     *
     * @param paramName The action's param's name.
     * @param param The corresponding action call's generated value or literal.
     * @throws Impossible If the unification error occurred during the param's mapping.
     * @throws SQLException If the database error occurred during the param's mapping.
     */
    private void mapParam(String paramName, Object param) throws Impossible, SQLException {
        if (param instanceof Variable) {
            Variable variable = variableMapper.map(new LocalVariable(paramName));
            Unify.unify(variable, param, stateRecorder);
            return;
        }

        String paramString = (String) param;

        Matcher dbnAutoincrementMatcher = DBN_AUTOINCREMENT_PATTERN.matcher(paramString);

        if (dbnAutoincrementMatcher.matches()) {
            Variable generatedValue = connectionInstance.mapParamToAutoincrementedValue(
                dbnAutoincrementMatcher.group("tableName"),
                stateRecorder
            );
            Variable variable = variableMapper.map(new LocalVariable(paramName));
            Unify.unify(variable, generatedValue, stateRecorder);
        }
    }
}
