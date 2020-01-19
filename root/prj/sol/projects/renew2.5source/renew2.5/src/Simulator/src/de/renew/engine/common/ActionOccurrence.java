package de.renew.engine.common;

import de.renew.engine.searcher.AbstractOccurrence;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.OccurrenceDescription;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.net.TransitionInstance;

import de.renew.unify.Impossible;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;


public class ActionOccurrence extends AbstractOccurrence {
    Expression expression;
    VariableMapper mapper;

    public ActionOccurrence(Expression expression, VariableMapper mapper,
                            TransitionInstance tInstance) {
        super(tInstance);
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.expression = expression;
        this.mapper = mapper;
    }

    public Collection<Binder> makeBinders(Searcher searcher)
            throws Impossible {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        expression.registerCalculation(mapper, searcher.recorder,
                                       searcher.calcChecker);
        // That's it. No binders required.
        return Collections.emptySet();
    }

    public Collection<Executable> makeExecutables(VariableMapperCopier copier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Collection<Executable> coll = new Vector<Executable>();
        coll.add(new ActionExecutable(expression, copier.makeCopy(mapper)));
        return coll;
    }

    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier variableMapperCopier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return null;
    }
}