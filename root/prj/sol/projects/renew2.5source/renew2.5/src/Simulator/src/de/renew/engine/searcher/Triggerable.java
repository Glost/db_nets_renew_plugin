package de.renew.engine.searcher;



/**
 * Currently implemented by TransitionInstance and
 * SynchronisationRequest within de.renew.
 **/
public interface Triggerable {

    /**
     * Return the set of all triggers.
     **/
    public TriggerCollection triggers();

    /**
     * Trigger a new search because bindings might have appeared of
     * disappeared.
     **/
    public void proposeSearch();
}