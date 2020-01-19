package de.renew.engine.searcher;

import de.renew.engine.simulator.SimulationThreadPool;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class TriggerCollection implements Serializable {
    // Keep track of triggering objects.
    private Set<TriggerableCollection> triggers;
    private Triggerable triggerable;

    public TriggerCollection(Triggerable triggerable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        triggers = new HashSet<TriggerableCollection>();
        this.triggerable = triggerable;
    }

    // The following two method must only be accessed through
    // the include and exclude methods of triggerable collections,
    // otherwise deadlocks might occur. Those method will make sure
    // to lock this object.
    public void include(TriggerableCollection trigger) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        triggers.add(trigger);
    }

    public void exclude(TriggerableCollection trigger) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        triggers.remove(trigger);
    }

    public void clear() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        List<TriggerableCollection> list = new ArrayList<TriggerableCollection>();
        synchronized (this) {
            // Get the triggers.
            list.addAll(triggers);
        }


        // Send the notifications. No need to synchronize here.
        // Better allow concurrent accesses to reduce deadlock potential.
        // The triggerable collections are required to do the locking.
        Iterator<TriggerableCollection> iterator = list.iterator();
        while (iterator.hasNext()) {
            TriggerableCollection triggerables = iterator.next();
            triggerables.exclude(triggerable);
        }
    }
}