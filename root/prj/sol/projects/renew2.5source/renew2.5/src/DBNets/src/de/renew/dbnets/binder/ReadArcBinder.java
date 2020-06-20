package de.renew.dbnets.binder;

import de.renew.dbnets.datalogic.QueryCall;
import de.renew.engine.searcher.BindingBadness;
import de.renew.engine.searcher.Searcher;
import de.renew.expression.VariableMapper;
import de.renew.net.DBNetTransitionInstance;
import de.renew.net.PlaceInstance;
import de.renew.net.TokenReserver;
import de.renew.net.ViewPlaceInstance;
import de.renew.net.arc.InputArcBinder;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Tuple;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import java.sql.Connection;
import java.util.List;

public class ReadArcBinder extends InputArcBinder {

    private final Variable tokenVariable;

    private final DBNetTransitionInstance transitionInstance;

    private final VariableMapper variableMapper;

    private final StateRecorder stateRecorder;

    private final Connection connection;

    private boolean isBound = false;

    public ReadArcBinder(Variable tokenVariable,
                         Variable delayVariable,
                         PlaceInstance placeInstance,
                         DBNetTransitionInstance transitionInstance,
                         VariableMapper variableMapper,
                         StateRecorder stateRecorder,
                         Connection connection) {
        super(tokenVariable, delayVariable, placeInstance);
        this.tokenVariable = tokenVariable;
        this.transitionInstance = transitionInstance;
        this.variableMapper = variableMapper;
        this.stateRecorder = stateRecorder;
        this.connection = connection;
    }

    @Override
    public int bindingBadness(Searcher searcher) {
        QueryCall queryCall = ((ViewPlaceInstance) getPlaceInstance()).getPlace().getQueryCall();

        return isBound || !queryCall.checkBoundness(variableMapper) ? BindingBadness.max : BindingBadness.max - 1;
    }

    @Override
    protected boolean mayBind() {
        return true;
    }

    @Override
    protected boolean possible(TokenReserver reserver, Object token) {
        return true;
    }

    @Override
    protected boolean remove(TokenReserver reserver, Object token) {
        return true;
    }

    @Override
    public void bind(Searcher searcher) {
//        transitionInstance.lock();

        if (isBound) {
            searcher.search();

            return;
        }

        QueryCall queryCall = ((ViewPlaceInstance) getPlaceInstance()).getPlace().getQueryCall();

        try {
            List<Variable> queryResult = queryCall.executeQuery(connection, variableMapper, stateRecorder);
            if (tokenVariable.getValue() instanceof Tuple) {
                Tuple queryResultTuple = new Tuple(queryResult.toArray(), stateRecorder);
                Unify.unify(tokenVariable, queryResultTuple, stateRecorder);
            } else if (!queryResult.isEmpty()) {
                Unify.unify(tokenVariable, queryResult.get(0), stateRecorder);
            }

            isBound = true;
        } catch (Impossible e) {
            e.printStackTrace();
//            throw new RuntimeException(); // TODO: ...
        }

        searcher.search();
    }

    private void tryBind(Searcher searcher) {

    }
}
