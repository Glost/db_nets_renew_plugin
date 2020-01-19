package de.renew.engine.searcher;



/**
 * An intermediate object that allows the lazy computation
 * of occurrence descriptions.
 */
public interface OccurrenceDescription {

    /**
     * Return the actual description of an occurrence as a string.
     * This method may return <var>null</var>, indicating that
     * the occurrence is not directly instpectable by the user.
     *
     * @return the actual description text
     */
    public String getDescription();
}