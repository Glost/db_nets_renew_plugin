package de.renew.engine.common;

import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.BindingBadness;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searcher.TriggerableCollection;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.unify.Impossible;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public abstract class AssignBinder implements Binder {
    Variable variable;
    boolean checkBound;

    public AssignBinder(Variable variable, boolean checkBound) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.variable = variable;
        this.checkBound = checkBound;
    }

    public int bindingBadness(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // The possibly incomplete value in the variable
        // will not be stored anywhere, so that it is
        // not required to make a copy of it.
        if (Unify.isBound(variable)) {
            // This binder has become useless. It should
            // be removed soon, but aborting the search is still
            // preferable.
            return 1;
        } else {
            int result;
            lock();
            try {
                Collection<?extends Object> candidates = getCandidates(variable
                                                                       .getValue());
                result = BindingBadness.clip(candidates.size());
            } finally {
                unlock();
            }
            return result;
        }
    }

    public void bind(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (Unify.isBound(variable) && !checkBound) {
            // The variable is already bound.
            // No need to try different values.
            // It is not even required to register a trigger,
            // because other binders have narrowed down the
            // number of possible bindings to one. They have registered
            // triggers that set off when there will be other possible
            // values.
            searcher.search();
        } else {
            // Make sure that the trigger notifies
            // the searchable if its state changes,
            // because in that case the possible bindings would have
            // to be rechecked.
            TriggerableCollection triggerables = getTriggerables();
            if (triggerables != null) {
                // There is a trigger.
                searcher.insertTriggerable(triggerables);
            }

            List<Object> candidates;
            lock();
            try {
                candidates = new ArrayList<Object>(getCandidates(variable
                                 .getValue()));
            } finally {
                unlock();
            }

            // Make sure to randomize the array so that binding
            // are determined in an approximately fair way. 
            Collections.shuffle(candidates);
            Iterator<Object> values = candidates.iterator();

            while (values.hasNext() && !searcher.isCompleted()) {
                Object value = values.next();

                int checkpoint = searcher.recorder.checkpoint();
                try {
                    Unify.unify(variable, value, searcher.recorder);


                    // Ok, unification succeeded.
                    searcher.search();
                } catch (Impossible e) {
                    // Unification failed.
                }
                searcher.recorder.restore(checkpoint);
            }
        }
    }

    /**
     * Subclasses that return a volatile candidate enumeration
     * may use this procedure to guarantee the stability of the enumeration.
     * Other subclasses should provide a null implementation.
     */
    public abstract void lock();

    /**
     * Undo the effects of {@link #lock()}.
     */
    public abstract void unlock();

    /**
     * Return a collection of possible values. It is allowed to return values
     * that are impossible, but this will affect performance.
     * This list must be robust enough not to change internally
     * due to search activities.
     *
     * @param pattern a possibly incomplete pattern that the candidates
     *   should match
     * @return a collection of values
     */
    public abstract Collection<?extends Object> getCandidates(Object pattern);

    /**
     * Get the triggerable collection where we can register
     * for updates of the candidate list.
     *
     * @return the triggerable collection or <tt>null</tt> if the
     *   enumeration will always be the same
     */
    public abstract TriggerableCollection getTriggerables();
}