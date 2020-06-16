package de.renew.dbnets.binder;

import de.renew.dbnets.datalogic.QueryCall;
import de.renew.engine.searcher.BindingBadness;
import de.renew.engine.searcher.Searcher;
import de.renew.net.PlaceInstance;
import de.renew.net.TokenReserver;
import de.renew.net.ViewPlaceInstance;
import de.renew.net.arc.InputArcBinder;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

public class ReadArcBinder extends InputArcBinder {

    private final Variable tokenVariable;

    private final StateRecorder stateRecorder;

    private boolean isBound = false;

    public ReadArcBinder(Variable tokenVariable,
                         Variable delayVariable,
                         PlaceInstance placeInstance,
                         StateRecorder stateRecorder) {
        super(tokenVariable, delayVariable, placeInstance);
        this.tokenVariable = tokenVariable;
        this.stateRecorder = stateRecorder;
    }

    @Override
    public int bindingBadness(Searcher searcher) {
        return isBound ? BindingBadness.max : 1;
    }

    @Override
    protected boolean mayBind() {
        return !isBound;
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
        if (isBound) {
            return;
        }

        QueryCall queryCall = ((ViewPlaceInstance) getPlaceInstance()).getPlace().getQueryCall();

        Variable queryResult = queryCall.executeQuery();

        try {
            Unify.unify(tokenVariable, queryResult, stateRecorder);
        } catch (Impossible e) {
            throw new RuntimeException(); // TODO: ...
        }

        isBound = true;

        searcher.search();
    }
}
