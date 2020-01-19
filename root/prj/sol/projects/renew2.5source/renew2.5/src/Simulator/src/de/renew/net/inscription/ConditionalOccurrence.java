package de.renew.net.inscription;

import de.renew.engine.searcher.AbstractOccurrence;
import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.BindingBadness;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.OccurrenceDescription;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;

import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.Transition;

import de.renew.unify.Impossible;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import de.renew.util.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


// A conditional occurrence is its own binder. This simplifies
// the code a bit.
class ConditionalOccurrence extends AbstractOccurrence implements Occurrence,
                                                                  Binder {
    ConditionalInscription conditionalInscription;
    VariableMapper mapper;
    NetInstance netInstance;
    Variable conditionVariable;
    private Collection<Occurrence> secondaryOccurrences;
    private boolean wantToOccur;

    public ConditionalOccurrence(ConditionalInscription conditionalInscription,
                                 VariableMapper mapper,
                                 NetInstance netInstance, Transition transition) {
        super(netInstance.getInstance(transition));
        this.conditionalInscription = conditionalInscription;
        this.mapper = mapper;
        this.netInstance = netInstance;
    }

    public Collection<Binder> makeBinders(Searcher searcher)
            throws Impossible {
        conditionVariable = new Variable(conditionalInscription.conditionExpression
                                         .startEvaluation(mapper,
                                                          searcher.recorder,
                                                          searcher.calcChecker),
                                         searcher.recorder);


        // I am my own binder.
        Collection<Binder> coll = new Vector<Binder>();
        coll.add(this);
        return coll;
    }

    public int bindingBadness(Searcher searcher) {
        if (Unify.isBound(conditionVariable)) {
            return 1;
        } else {
            return BindingBadness.max;
        }
    }

    public void bind(Searcher searcher) {
        Object obj = conditionVariable.getValue();
        if (!(obj instanceof Value)) {
            // Expression is badly bound.
            return;
        }

        if (!(obj instanceof Boolean)) {
            // Expression is badly bound.
            return;
        }

        Collection<Binder> binders = new ArrayList<Binder>();

        int checkpoint = searcher.recorder.checkpoint();

        try {
            boolean wantToOccur = ((Boolean) ((Value) obj).value).booleanValue();

            secondaryOccurrences = new ArrayList<Occurrence>();
            if (wantToOccur) {
                secondaryOccurrences.addAll(conditionalInscription.inscription
                    .makeOccurrences(mapper, netInstance, searcher));


                // Create all binders.
                Iterator<Occurrence> iterator = secondaryOccurrences.iterator();
                while (iterator.hasNext()) {
                    Occurrence occurrence = iterator.next();
                    binders.addAll(occurrence.makeBinders(searcher));
                }


                // After this statement, no further exceptions should be thrown.
                // Add binders to searcher.
                searcher.addBinders(binders);
            }

            searcher.removeBinder(this);
            searcher.search();
            searcher.addBinder(this);

            if (wantToOccur) {
                searcher.removeBinders(binders);
                secondaryOccurrences = null;
            }
        } catch (Impossible e) {
            // This is expected. The secondary occurrence could
            // not create its binders.
        }

        searcher.recorder.restore(checkpoint);
    }

    public Collection<Executable> makeExecutables(VariableMapperCopier copier) {
        Collection<Executable> executables = new ArrayList<Executable>();

        if (wantToOccur) {
            // Create all executables.
            Iterator<Occurrence> iterator = secondaryOccurrences.iterator();
            while (iterator.hasNext()) {
                Occurrence occurrence = iterator.next();
                executables.addAll(occurrence.makeExecutables(copier));
            }
        }

        return executables;
    }

    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier variableMapperCopier) {
        return null;
    }
}