/*
 * Created on 26.11.2004
 *
 */
package de.renew.gui.logging;

import de.renew.engine.common.SimulatorEvent;
import de.renew.engine.common.StepIdentifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;


/**
 * A <code>LoggerRepository</code> groups simulation events related to some
 * category (e.g. a specific logger name). It keeps references to
 * {@link StepTrace} objects up to a specified capacity which can be changed
 * dynamically.
 * <p>
 * In fact the {@link StepTrace} objects are stored at the
 * {@link MainRepository} and therefore shared with other
 * <code>LoggerRepository</code> instances. This class just stores
 * {@link StepIdentifier} objects to refer to corresponding {@link StepTrace}s
 * so that it presents a filtered view of step traces. Objects of this class also
 * listen to step trace changes in the {@link MainRepository} so that they do
 * not miss any updates not directly sent to them.
 * </p>
 *
 * @author Sven Offermann (code)
 * @author Michael Duvigneau (documentation)
 *
 * @see MainRepository
 * @see #setCapacity(int)
 **/
public class LoggerRepository extends AbstractRepository
        implements RepositoryChangeListener {

    /**
     * the current capacity setting.
     **/
    private int capacity = 0;

    /**
     * We identify the our local list of {@link StepTrace}s of interest
     * by keeping the corresponding {@link StepIdentifier}s.
     **/
    private List<StepIdentifier> stepTraces = new LinkedList<StepIdentifier>();

    /**
     * a reference to our shared <code>MainRepository</code> that manages the
     * {@link StepTrace} objects
     **/
    private MainRepository repository;

    /**
     * Create a new logger repository with the given <code>capacity</code> that
     * shares information with the given {@link MainRepository}.
     * <p>
     * <code>LoggerRepository</code> objects should be created by
     * {@link MainRepository#getLoggerRepository(String, int)} only.
     * </p>
     *
     * @param repository the main repository to share {@link StepTrace}s with.
     * @param capacity the initial limit on the number of {@link StepTrace}s
     *            this repository will store.
     */
    LoggerRepository(MainRepository repository, int capacity) {
        if (capacity >= 0) {
            this.capacity = capacity;
        }
        this.repository = repository;
    }

    /**
     * Reconfigure the capacity of this repository. The change will take effect
     * the next time the capacity limit is reached when a new step trace is
     * added.
     *
     * @param capacity the new limit on the number of {@link StepTrace}s this
     *            repository will store.
     **/
    public void setCapacity(int capacity) {
        if (capacity >= 0) {
            this.capacity = capacity;
        }
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.StepTraceRepository#getStepTraces(de.renew.engine.common.StepIdentifier[])
     */
    @Override
    public StepTrace[] getStepTraces(StepIdentifier[] steps) {
        // To prohibit retrieval of step traces from the main repository
        // that are not visible in this LoggerRepository, we filter the
        // argument for known step identifiers.
        Vector<StepIdentifier> traces = new Vector<StepIdentifier>();
        for (int x = 0; x < steps.length; x++) {
            if (stepTraces.contains(steps[x])) {
                traces.add(steps[x]);
            }
        }

        // Now we can delegate step trace retrieval to the main repository
        // with the filtered identifiers.
        return repository.getStepTraces(traces.toArray(new StepIdentifier[] {  }));
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.StepTraceRepository#getAllStepTraces()
     */
    @Override
    public StepTrace[] getAllStepTraces() {
        // pass our list of known step identifiers to the main
        // repository for step trace retrieval
        return repository.getStepTraces(stepTraces.toArray(new StepIdentifier[] {  }));
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.StepTraceRepository#getStepTrace(de.renew.engine.common.StepIdentifier)
     */
    @Override
    public StepTrace getStepTrace(StepIdentifier stepIdentifier) {
        // To prohibit retrieval of step traces from the main repository
        // that are not visible in this LoggerRepository, we filter the
        // argument for known step identifiers.
        if (!stepTraces.contains(stepIdentifier)) {
            return null;
        }
        return repository.getStepTrace(stepIdentifier);
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.StepTraceRepository#getStepTrace(de.renew.engine.common.StepIdentifier)
     */
    @Override
    public boolean removeStepTrace(StepIdentifier stepIdentifier) {
        boolean success = this.stepTraces.remove(stepIdentifier);
        if (success) {
            StepTrace stepTrace = repository.getStepTrace(stepIdentifier);

            // remove from the underlying repository
            repository.removeStepTrace(stepIdentifier);

            // so we must inform our listeners about the removal of the StepTrace.
            this.fireStepTraceRemoved(stepTrace);
        }
        return success;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If a new step trace is created and the configured capacity of this
     * repository is reached, old step traces will be discarded.
     * </p>
     **/
    @Override
    public void addEvent(SimulatorEvent event) {
        // add messages to the underlying repository in any case.
        repository.addEvent(event);

        // if the step identifier is yet unknown to this LoggerRepository,
        // we have a visible addition.  Check the capacity, store a reference
        // and inform listeners.
        if (!this.stepTraces.contains(event.getStep())) {
            if ((capacity > 0) && (stepTraces.size() > capacity - 1)) {
                while (stepTraces.size() > capacity - 1) {
                    StepIdentifier rStep = stepTraces.get(0);
                    removeStepTrace(rStep);
                }
            }

            this.stepTraces.add(event.getStep());

            // inform listeners that we added some logging information
            this.fireStepTraceAdded(repository.getStepTrace(event.getStep()));
        }
    }

    // ------------------------------ listener for step trace repository changes

    /* (non-Javadoc)
     * @see de.renew.gui.logging.RepositoryChangeListener#stepTraceAdded(de.renew.gui.logging.StepTraceRepository, de.renew.gui.logging.StepTrace)
     */
    @Override
    public void stepTraceAdded(StepTraceRepository repository,
                               StepTrace stepTrace) {
        // forward only if we know the added StepTrace. Normally we added it.
        if (this.stepTraces.contains(stepTrace.getStepIdentifier())) {
            this.fireStepTraceAdded(stepTrace);
        }
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.RepositoryChangeListener#stepTraceRemoved(de.renew.gui.logging.StepTraceRepository, de.renew.gui.logging.StepTrace)
     */
    @Override
    public void stepTraceRemoved(StepTraceRepository repository,
                                 StepTrace stepTrace) {
        // do nothing. we inform our listeners in the remove method.
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.RepositoryChangeListener#stepTraceRemoveRequest(de.renew.gui.logging.StepTraceRemoveRequest)
     */
    @Override
    public void stepTraceRemoveRequest(StepTraceRemoveRequest request) {
        if (this.stepTraces.contains(request.getStepTrace().getStepIdentifier())) {
            request.veto();
        } else {
            // forward the event to registered listeners
            this.fireStepTraceRemoveRequest(request.getStepTrace());
        }
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.StepTraceChangeListener#stepTraceChanged(de.renew.gui.logging.StepTrace)
     */
    @Override
    public void stepTraceChanged(StepTrace stepTrace) {
        // forward only if we know the added StepTrace. Normally we added it.
        if (this.stepTraces.contains(stepTrace.getStepIdentifier())) {
            this.fireStepTraceChanged(stepTrace);
        }
    }
}