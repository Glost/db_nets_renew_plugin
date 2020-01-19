package de.renew.engine.searcher;



/**
 * Currently implemented by TransitionInstance and
 * SynchronisationRequest within de.renew.
 **/
public interface Searchable extends Triggerable {

    /**
     * This method is called if somebody wants my descendant to start a
     * search for an activated binding. My descendant initiates the search and
     * lets the searcher do the rest.
     */
    public void startSearch(Searcher searcher);
}