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


class FlexibleArcOccurrence extends AbstractOccurrence {
    PlaceInstance placeInstance;
    VariableMapper mapper;
    Variable tokenVar;
    FlexibleArc arc;
    /**
     * This variable is assigned by the binder during the bind
     * procedure for input arcs. For output arcs, it has no meaning.
     */
    Vector<Object> inTokens;

    public FlexibleArcOccurrence(FlexibleArc arc, VariableMapper mapper,
                                 NetInstance netInstance) {
        super(netInstance.getInstance(arc.transition));
        this.arc = arc;
        this.mapper = mapper;
        placeInstance = netInstance.getInstance(arc.place);
    }

    public Collection<Binder> makeBinders(Searcher searcher)
            throws Impossible {
        tokenVar = new Variable(arc.expression.startEvaluation(mapper,
                                                               searcher.recorder,
                                                               searcher.calcChecker),
                                searcher.recorder);
        if (arc.arcType == FlexibleArc.out) {
            searcher.calcChecker.addLateVariable(tokenVar, searcher.recorder);
            return Collections.emptySet();
        } else {
            Collection<Binder> coll = new Vector<Binder>();
            coll.add(new FlexibleArcBinder(this));
            return coll;
        }
    }

    public Collection<Executable> makeExecutables(VariableMapperCopier copier) {
        Collection<Executable> coll = new Vector<Executable>();
        switch (arc.arcType) {
        case FlexibleArc.out:
            Variable copiedTokenVar = (Variable) copier.getCopier()
                                                       .copy(tokenVar);
            coll.add(new FlexibleOutArcExecutable(placeInstance,
                                                  getTransition(),
                                                  copiedTokenVar, arc));
            return coll;
        case FlexibleArc.in:
        case FlexibleArc.fastBoth:
            coll.add(new FlexibleInArcExecutable(placeInstance,
                                                 getTransition(), inTokens, arc));
            return coll;
        default:
            throw new RuntimeException("Bad arc type.");
        }
    }

    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier variableMapperCopier) {
        return null;
    }
}