package de.renew.formalism.fs;

import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.PlaceInstance;
import de.renew.net.arc.Arc;
import de.renew.net.arc.ArcOccurrence;
import de.renew.net.arc.InhibitorArcBinder;

import de.renew.unify.Variable;


public class FSArcOccurrence extends ArcOccurrence {
    public FSArcOccurrence(Arc arc, VariableMapper mapper,
                           NetInstance netInstance) {
        super(arc, mapper, netInstance);
    }

    protected InhibitorArcBinder getInhibitorArcBinder(Variable tokenVar,
                                                       PlaceInstance placeInstance) {
        return new FSInhibitorArcBinder(tokenVar, placeInstance);
    }
}