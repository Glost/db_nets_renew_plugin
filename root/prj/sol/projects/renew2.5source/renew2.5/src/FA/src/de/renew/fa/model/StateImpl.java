/*
 * Created on Aug 1, 2005
 *
 */
package de.renew.fa.model;



/**
 * @author cabac
 *
 */
public class StateImpl implements State {
    private String _name;
    private boolean endState;
    private boolean startState;

    /**
     *
     */
    public StateImpl() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param token
     */
    public StateImpl(String token) {
        _name = token;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof State) {
            State state = (State) obj;
            return this.getName().equals(state.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }


    /**
     * @return the name as string.
     */
    @Override
    public String getName() {
        return _name;
    }

    /**
     * @return Returns the endState.
     */
    @Override
    public boolean isEndState() {
        return endState;
    }

    /**
     * @return Returns the startState.
     */
    @Override
    public boolean isStartState() {
        return startState;
    }

    /**
     * @param endState
     *            The endState to set.
     */
    @Override
    public void setEndState(boolean endState) {
        this.endState = endState;
    }

    /**
     * @param startState
     *            The startState to set.
     */
    @Override
    public void setStartState(boolean startState) {
        this.startState = startState;
    }

    @Override
    public String toString() {
        return "" + _name;
    }
}