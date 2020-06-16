package de.renew.net;

import de.renew.dbnets.datalogic.ActionCall;
import de.renew.dbnets.datalogic.ActionCallExecutable;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;
import de.renew.unify.Impossible;
import de.renew.unify.Variable;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DBNetTransitionOccurence extends TransitionOccurrence {

    public DBNetTransitionOccurence(TransitionInstance transitionInstance,
                                    Variable params,
                                    Searcher searcher) throws Impossible {
        super(transitionInstance, params, searcher);
    }

    @Override
    public Collection<Executable> makeExecutables(VariableMapperCopier copier) {
        ActionCall actionCall = ((DBNetTransition) getTransition().getTransition()).getActionCall();

        return Stream.concat(super.makeExecutables(copier).stream(), Stream.of(new ActionCallExecutable(actionCall)))
                .collect(Collectors.toList());
    }
}
