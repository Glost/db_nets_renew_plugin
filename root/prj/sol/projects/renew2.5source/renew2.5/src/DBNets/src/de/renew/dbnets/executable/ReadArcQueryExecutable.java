package de.renew.dbnets.executable;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.LateExecutable;
import de.renew.net.ViewPlaceInstance;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

public class ReadArcQueryExecutable implements LateExecutable {

    private final ViewPlaceInstance placeInstance;

    private final Variable tokenVariable;

    private final StateRecorder stateRecorder;

    public ReadArcQueryExecutable(ViewPlaceInstance placeInstance,
                                  Variable tokenVariable,
                                  StateRecorder stateRecorder) {
        this.placeInstance = placeInstance;
        this.tokenVariable = tokenVariable;
        this.stateRecorder = stateRecorder;
    }

    @Override
    public int phase() {
        return ACTION;
    }

    @Override
    public boolean isLong() {
        return true;
    }

    @Override
    public void execute(StepIdentifier stepIdentifier) throws Impossible {
        Object queryResult = placeInstance.executeQuery();

        Unify.unify(tokenVariable, queryResult, stateRecorder);
    }

    @Override
    public void executeAfterException(StepIdentifier stepIdentifier, Throwable t) {
    }
}
