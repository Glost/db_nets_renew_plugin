package de.renew.net;

import de.renew.engine.common.CompositeOccurrence;
import de.renew.engine.common.TraceExecutable;
import de.renew.engine.searcher.Executable;
import de.renew.engine.searcher.OccurrenceDescription;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.VariableMapperCopier;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.expression.LocalVariable;
import de.renew.expression.VariableMapper;

import de.renew.net.event.FiringEvent;

import de.renew.unify.Impossible;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * A transition occurrence is a transition instance
 * that is (or might be) firing. A transition instance that fires
 * simultaneously with itself will require multiple transition
 * occurrences. Each occurence has its own set of local variables,
 * its own set of binders, and its own set of inscription
 * occurrences.
 *
 * @author Olaf Kummer
 **/
public class TransitionOccurrence extends CompositeOccurrence {
    private VariableMapper mapper;

    /**
     * Create a transition occurrence. Inscription occurrences
     * will be created as needed.
     *
     * @param transitionInstance
     *   the transition instance of which I will be an occurence
     * @param params
     *   parameters of the synchronous channel that invoked me,
     *   if any
     * @param searcher
     *   the searcher that initiated the occurence of the
     *   transition
     * @exception de.renew.unify.Impossible
     *   I cannot possibly occur
     *   with the current parameters, regardless of the marking
     *   of the neighboring places. (E.g. two parameters in the channel
     *   are required to be the same, but aren't.)
     **/
    TransitionOccurrence(TransitionInstance transitionInstance,
                         Variable params, Searcher searcher)
            throws Impossible {
        super(transitionInstance);

        // Make a simple variable mapper.
        mapper = new VariableMapper();
        // Make the special variable "this" known to the mapper.
        Variable thisVariable = mapper.map(new LocalVariable("this", false));
        try {
            Unify.unify(thisVariable, transitionInstance.getNetInstance(), null);
        } catch (Impossible e) {
            throw new RuntimeException("Unification failed unexpectedly.");
        }

        Transition transition = transitionInstance.getTransition();
        if (transition.uplink != null) {
            Object up = transition.uplink.params.startEvaluation(mapper,
                                                                 searcher.recorder,
                                                                 searcher.calcChecker);
            Unify.unify(up, params, searcher.recorder);


            // If the unification produced an exception,
            // then no bindings are possible. The caller will
            // handle this situation.
        }

        Iterator<TransitionInscription> iterator = transition.inscriptions
                                                       .iterator();
        while (iterator.hasNext()) {
            TransitionInscription inscription = iterator.next();
            addOccurrences(inscription.makeOccurrences(mapper,
                                                       transitionInstance
                .getNetInstance(), searcher));
        }
    }

    /**
     * I will provide executables that
     * perform the required actions according to the
     * current bindings of variables. This includes, among other
     * actions, token moves and tests.
     *
     * All my binders must have been processed before it is allowed
     * to call this method.
     *
     * @param copier
     *   a copier that I can use to rescue the current values of
     *   variables from backtracking
     * @return an enumeration of executables
     *
     * @see de.renew.engine.searcher.Executable
     **/
    public Collection<Executable> makeExecutables(VariableMapperCopier copier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Collection<Executable> executables = new ArrayList<Executable>();
        executables.addAll(super.makeExecutables(copier));

        // Possibly insert a trace object.
        if (getTransition().getTransition().getTrace()) {
            executables.add(new TraceExecutable("Firing " + getTransition(),
                                                getTransition()));
        }


        // Insert notifiers that can report the variable mapping.
        // Both will report the same firing event to the
        // listeners. Event identities are unique so that
        // the listener can associate start and end of a
        // firing.
        FiringEvent event = new FiringEvent(getTransition(),
                                            copier.makeCopy(mapper));
        executables.add(new FiringStartExecutable(event));
        executables.add(new FiringCompleteExecutable(event));

        return executables;
    }

    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier copier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return new TransitionOccurrenceDescription(getTransition(),
                                                   copier.makeCopy(mapper));
    }

    public String toString() {
        return getTransition().toString();
    }
}