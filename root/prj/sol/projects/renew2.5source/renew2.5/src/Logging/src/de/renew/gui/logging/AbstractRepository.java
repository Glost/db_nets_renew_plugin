/*
 * Created on 26.11.2004
 *
 */
package de.renew.gui.logging;

import java.util.HashSet;
import java.util.Set;


/**
 * This class provides basic listener registration and notification
 * functionality needed by common repository implementations.
 *
 * @author Sven Offermann (code)
 * @author Michael Duvigneau (documentation)
 **/
public abstract class AbstractRepository implements StepTraceRepository {

    /**
     * Stores references to all registered {@link RepositoryChangeListener}
     * instances.
     */
    protected Set<RepositoryChangeListener> listeners = new HashSet<RepositoryChangeListener>();

    /* (non-Javadoc)
     * @see de.renew.gui.logging.StepTraceRepository#addRepositoryChangeListener(de.renew.gui.logging.RepositoryChangeListener)
     */
    @Override
    public void addRepositoryChangeListener(RepositoryChangeListener listener) {
        this.listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.StepTraceRepository#removeRepositoryChangeListener(de.renew.gui.logging.RepositoryChangeListener)
     */
    @Override
    public void removeRepositoryChangeListener(RepositoryChangeListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Inform all currently registered listeners about a step trace change.
     *
     * @param stepTrace the affected step trace.
     * @see StepTraceChangeListener#stepTraceChanged(StepTrace)
     **/
    protected void fireStepTraceChanged(StepTrace stepTrace) {
        RepositoryChangeListener[] l = this.listeners.toArray(new RepositoryChangeListener[] {  });
        for (int x = 0; x < l.length; x++) {
            l[x].stepTraceChanged(stepTrace);
        }
    }

    /**
     * Inform all currently registered listeners about a newly added step trace.
     *
     * @param stepTrace the new step trace
     * @see RepositoryChangeListener#stepTraceAdded(StepTraceRepository,
     *      StepTrace)
     **/
    protected void fireStepTraceAdded(StepTrace stepTrace) {
        RepositoryChangeListener[] l = this.listeners.toArray(new RepositoryChangeListener[] {  });
        for (int x = 0; x < l.length; x++) {
            l[x].stepTraceAdded(null, stepTrace);
        }
    }

    /**
     * Inform all currently registered listeners about a removed step trace.
     *
     * @param stepTrace  the former step trace
     * @see RepositoryChangeListener#stepTraceRemoved(StepTraceRepository, StepTrace)
     **/
    protected void fireStepTraceRemoved(StepTrace stepTrace) {
        RepositoryChangeListener[] l = this.listeners.toArray(new RepositoryChangeListener[] {  });
        for (int x = 0; x < l.length; x++) {
            l[x].stepTraceRemoved(null, stepTrace);
        }
    }

    /**
     * Inform all currently registered listeners that a step trace might be
     * removed. Vetos are collected, the event distribution is cancelled on the
     * first veto.
     *
     * @param stepTrace  the step trace to remove
     * @return whether some listener issued a veto
     * @see RepositoryChangeListener#stepTraceRemoveRequest(StepTraceRemoveRequest)
     */
    protected boolean fireStepTraceRemoveRequest(StepTrace stepTrace) {
        boolean removeVeto = false;
        StepTraceRemoveRequest request = new StepTraceRemoveRequest(this,
                                                                    stepTrace);

        RepositoryChangeListener[] l = this.listeners.toArray(new RepositoryChangeListener[] {  });
        for (int x = 0; ((x < l.length) && (!removeVeto)); x++) {
            l[x].stepTraceRemoveRequest(request);
            removeVeto = request.hasVeto();
        }

        return removeVeto;
    }
}