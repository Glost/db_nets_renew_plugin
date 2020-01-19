package de.renew.engine.searcher;



/**
 * Stores a set of state changes (deltas) for the currently evaluated
 * binding versus the global state. This is a tagging interface
 * to facilitate a type safe collection.
 */
public interface DeltaSet {

    /**
     * Compute and return the earliest time at which the deltas
     * stored in this delta set can be realized. (They must also
     * be realizable at any later moment as per the restrictions
     * of out time model.)
     *
     * @return the earliest time
     */
    double computeEarliestTime();
}