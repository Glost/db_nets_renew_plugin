/*
 * Created on Aug 3, 2004
 *
 */
package de.renew.engine.searcher;

import de.renew.net.TransitionInstance;

import de.renew.unify.Impossible;

import java.util.Collection;


/**
 * Abstract occurrence class where new occurrence classes can be
 * derived from.
 *
 * @author Sven Offermann
 */
public abstract class AbstractOccurrence implements Occurrence {
    private TransitionInstance tInstance = null;

    /**
     * Creates a new Occurrence related to the given transition instance.
     *
     * @param tInstance
     *   the transition instance
     */
    protected AbstractOccurrence(TransitionInstance tInstance) {
        this.tInstance = tInstance;
    }

    /**
     * @see de.renew.engine.searcher.Occurrence#getTransition()
     */
    public TransitionInstance getTransition() {
        return this.tInstance;
    }

    /**
     * @see de.renew.engine.searcher.Occurrence#makeBinders(de.renew.engine.searcher.Searcher)
     */
    public abstract Collection<Binder> makeBinders(Searcher searcher)
            throws Impossible;

    /**
     * @see de.renew.engine.searcher.Occurrence#makeExecutables(de.renew.engine.searcher.VariableMapperCopier)
     */
    public abstract Collection<Executable> makeExecutables(VariableMapperCopier variableMapperCopier);

    /**
     * @see de.renew.engine.searcher.Occurrence#makeOccurrenceDescription(de.renew.engine.searcher.VariableMapperCopier)
     */
    public abstract OccurrenceDescription makeOccurrenceDescription(VariableMapperCopier variableMapperCopier);
}