package de.renew.engine.searcher;



/**
 * A delta set factory is responsible for creating delta sets
 * and for identifying the category of the created delta sets.
 */
public interface DeltaSetFactory {

    /**
     * Return the category handled by this factory.
     **/
    public String getCategory();

    /**
     * Create one delta set instance as appropriate for this factory.
     * @return the created delta set
     */
    public DeltaSet createDeltaSet();
}