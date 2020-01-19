package de.renew.net.arc;

import de.renew.engine.searcher.AbstractOccurrence;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.OccurrenceDescription;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;

import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.PlaceInstance;

import de.renew.unify.Impossible;
import de.renew.unify.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;


class ClearArcOccurrence extends AbstractOccurrence {
    PlaceInstance placeInstance;
    VariableMapper mapper;
    Variable variable;
    ClearArc arc;

    public ClearArcOccurrence(ClearArc arc, VariableMapper mapper,
                              NetInstance netInstance) {
        super(netInstance.getInstance(arc.transition));
        this.arc = arc;
        this.mapper = mapper;
        placeInstance = netInstance.getInstance(arc.place);
    }

    public Collection<Binder> makeBinders(Searcher searcher)
            throws Impossible {
        variable = new Variable(arc.expression.startEvaluation(mapper,
                                                               searcher.recorder,
                                                               searcher.calcChecker),
                                searcher.recorder);

        searcher.calcChecker.addCalculated(arc.expression.getType(), variable,
                                           null, searcher.recorder);
        return Collections.emptySet();
    }

    public Collection<Executable> makeExecutables(VariableMapperCopier variableMapperCopier) {
        Variable copiedVariable = (Variable) variableMapperCopier.getCopier()
                                                                 .copy(variable);
        Collection<Executable> coll = new Vector<Executable>();
        coll.add(new ClearArcExecutable(placeInstance, getTransition(),
                                        copiedVariable, arc));
        return coll;
    }

    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier variableMapperCopier) {
        return null;
    }
}