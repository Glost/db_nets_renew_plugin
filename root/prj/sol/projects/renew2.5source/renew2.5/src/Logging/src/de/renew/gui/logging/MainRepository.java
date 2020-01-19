/*
 * Created on Aug 23, 2004
 */
package de.renew.gui.logging;

import de.renew.engine.common.SimulatorEvent;
import de.renew.engine.common.StepIdentifier;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;


/**
 * A <code>MainRepository</code> stores all simulation events for one simulation
 * run. Events are recorded into {@link StepTrace} objects that accumulate all
 * log messages related to a single simulation step. The {@link StepIdentifier}
 * of simulation events determines which {@link StepTrace} the event belongs to.
 * <p>
 * Although the step traces are stored in this <code>MainRepository</code>, the
 * responsibility for event inclusion and removal lies with separate
 * {@link LoggerRepository} objects. A <code>LoggerRepository</code> can be
 * retrieved based on a distinguished name via
 * {@link #getLoggerRepository(String, int)}. <code>LoggerRepository</code>
 * instances are created on demand. Step traces are stored in this
 * <code>MainRepository</code> as long as any <code>LoggerRepository</code>
 * refers to the trace.
 * </p>
 *
 * @author Sven Offermann (code)
 * @author Michael Duvigneau (documentation)
 */
public class MainRepository extends AbstractRepository
        implements StepTraceChangeListener {

    /**
    * maps logger names to LoggerRepositories
    */
    private Map<String, LoggerRepository> loggerRepositories = new Hashtable<String, LoggerRepository>();

    /**
     * maps StepIdentifiers to StepTraces (stores all occured log messages
     * for a single simulation step)
     */
    private Map<StepIdentifier, StepTrace> stepTraces = new Hashtable<StepIdentifier, StepTrace>();

    /**
     * Retrieve the {@link LoggerRepository} instance responsible for the given
     * logger name. If such a repository does not exist yet, it is automatically
     * created. In any case, the given <code>capacity</code> is passed to the
     * repository.
     *
     * @param loggerName the name identifying the logger repository. In most
     *            cases, this is the category name used in the Log4j framework.
     * @param capacity the limit on stored step traces for the repository. It is
     *            passed to newly created as well as to already existing
     *            repositories to ensure dynamic configuration.
     * @return the logger repository for the given name.  If none existed before,
     *            one is created.
     **/
    public LoggerRepository getLoggerRepository(String loggerName, int capacity) {
        LoggerRepository lRepository = this.loggerRepositories.get(loggerName);

        if (lRepository == null) {
            lRepository = new LoggerRepository(this, capacity);
            this.loggerRepositories.put(loggerName, lRepository);
            this.addRepositoryChangeListener(lRepository);
        }

        lRepository.setCapacity(capacity);
        return lRepository;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The <code>MainRepository</code> does not discard any step traces by its
     * own initiative. Step trace removal is requested by
     * {@link LoggerRepository} instances when they no longer need a specific
     * trace.
     * </p>
     * <p>
     * The <code>MainRepository</code> registers itself as listener to all step
     * traces so that it will be notified about event additions to any step
     * trace it stores.
     * </p>
     **/
    @Override
    public void addEvent(SimulatorEvent event) {
        // add the event information to the repository
        StepTrace stepTrace = this.stepTraces.get(event.getStep());
        if (stepTrace == null) {
            stepTrace = new StepTrace(event.getStep());
            stepTrace.addStepTraceChangeListener(this);
            this.stepTraces.put(event.getStep(), stepTrace);
            this.fireStepTraceAdded(stepTrace);
        }

        stepTrace.log(event.getMessage());
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.StepTraceRepository#getStepTraces(de.renew.engine.common.StepIdentifier[])
     */
    @Override
    public StepTrace[] getStepTraces(StepIdentifier[] steps) {
        Vector<StepTrace> traces = new Vector<StepTrace>();

        for (int x = 0; x < steps.length; x++) {
            if (steps[x] != null) {
                StepTrace trace = stepTraces.get(steps[x]);
                if (trace != null) {
                    traces.add(trace);
                }
            }
        }

        return traces.toArray(new StepTrace[] {  });
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.StepTraceRepository#getAllStepTraces()
     */
    @Override
    public StepTrace[] getAllStepTraces() {
        return stepTraces.entrySet().toArray(new StepTrace[] {  });
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.StepTraceRepository#getStepTrace(de.renew.engine.common.StepIdentifier)
     */
    @Override
    public StepTrace getStepTrace(StepIdentifier stepIdentifier) {
        StepTrace trace = this.stepTraces.get(stepIdentifier);

        return trace;
    }

    /* (non-Javadoc)
     * @see de.renew.gui.logging.StepTraceRepository#removeStepTrace(de.renew.engine.common.StepIdentifier)
     */
    @Override
    public boolean removeStepTrace(StepIdentifier stepIdentifier) {
        StepTrace stepTrace = getStepTrace(stepIdentifier);

        if (stepTrace != null) {
            if (!(fireStepTraceRemoveRequest(stepTrace))) {
                // no vetos, so we can remove the StepTrace from the repository
                this.stepTraces.remove(stepIdentifier);

                return true;
            }
        }

        return false;
    }

    // ----------------------------------- step trace listener implementation

    /**
     * {@inheritDoc}
     * <p>
     * The change is propagated to all listeners of this <code>MainRepository</code>.
     * </p>
     **/
    @Override
    public void stepTraceChanged(StepTrace stepTrace) {
        // simply forward the change event
        this.fireStepTraceChanged(stepTrace);
    }
}