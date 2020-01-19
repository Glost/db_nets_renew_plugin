package de.renew.net;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;

import de.renew.expression.VariableMapper;

import de.renew.unify.Impossible;

import java.io.Serializable;


/**
 * This interface tags objects that may be inscribed to a transition.
 *
 * @author Olaf Kummer
 **/
public interface TransitionInscription extends Serializable {
    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher)
            throws Impossible;
}