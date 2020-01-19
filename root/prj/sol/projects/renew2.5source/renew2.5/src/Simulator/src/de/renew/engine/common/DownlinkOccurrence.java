package de.renew.engine.common;

import de.renew.engine.searcher.AbstractOccurrence;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.ChannelBinder;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.OccurrenceDescription;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.net.TransitionInstance;

import de.renew.unify.Impossible;
import de.renew.unify.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;


public class DownlinkOccurrence extends AbstractOccurrence {
    private Expression params;
    private Expression callee;
    private String name;
    private boolean isOptional;
    private VariableMapper mapper;

    public DownlinkOccurrence(Expression params, Expression callee,
                              String name, boolean isOptional,
                              VariableMapper mapper,
                              TransitionInstance tInstance) {
        super(tInstance);
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.params = params;
        this.callee = callee;
        this.name = name;
        this.isOptional = isOptional;
        this.mapper = mapper;
    }

    public Collection<Binder> makeBinders(Searcher searcher)
            throws Impossible {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Variable calleeVariable = new Variable(callee.startEvaluation(mapper,
                                                                      searcher.recorder,
                                                                      searcher.calcChecker),
                                               searcher.recorder);
        Variable paramsVariable = new Variable(params.startEvaluation(mapper,
                                                                      searcher.recorder,
                                                                      searcher.calcChecker),
                                               searcher.recorder);
        Collection<Binder> coll = new Vector<Binder>();
        coll.add(new ChannelBinder(calleeVariable, name, paramsVariable,
                                   isOptional));
        return coll;
    }

    public Collection<Executable> makeExecutables(VariableMapperCopier copier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // A downlink does not cause any explicit action
        // during execution.
        return Collections.emptySet();
    }

    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier variableMapperCopier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return null;
    }
}