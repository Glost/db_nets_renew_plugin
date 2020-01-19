package de.renew.engine.simulator;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.Finder;
import de.renew.engine.searcher.Searcher;


public class ExecuteFinder implements Finder {
    private Binding binding;
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ExecuteFinder.class);

    public ExecuteFinder() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
    }

    public void found(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (binding != null) {
            throw new RuntimeException("ExecuteFinder was reused.");
        }
        binding = new Binding(searcher);
    }

    public boolean isCompleted() {
        return binding != null;
    }

    public boolean execute(final StepIdentifier stepIdentifier,
                           final boolean asynchronous) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        boolean result = binding.execute(stepIdentifier, asynchronous);
        binding = null;
        return result;

    }
}