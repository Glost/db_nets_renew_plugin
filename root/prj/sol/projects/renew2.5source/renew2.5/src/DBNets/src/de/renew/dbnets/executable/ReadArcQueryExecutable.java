package de.renew.dbnets.executable;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.EarlyExecutable;
import de.renew.net.ViewPlaceInstance;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

public class ReadArcQueryExecutable implements EarlyExecutable {

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
        return INPUT;
    }

    @Override
    public void verify(StepIdentifier stepIdentifier) throws Impossible {
    }

    //    @Override
//    public boolean isLong() {
//        return true;
//    }

    @Override
    public void execute(StepIdentifier stepIdentifier) {
        Object queryResult = placeInstance.executeQuery();

        try {
            Unify.unify(tokenVariable, queryResult, stateRecorder);
        } catch (Impossible e) {
            throw new RuntimeException(); // TODO: ...
        }
    }

    @Override
    public void rollback() {
    }

    @Override
    public long lockPriority() {
        return placeInstance.lockOrder;
    }

    @Override
    public void lock() {
        placeInstance.lock.lock();
    }

    @Override
    public void unlock() {
        placeInstance.lock.unlock();
    }
}
