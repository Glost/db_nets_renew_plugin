/*
 * Created on 26.11.2004
 *
 */
package de.renew.gui.logging;

import de.renew.engine.common.SimulatorEvent;
import de.renew.engine.common.StepIdentifier;


/**
 * The interface for a repository which stores {@link StepTrace}s with logging
 * events of a simulation run.
 *
 * @author Sven Offermann (code)
 * @author Michael Duvigneau (documentation)
 */
public interface StepTraceRepository {

    /**
     * Add the given <code>event</code> to the repository. The event is stored
     * as part of the {@link StepTrace} assigned to its {@link StepIdentifier}.
     * If no such step trace exists, it is created on demand.
     * <p>
     * Registered {@link RepositoryChangeListener}s will be informed about the change.
     * </p>
     * <p>
     * Repositories are allowed to automatically discard step traces if their
     * capacity limit is reached.
     * </p>
     *
     * @param event the event to add to the repository.
     * @see StepTrace#log(de.renew.engine.events.SimulationEvent)
     * @see SimulatorEvent#getStep()
     */
    public void addEvent(SimulatorEvent event);

    /**
     * Retrieve those step traces from the repository that match the given step
     * identifiers.
     *
     * @param steps the step identifiers denoting the step traces to retrieve.
     * @return the step traces corresponding to the given step identifiers.
     */
    public StepTrace[] getStepTraces(StepIdentifier[] steps);

    /**
     * Retrieve all step traces from the repository.
     *
     * @return all step traces currently stored in the repository.
     */
    public StepTrace[] getAllStepTraces();

    /**
     * Retrieve the step trace that matches the given step identifier.
     *
     * @param stepIdentifier the step identifier denoting the step traces to
     *            retrieve.
     * @return the step traces corresponding to the given step identifiers.
     */
    public StepTrace getStepTrace(StepIdentifier stepIdentifier);

    /**
     * Remove the step trace that matches the given step identifier from the
     * repository, if it exists.
     * <p>
     * Registered {@link RepositoryChangeListener}s will be informed about the
     * change.
     * </p>
     *
     * @param stepIdentifier the step identifier denoting the step traces to
     *            retrieve.
     * @return <code>true</code>, if a step trace was removed.
     */
    public boolean removeStepTrace(StepIdentifier stepIdentifier);

    /**
     * Register the given listener so that it will be notified of future changes
     * to this repository.
     *
     * @param listener the observer to notify about changes.
     */
    public void addRepositoryChangeListener(RepositoryChangeListener listener);

    /**
     * Unregister the given listener so that it will no longer be informed of
     * changes to this repository.
     *
     * @param listener the observer that should no longer be notified about changes.
     */
    public void removeRepositoryChangeListener(RepositoryChangeListener listener);
}