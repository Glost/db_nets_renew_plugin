package de.renew.engine.searcher;

import de.renew.net.TransitionInstance;

import de.renew.unify.Impossible;

import java.util.Collection;


/**
 * I represent a something that occurs, e.g. a transition
 * occurrence. Upon successful search, I allow executables
 * to be extracted.
 *
 * @author Olaf Kummer
 **/
public interface Occurrence {
    /**
     * Return the binders that allow this occurrence to act.
     * <p>
     * If this method throws an Impossible, then there
     * cannot be a suitable binding. Possible bindings made using
     * the state recorder are not rolled back, this is left to
     * the caller.
     * </p>
     *
     * @param searcher the sarcher that is currently evaluating
     *   the occurrence
     * @throws Impossible when the binders cannot be set up, implying
     *   that the occurrence cannot occur
     *
     * @see Binder
     **/
    Collection<Binder> makeBinders(Searcher searcher) throws Impossible;

    /**
     * I will provide executables that
     * perform the required actions according to the
     * current bindings of variables.
     *
     * All binders must have been processed before it is allowed
     * to call this method.
     *
     * @param variableMapperCopier
     *   a copier that can be used to rescue the current values of
     *   variables from backtracking, effectively duplicating
     *   variable mappers involved in the search process
     * @return an enumeration of executables
     *
     * @see Executable
     **/
    public Collection<Executable> makeExecutables(VariableMapperCopier variableMapperCopier);

    /**
     * Prepare a description of this occurrence that can later on be
     * queried for the actual description text.
     *
     * @return the occurrence description
     */
    public OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier variableMapperCopier);

    /**
     * Returns the transition instance to which this occurrence is related.
     *
     * @return the transitions instance
     */
    public TransitionInstance getTransition();
}