package de.renew.net;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import java.util.Collections;


/**
 * I represent an uplink that can be inscribed to a transition.
 * I do not do anything, except remembering my name
 * and my argument expression.
 *
 * @author Olaf Kummer
 **/
public class UplinkInscription implements TransitionInscription {

    /**
     * My name.
     **/
    public String name;

    /**
     * My argument expression. Typically this is a tuple expression.
     **/
    public Expression params;

    /**
     * I (an uplink inscription) am created. I will store
     * the given name and expression
     * for later retrieval.
     *
     * @param name
     *   the name of the channel that I represent
     * @param params
     *   the expression that must match the expression
     *   of the respective uplink
     **/
    public UplinkInscription(String name, Expression params) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.name = name;
        this.params = params;
    }

    // Refactoring
    public int uplinkBeginLine;
    public int uplinkBeginColumn;
    public int uplinkEndLine;
    public int uplinkEndColumn;
    public int nameBeginLine;
    public int nameBeginColumn;
    public int nameEndLine;
    public int nameEndColumn;

    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return Collections.emptySet();
    }
}