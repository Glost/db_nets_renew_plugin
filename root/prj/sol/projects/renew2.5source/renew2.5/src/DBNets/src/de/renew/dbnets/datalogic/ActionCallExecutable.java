package de.renew.dbnets.datalogic;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.LateExecutable;
import de.renew.expression.VariableMapper;
import de.renew.net.DBNetTransitionInstance;
import de.renew.unify.Impossible;

import java.sql.Connection;

public class ActionCallExecutable implements LateExecutable {

    private final ActionCall actionCall;

    private final DBNetTransitionInstance transitionInstance;

    private final VariableMapper variableMapper;

    private final Connection connection;

    public ActionCallExecutable(ActionCall actionCall,
                                DBNetTransitionInstance transitionInstance,
                                VariableMapper variableMapper,
                                Connection connection) {
        this.actionCall = actionCall;
        this.transitionInstance = transitionInstance;
        this.variableMapper = variableMapper;
        this.connection = connection;
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
        actionCall.performAction(variableMapper, connection);
//        transitionInstance.resetOccurence();
//        transitionInstance.setBound(false);
//        transitionInstance.unlock();
    }

    @Override
    public void executeAfterException(StepIdentifier stepIdentifier, Throwable t) {
        // TODO: mark for rollback.
    }
}
