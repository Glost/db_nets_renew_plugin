package de.renew.net;

import de.renew.dbnets.binder.ActionCallValuesBinder;
import de.renew.dbnets.datalogic.ActionCall;
import de.renew.dbnets.datalogic.ActionCallExecutable;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;
import de.renew.expression.VariableMapper;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Variable;

import java.sql.Connection;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DBNetTransitionOccurrence extends TransitionOccurrence {

    private final StateRecorder stateRecorder;

    public DBNetTransitionOccurrence(TransitionInstance transitionInstance,
                                     Variable params,
                                     Searcher searcher) throws Impossible {
        super(transitionInstance, params, searcher);
        stateRecorder = searcher.recorder;
    }

    @Override
    public Collection<Binder> makeBinders(Searcher searcher) throws Impossible {
        ActionCall actionCall = ((DBNetTransition) getTransition().getTransition()).getActionCall();
        Connection connection = ((DBNetControlLayerInstance) getTransition().getNetInstance()).getConnection();

        return Stream.concat(
                super.makeBinders(searcher).stream(),
                Stream.of(new ActionCallValuesBinder(
                        actionCall,
                        (DBNetTransitionInstance) getTransition(),
                        stateRecorder,
                        connection
                ))
        ).collect(Collectors.toList());
    }

    @Override
    public Collection<Executable> makeExecutables(VariableMapperCopier copier) {
        ActionCall actionCall = ((DBNetTransition) getTransition().getTransition()).getActionCall();

        if (Objects.isNull(actionCall)) {
            return super.makeExecutables(copier);
        }

        VariableMapper variableMapper = ((DBNetTransitionInstance) getTransition()).getVariableMapper();
        Connection connection = ((DBNetControlLayerInstance) getTransition().getNetInstance()).getConnection();

        return Stream.concat(
                super.makeExecutables(copier).stream(),
                Stream.of(
                        new ActionCallExecutable(actionCall, copier.makeCopy(variableMapper), connection)
                )
        ).collect(Collectors.toList());
    }
}
