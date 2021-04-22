package de.renew.dbnets.binder;

import de.renew.dbnets.datalogic.QueryCall;
import de.renew.dbnets.persistence.JdbcConnectionInstance;
import de.renew.engine.searcher.BindingBadness;
import de.renew.engine.searcher.Searcher;
import de.renew.expression.VariableMapper;
import de.renew.net.PlaceInstance;
import de.renew.net.ViewPlaceInstance;
import de.renew.net.arc.InputArcBinder;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Tuple;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import java.util.List;

/**
 * The db-net's read arc's binder.
 * Maps the read arc's variables to the retrieved (from the db-net's persistence layer) values
 * through the corresponding view place's query call.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class ReadArcBinder extends InputArcBinder {

    /**
     * The read arc's token inscription variable.
     */
    private final Variable tokenVariable;

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
     * @param tokenVariable The read arc's token inscription variable.
     * @param delayVariable The read arc's delay variable.
     * @param placeInstance The instance of the view place which is one of the ends of the read arc.
     * @param variableMapper The transition instance's variable mapper.
     *                       Maps the net's variables' names into their values.
     * @param stateRecorder The state recorder instance.
     * @param connectionInstance The database connection instance.
     */
    public ReadArcBinder(Variable tokenVariable,
                         Variable delayVariable,
                         PlaceInstance placeInstance,
                         VariableMapper variableMapper,
                         StateRecorder stateRecorder,
                         JdbcConnectionInstance connectionInstance) {
        super(tokenVariable, delayVariable, placeInstance);
        this.tokenVariable = tokenVariable;
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
        QueryCall queryCall = ((ViewPlaceInstance) getPlaceInstance()).getPlace().getQueryCall();

        return isBound || !queryCall.checkBoundedness(variableMapper) ? BindingBadness.max : BindingBadness.max - 1;
    }

    /**
     * Maps the read arc's variables to the retrieved (from the db-net's persistence layer) values
     * through the corresponding view place's query call.
     *
     * @param searcher The searcher instance.
     */
    @Override
    public void bind(Searcher searcher) {
        if (isBound) {
            searcher.search();

            return;
        }

        QueryCall queryCall = ((ViewPlaceInstance) getPlaceInstance()).getPlace().getQueryCall();

        try {
            List<Variable> queryResult = queryCall.executeQuery(connectionInstance, variableMapper, stateRecorder);
            if (tokenVariable.getValue() instanceof Tuple) {
                Tuple queryResultTuple = new Tuple(queryResult.toArray(), stateRecorder);
                Unify.unify(tokenVariable, queryResultTuple, stateRecorder);
            } else if (!queryResult.isEmpty()) {
                Unify.unify(tokenVariable, queryResult.get(0), stateRecorder);
            }

            isBound = true;
        } catch (Impossible e) {
            e.printStackTrace();
        }

        searcher.search();
    }
}
