/*
 * Created on Aug 2, 2005
 *
 */
package de.renew.fa.model;

import de.renew.fa.FADrawing;
import de.renew.fa.util.FAHelper;
import de.renew.fa.util.LetterNameComparator;
import de.renew.fa.util.StateNameComparator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


/**
 * @see de.renew.fa.model.FA
 * @author cabac
 *
 */
public class FAImpl implements FA {
    private HashMap<String, Letter> alphabet = new HashMap<String, Letter>();
    private HashMap<String, Arc> arcs = new HashMap<String, Arc>();
    private String name = "";
    private HashMap<String, State> states = new HashMap<String, State>();

    /**
     * Construct a new empty Finite Automata.
     */
    public FAImpl() {
        super();
    }

    /**
     * @param drawing
     */
    public FAImpl(FADrawing drawing) {
    }

    /**
     * @return a Vector of States that are end states.
     */
    @Override
    public Vector<State> endStates() {
        Iterator<State> it = getStates();
        Vector<State> endStates = new Vector<State>();
        while (it.hasNext()) {
            State state = it.next();
            if (state.isEndState()) {
                endStates.add(state);
            }
        }
        return endStates;
    }

    /**
     * @return Returns the sigma.
     */
    @Override
    public Iterator<Letter> getAlphabet() {
        Vector<Letter> values = new Vector<Letter>(alphabet.values());
        Collections.sort(values, new LetterNameComparator());
        return values.iterator();
    }

    /**
     * @return Returns the arcs as a <code>HashMap</code>.
     */
    @Override
    public Iterator<Arc> getArcs() {
        return arcs.values().iterator();
    }

    /**
     * @see de.renew.fa.model.FA#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param name
     * @return the state of the given name.
     */
    @Override
    public State getStateByName(String name) {
        return states.get(name);
    }

    /**
     * @return Returns the states.
     */
    @Override
    public Iterator<State> getStates() {
        Vector<State> stateList = new Vector<State>(states.values());
        Collections.sort(stateList, new StateNameComparator());
        return stateList.iterator();
    }

    /**
     * @return Returns the states.
     */
    @Override
    public State[] getStatesAsArray() {
        return states.values().toArray(new State[states.size()]);
    }


    /**
     * @param state
     * @param state2
     * @return true if an arc connects state and state2, false otherwise
     */
    @Override
    public boolean hasArc(String state, String state2) {
        Iterator<Arc> it = getArcs();
        while (it.hasNext()) {
            Arc arc = it.next();
            if (arc.getFrom().getName().equals(state)
                        && arc.getTo().getName().equals(state2)) {
                return true;
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.fa.model.FA#isLiteral(de.renew.fa.model.Word)
     */
    @Override
    public boolean isLiteral(Word word) {
        return alphabet.containsKey(word.getName());
    }

    /**
     * Adds a new arc to the fa.
     *
     * @param from -
     *            start state of the connection.
     * @param inscription -
     *            inscription of the connection.
     * @param to -
     *            end state of the connection.
     * @return the arc itself if successful, null otherwise.
     */
    @Override
    public Arc newArc(State from, Word inscription, State to) {
        if (from == null || inscription == null || to == null) {
            return null;
        }
        Arc newArc = new ArcImpl(from, inscription, to);
        arcs.put(newArc.getName(), newArc);
        return newArc;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.fa.model.FA#newLetter(java.lang.String)
     */
    @Override
    public Letter newLetter(String letterName) {
        Letter letter = new LetterImpl(letterName);
        if (!alphabet.containsKey(letterName)) {
            alphabet.put(letterName, letter);
            return letter;
        }
        return null;
    }

    /**
     * @param name - the name of the state
     * @return - the created State
     */
    @Override
    public State newState(String name) {
        if (states.containsKey(name)) {
            return null;
        }
        State state = new StateImpl(name);
        states.put(name, state);
        return state;
    }

    @Override
    public Word newWord(String name) {
        return new WordImpl(name);
    }

    /**
     * @param sigma
     *            The alphabet to set.
     */
    public void setAlphabet(Iterator<Letter> sigma) {
        while (sigma.hasNext()) {
            Letter letter = sigma.next();
            this.newLetter(letter.getName());
        }
    }

    //    /**
    //     * @param delta
    //     *            The delta to set.
    //     */
    //    public void setArcs(Collection arcs) {
    //    	Iterator it = arcs.iterator();
    //    	while (it.hasNext()) {
    //			Arc arc = (Arc) it.next();
    //			this.newArc(arc.);
    //		}
    //    }


    /**
     * @param name
     */
    @Override
    public boolean setAsEndState(String name) {
        if (states.containsKey(name)) {
            getStateByName(name).setEndState(true);
            return true;
        }
        return false;
    }

    /**
     * @param name
     */
    @Override
    public boolean setAsStartState(String name) {
        if (states.containsKey(name)) {
            getStateByName(name).setStartState(true);
            return true;
        }
        return false;
    }

    //
    //    /**
    //     * Takes the model of this FA and transforms it to a
    //     * Renew <code>FADrawing</code>.
    //     * However, layout is not considered yet.
    //     * @return - the renew drawing of this FA.
    //     */
    //    public Drawing[] toDrawings() {
    //        Drawing[] drawings = new Drawing[1];
    //        drawings[0] = FAHelper.convertModelToDrawing(this);
    //        return drawings;
    //    }


    /**
     * @see de.renew.fa.model.FA#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param states
     *            The states to set.
     */
    public void setStates(Iterator<State> states) {
        while (states.hasNext()) {
            State state = states.next();
            this.newState(state.getName());
        }
    }

    /**
     * @return a Vector of States that are start states.
     */
    @Override
    public Vector<State> startStates() {
        Iterator<State> it = getStates();
        Vector<State> startStates = new Vector<State>();
        while (it.hasNext()) {
            State state = it.next();
            if (state.isStartState()) {
                startStates.add(state);
            }
        }
        return startStates;
    }

    /**
     * Writes the FA in XFA format.
     */
    @Override
    public String toString() {
        return FAHelper.toProperties(this).toString();


        //        return "Z       = " + states.values().toString() + "\nSigma   = "
        //                + alphabet.values().toString() + "\nK       = "
        //                + arcs.values().toString() + "\nZ_Start = "
        //                + startStates().toString() + "\nZ_End   = "
        //                + endStates().toString();
    }

    /**
     * @see de.renew.fa.model.FA#numberOfStates()
     */
    @Override
    public int numberOfStates() {
        return states.values().size();
    }

    /**
     * @see de.renew.fa.model.FA#numberOfArcs()
     */
    @Override
    public int numberOfArcs() {
        return arcs.size();
    }

    /**
     * @see de.renew.fa.model.FA#numberOfLetters()
     */
    @Override
    public int numberOfLetters() {
        return alphabet.size();
    }
}