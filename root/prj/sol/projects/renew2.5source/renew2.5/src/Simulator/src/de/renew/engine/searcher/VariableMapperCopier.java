package de.renew.engine.searcher;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.expression.VariableMapper;

import de.renew.unify.Copier;

import java.util.HashMap;
import java.util.Map;


/**
 * A copier for variable mapper provides copied versions
 * of variable mappers on demand. Each variable mapper
 * is copied exactly once using a copier parameterized at
 * creation time.
 */
public class VariableMapperCopier {
    private Copier copier;
    private Map<VariableMapper, VariableMapper> mappers = new HashMap<VariableMapper, VariableMapper>();

    /**
     * Create a new instance that uses the given copier to
     * copy variable mappers.
     *
     * @param copier the copier for copying variable mappers
     */
    public VariableMapperCopier(Copier copier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.copier = copier;
    }

    /**
     * Copy one variable mapper or retrieve the existing copy.
     *
     * @param mapper the mapper to copy
     * @return the copied mapper
     */
    public VariableMapper makeCopy(VariableMapper mapper) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        VariableMapper result = mappers.get(mapper);
        if (result == null) {
            result = mapper.makeCopy(copier);
            mappers.put(mapper, result);
        }
        return result;
    }

    /**
     * Return the copier used when copying variable mappers.
     */
    public Copier getCopier() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return copier;
    }
}