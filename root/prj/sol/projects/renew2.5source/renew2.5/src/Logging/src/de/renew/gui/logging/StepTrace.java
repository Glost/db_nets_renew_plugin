/*
 * Created on 17.08.2004
 */
package de.renew.gui.logging;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.SimulationEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;


/**
 * Stores the log events (see de.renew.engine.events.*) for a single simulation step
 *
 * @author Sven Offermann
 */
public class StepTrace {
    private Vector<SimulationEvent> events = new Vector<SimulationEvent>();
    private Set<StepTraceChangeListener> listeners = Collections
                                                         .synchronizedSet(new HashSet<StepTraceChangeListener>());
    private StepIdentifier step;

    public StepTrace(StepIdentifier step) {
        this.step = step;
    }

    public void log(SimulationEvent event) {
        if (!events.contains(event)) {
            events.add(event);
            fireStepTraceChanged();
        }
    }

    public SimulationEvent[] getEvents() {
        return this.events.toArray(new SimulationEvent[] {  });
    }

    public StepIdentifier getStepIdentifier() {
        return this.step;
    }

    public String toString() {
        return "Simulator Step " + step;
    }

    public void addStepTraceChangeListener(StepTraceChangeListener listener) {
        this.listeners.add(listener);
    }

    public boolean removeStepTraceChangeListener(StepTraceChangeListener listener) {
        return this.listeners.remove(listener);
    }

    public void fireStepTraceChanged() {
        StepTraceChangeListener[] l = this.listeners.toArray(new StepTraceChangeListener[] {  });
        for (int x = 0; x < l.length; x++) {
            l[x].stepTraceChanged(this);
        }
    }
}