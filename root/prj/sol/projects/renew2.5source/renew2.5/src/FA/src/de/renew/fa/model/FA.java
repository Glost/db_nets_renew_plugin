/*
 * Created on Oct 4, 2005
 *
 */
package de.renew.fa.model;

import java.util.Iterator;
import java.util.Vector;


/**
 * Model of a finite automaton.
 * A finite automata consists in a set of states, a set of letters (called alphabet),
 * a set of arcs that are connecting states.
 * States are identified by their name.
 * Some of the states can be start states or end states.
 * Arcs are directed and identified by their "from" state, their "to" state and
 * the inscription. In general the inscriptions on arcs  can be words. These words
 * determine the part that is read from the input while changing from one state to
 * another. To be able to change states the words have to be read from the input and
 * each letter has to be in the alphabet.
 *
 * Letters are considered to be singel character strings.
 *
 * An implementing class should function as a factory for all other elements of the
 * finite automaton, thus it can be  ensured that that States cannot be duplicates or
 * misssing.
 *
 * @see de.renew.fa.model.Letter
 * @see de.renew.fa.model.Word
 * @see de.renew.fa.model.State
 * @see de.renew.fa.model.Arc
 *
 * @author cabac
 *
 */
public interface FA {

    /**
     * @return The <code>Vector</code> of States that are end states.
     */
    public abstract Vector<State> endStates();

    /**
     * @return Returns the alphabet.
     */
    public abstract Iterator<Letter> getAlphabet();

    /**
     * @return Returns the arcs as a <code>HashMap</code>.
     */
    public abstract Iterator<Arc> getArcs();

    /**
     * @return The name of the fa model.
     */
    public String getName();

    /**
     * @param name -  Identifier of the State.
     * @return the state of the given name.
     */
    public abstract State getStateByName(String name);

    /**
     * @return Returns the states.
     */
    public abstract Iterator<State> getStates();

    /**
     * Determines if an arc between two given states exists.
     * With respect to the direction of the arc.
     * @param from - One state.
     * @param to   - Another state
     * @return True, if an arc btween the two states exists.
     */
    public abstract boolean hasArc(String from, String to);


    /**
     * Determines if a given word is a literal, i.e. element of the alphabet.
     *
     * @param word - Given word.
     * @return True, if the word is only one lette and is in the alphabet.
     */
    public boolean isLiteral(Word word);

    /**
     * Adds a new arc to the fa by creation.
     *
     * @param from -
     *            start state of the connection.
     * @param inscription -
     *            inscription of the connection.
     * @param to -
     *            end state of the connection.
     * @return the arc itself if successful, null otherwise.
     */
    public abstract Arc newArc(State from, Word inscription, State to);

    /**
     * Adds a new letter to the fa by creation, if the letter is not already in the alphabet.
     * @param letterName - The name of the letter.
     * @return The created letter or <code>null</code> if letter already exists.
     */
    public abstract Letter newLetter(String letterName);

    /**
     * Adds a new state to the fa by creation. Duplicate stats are ignored.
     * @param name - The identifier of the state.
     * @return The created state or <code>null</code> if state already exists.
     */
    public abstract State newState(String name);

    /**
     * Creates a new word, without adding it to any list of word.
     * @param name - The identifier of the word, i.e. the word.
     * @return The created <code>Word</code>.
     */
    public abstract Word newWord(String name);

    /**
     * Sets the end state property of a State identified by its name.
     *
     * @param name - Identifier of the state.
     */
    public abstract boolean setAsEndState(String name);

    /**
     * Sets the start state property of a State identified by its name.
     *
     * @param name - Identifier of the state.
     */
    public abstract boolean setAsStartState(String name);

    /**
     * Sets the name for the fa.
     * @param name - Given name.
     */
    void setName(String name);

    /**
     * @return The Vector of States that are start states.
     */
    public abstract Vector<State> startStates();

    /**
     * @return number of states in this fa.
     */
    public abstract int numberOfStates();

    /**
     * @return number of arcs of this fa.
     */
    public abstract int numberOfArcs();


    /**
     * @return size of alphabet.
     */
    public abstract int numberOfLetters();

    public abstract State[] getStatesAsArray();
}