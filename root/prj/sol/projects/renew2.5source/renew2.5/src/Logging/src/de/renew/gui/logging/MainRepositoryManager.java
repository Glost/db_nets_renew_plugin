/*
 * Created on Nov 22, 2004
 *
 */
package de.renew.gui.logging;

import de.renew.application.SimulatorPlugin;

import de.renew.engine.common.SimulatorEvent;
import de.renew.engine.common.StepIdentifier;

import java.util.HashSet;
import java.util.Vector;


/**
 * @author Sven Offermann
 *
 */
public class MainRepositoryManager {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(MainRepositoryManager.class);
    private static MainRepositoryManager manager = null;

    // manages the repositories for different simulation runs.
    // for performance reasons this is implemented with arrays 
    // and not with util classes.
    private LongHashSet[] simulationRunIds;
    private MainRepository[] repositories;

    /** simulationHistoriesDim determines the dimension of the
     * simulation history. This value determines how many
     * simulation traces should be stored in the memory.
     * The simulation traces are stored in a kind of ring puffer.
     * Older simulations traces will be removed from the puffer.
     * This value should be as small as possible to prevent
     * memory consumption.
     */
    private static int defaultSimulationHistoriesDim = 2;

    public MainRepositoryManager() {
        this(defaultSimulationHistoriesDim);
    }

    public MainRepositoryManager(int simulationHistoriesDim) {
        this.simulationRunIds = new LongHashSet[simulationHistoriesDim];
        this.repositories = new MainRepository[simulationRunIds.length];
    }

    public static MainRepositoryManager getInstance() {
        if (manager == null) {
            manager = new MainRepositoryManager();
        }

        return manager;
    }

    public long[] getSimulationRunIds() {
        Vector<Long> idVector = new Vector<Long>();

        int x = 0;
        while ((x < this.simulationRunIds.length)
                       && (this.repositories[x] != null)) {
            idVector.addAll(this.simulationRunIds[x]);
            x++;
        }

        long[] ids = new long[idVector.size()];
        for (x = 0; x < idVector.size(); x++) {
            ids[x] = idVector.get(x).longValue();
        }

        return ids;
    }

    public MainRepository getRepository(long simulationRunId) {
        if (logger.isTraceEnabled()) {
            logger.trace(MainRepositoryManager.class.getSimpleName()
                         + " fetching main repo for runId: " + simulationRunId);
        }
        int x = 0;
        while ((x < repositories.length) && (repositories[x] != null)) {
            if (simulationRunIds[x].contains(simulationRunId)) {
                // found the queried repository 
                return repositories[x];
            }
            x++;
        }

        // Since the simulation run id is not known yet, we assume a new
        // simulation environment.  Collect all run ids of that environment.
        // If the encountered run id is included, we can assign a new
        // MainRepository.  If not, we are in an unknown situation.
        long[] collectedSimulationRunIds;
        try {
            collectedSimulationRunIds = SimulatorPlugin.getCurrent()
                                                       .getCurrentEnvironment()
                                                       .getSimulator()
                                                       .collectSimulationRunIds();
        } catch (NullPointerException e) {
            // There is no current simulator, so we cannot assign the run
            // id to the current simulation.
            logger.warn(MainRepositoryManager.class.getSimpleName()
                        + ": Could not determine the simulation the run id "
                        + simulationRunId + " belongs to.");
            if (logger.isDebugEnabled()) {
                logger.debug(MainRepositoryManager.class.getSimpleName() + ": "
                             + e);
            }
            collectedSimulationRunIds = new long[] { simulationRunId };
        }

        LongHashSet newRunIds = new LongHashSet(collectedSimulationRunIds);

        // check whether the set of run ids includes the encountered one
        if (!newRunIds.contains(simulationRunId)) {
            logger.warn(MainRepositoryManager.class.getSimpleName()
                        + ": The run id " + simulationRunId
                        + " does not belong to the current simulation.");
            collectedSimulationRunIds = new long[] { simulationRunId };
            newRunIds = new LongHashSet(collectedSimulationRunIds);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(MainRepositoryManager.class.getSimpleName()
                         + ": Creating new repository covering run ids "
                         + newRunIds);
        }

        // create a new repository.
        MainRepository repository = new MainRepository();

        // add repository with the runId to the list of known repositories.
        // For performance reasons we shift the existing repositories by one
        // position in the array list, cause normally the last created repository
        // will be queried most.
        for (int y = (repositories.length - 1); y > 0; y--) {
            simulationRunIds[y] = simulationRunIds[y - 1];
            repositories[y] = repositories[y - 1];
        }

        simulationRunIds[0] = newRunIds;
        repositories[0] = repository;

        return repository;
    }

    public MainRepository getRepository(SimulatorEvent simEvent) {
        StepIdentifier step = simEvent.getStep();

        //Simulation may be terminated while getting the repository
        if (step != null) {
            long runId = step.getSimulationRunId();
            return getRepository(runId);
        }
        if (logger.isTraceEnabled()) {
            logger.trace(MainRepositoryManager.class.getSimpleName()
                         + " found no main repo for: " + simEvent);
        }
        return null;

    }

    public StepTraceRepository getCurrentRepository(String loggerName) {
        long currentSimId = SimulatorPlugin.getCurrent().getCurrentEnvironment()
                                           .getSimulator()
                                           .currentStepIdentifier()
                                           .getSimulationRunId();

        return getRepository(currentSimId).getLoggerRepository(loggerName, -1);
    }

    /**
     * This is just a {@link HashSet} subclass parameterized with the element
     * type {@link Long}.
     *
     * @author Lichael Duvigneau
     */
    private static class LongHashSet extends HashSet<Long> {

        /**
         * Create a LongHashSet with the given initial capacity.
         * @param size the initial capacity
         * @see HashSet#HashSet(int)
         */
        public LongHashSet(int size) {
            super(size);
        }

        /**
         * Create a new HashSet with initial contents copied from the given
         * array of <code>long</code> values. The initial set capacity matching
         * the given array
         *
         * @param entries  the initial values.
         */
        public LongHashSet(long[] entries) {
            this(entries.length);
            for (long entry : entries) {
                add(entry);
            }
        }
    }
}