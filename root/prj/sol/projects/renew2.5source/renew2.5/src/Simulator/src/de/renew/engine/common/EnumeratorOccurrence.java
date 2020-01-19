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
import de.renew.unify.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;


public abstract class EnumeratorOccurrence extends AbstractOccurrence {
    private Expression expression;
    private VariableMapper mapper;
    private boolean checkBound;

    public EnumeratorOccurrence(Expression expression, boolean checkBound,
                                VariableMapper mapper,
                                TransitionInstance tInstance) {
        super(tInstance);
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.expression = expression;
        this.checkBound = checkBound;
        this.mapper = mapper;
    }

    public abstract Collection<?extends Object> getCandidates(Object pattern);

    public Collection<Binder> makeBinders(Searcher searcher)
            throws Impossible {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Variable var = new Variable(expression.startEvaluation(mapper,
                                                               searcher.recorder,
                                                               searcher.calcChecker),
                                    searcher.recorder);
        Collection<Binder> coll = new Vector<Binder>();
        coll.add(new EnumeratorBinder(this, checkBound, var));
        return coll;
    }

    public Collection<Executable> makeExecutables(VariableMapperCopier copier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return Collections.emptySet();
    }

    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier variableMapperCopier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return null;
    }
}