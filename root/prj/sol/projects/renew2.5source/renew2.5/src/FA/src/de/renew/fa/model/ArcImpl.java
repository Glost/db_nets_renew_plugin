/*
 * Created on Aug 1, 2005
 *
 */
package de.renew.fa.model;

import de.renew.fa.util.FAHelper;


/**
 * @author cabac
 *
 */
public class ArcImpl implements Arc {
    private State _from;
    private Word _inscription;
    private String _name;
    private State _to;

    /**
     *
     */
    public ArcImpl() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ArcImpl(State from, Word inscription, State to) {
        _name = FAHelper.getArcName(inscription, from, to);
        _from = from;
        _inscription = inscription;
        _to = to;

    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Arc) {
            Arc arc = (Arc) obj;
            if (getFrom().equals(arc.getFrom()) && getTo().equals(arc.getTo())
                        && getInscription().equals(arc.getInscription())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return Returns the _from.
     */
    @Override
    public State getFrom() {
        return _from;
    }

    /**
     * @return Returns the inscription.
     */
    @Override
    public Word getInscription() {
        return _inscription;
    }

    /**
     * @return the name as string.
     */
    @Override
    public String getName() {
        return _name;
    }

    /**
     * @return Returns the _to.
     */
    @Override
    public State getTo() {
        return _to;
    }

    /**
     * @param inscription
     *            The inscription to set.
     */
    @Override
    public void setInscription(Word inscription) {
        this._inscription = inscription;
    }

    @Override
    public String toString() {
        return _name;
    }

    @Override
    public int hashCode() {
        return getInscription().hashCode() ^ getTo().hashCode()
               ^ getFrom().hashCode();
    }
}