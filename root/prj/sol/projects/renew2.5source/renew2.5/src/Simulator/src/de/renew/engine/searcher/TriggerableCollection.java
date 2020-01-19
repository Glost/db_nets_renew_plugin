package de.renew.engine.searcher;

import de.renew.engine.simulator.SimulationThreadPool;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


// Recently added synchronisation. Will be necessary
// in concurrent environments. Should be covered by
// the place locking scheme, but let's not take any risks,
// because this might change.


/**
 * This class controls the triggering especially of transitions
 * for the purpose of reinsertion into the search queue.
 *
 * The general picture: *Before* relying on a marking for the analysis
 * of a triggerable's enabledness, the triggerable is registered with
 * the place's triggerable collection.
 *
 * After a change is done to a place, the place's triggerable collection
 * is used to inform all transitions of their possible enabledness.
 *
 * Each transition removes itself from all its triggers.
 * Afterwards it inserts itself into the search queue.
 *
 * This ensures that each marking change is guaranteed to force the
 * reexamination of the affected transitions. Transitions might
 * be examined too often, but only when a change was made
 * during their examination.
 *
 * Lock order for deadlock exclusion:
 * <ol>
 *   <li> triggerable collections
 *   <li> trigger collections
 * </ol>
 * The locking is done by synchronisation.
 *
 * The global data structure:
 * Each triggerable has an associated trigger collection.
 * Triggerable collections reference triggerables and make
 * sure that they are referenced by the associated trigger collections.
 *
 * The associations between trigger collections and triggerable collections
 * cannot be stored in a central data structure, because this would
 * eliminate garbage collection.
 *
 * Because there are now links in both directions, a triggerable
 * references its triggers even after the tokens that gave rise to the
 * possible connection have disappeared. Even if a black token
 * is removed from the input of a triggerable, that triggerable needs
 * to be rechecked, because it might be determined that a
 * triggering is no longer possible. That knowledge would in turn allow
 * garbage collection in some cases.
 */
public class TriggerableCollection implements Serializable {
    // Keep track of notified objects.
    private Set<Triggerable> triggerables;

    public TriggerableCollection() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        triggerables = new HashSet<Triggerable>();
    }

    public synchronized void include(Triggerable triggerable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        TriggerCollection triggers = triggerable.triggers();
        synchronized (triggers) {
            triggerables.add(triggerable);
            triggers.include(this);
        }
    }

    public synchronized void exclude(Triggerable triggerable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        TriggerCollection triggers = triggerable.triggers();
        synchronized (triggers) {
            triggerables.remove(triggerable);
            triggers.exclude(this);
        }
    }

    public void proposeSearch() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        List<Triggerable> list = new ArrayList<Triggerable>();
        synchronized (this) {
            // Get the triggerables.
            list.addAll(triggerables);
        }


        // Send the notifications. No need to synchronize here.
        // Better allow concurrent accesses to reduce deadlock potential.
        // This way the triggerables can exclude themselves during the
        // notification.
        Iterator<Triggerable> iterator = list.iterator();
        while (iterator.hasNext()) {
            Triggerable triggerable = iterator.next();
            triggerable.proposeSearch();
        }
    }
}