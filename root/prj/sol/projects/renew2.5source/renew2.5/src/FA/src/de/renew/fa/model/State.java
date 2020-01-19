/*
 * Created on Oct 4, 2005
 *
 */
package de.renew.fa.model;



/**
 * @author cabac
 *
 */
public interface State {

    /**
     * @return the name as string
     */
    public abstract String getName();

    /**
     * @return Returns the endState.
     */
    public abstract boolean isEndState();

    /**
     * @return Returns the startState.
     */
    public abstract boolean isStartState();

    /**
     * @param endState
     *            The endState to set.
     */
    abstract void setEndState(boolean endState);

    /**
     * @param startState
     *            The startState to set.
     */
    abstract void setStartState(boolean startState);

    @Override
    public abstract String toString();
}