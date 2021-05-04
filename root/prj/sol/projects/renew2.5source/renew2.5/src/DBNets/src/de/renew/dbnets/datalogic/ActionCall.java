package de.renew.dbnets.datalogic;

import de.renew.dbnets.persistence.JdbcConnectionInstance;
import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;
import de.renew.expression.VariableMapper;
import de.renew.net.NetInstance;
import de.renew.net.TransitionInscription;
import de.renew.unify.Impossible;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The action call - the db-net's data logic layer's action's usage in the concrete db-net's transition.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
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
     * @param connectionInstance The database connection instance.
     * @throws Impossible If the database or other error occurred during the action performing.
     */
    public void performAction(VariableMapper variableMapper,
                              JdbcConnectionInstance connectionInstance) throws Impossible {
        Map<String, Object> paramsValuesMap = IntStream.range(0, action.getParams().size()).boxed()
                .collect(Collectors.toMap(action.getParams()::get, params::get));

        connectionInstance.performAction(
            action.getAddedFacts(),
            action.getDeletedFacts(),
            paramsValuesMap,
            variableMapper
        );
    }
}
