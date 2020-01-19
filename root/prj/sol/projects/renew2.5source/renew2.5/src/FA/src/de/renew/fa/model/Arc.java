/*
 * Created on Oct 4, 2005
 *
 */
package de.renew.fa.model;



/**
 * @author cabac
 *
 */
public interface Arc {

    /**
     * @return Returns the _from.
     */
    public abstract State getFrom();

    /**
     * @return Returns the inscription.
     */
    public abstract Word getInscription();

    /**
     * @return the name as string.
     */
    public abstract String getName();

    /**
     * @return Returns the _to.
     */
    public abstract State getTo();

    /**
     * @param inscription
     *            The inscription to set.
     */
    public abstract void setInscription(Word inscription);

    @Override
    public abstract String toString();
}