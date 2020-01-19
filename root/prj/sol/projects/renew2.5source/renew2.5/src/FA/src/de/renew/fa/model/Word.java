/*
 * Created on Oct 4, 2005
 *
 */
package de.renew.fa.model;



/**
 * @author cabac
 *
 */
public interface Word {

    /**
     * @return  the word as String
     */
    public abstract String getName();

    /**
     * @return true if Word is the empty word (lambda).
     */
    public boolean isEmpty();

    @Override
    public abstract String toString();
}