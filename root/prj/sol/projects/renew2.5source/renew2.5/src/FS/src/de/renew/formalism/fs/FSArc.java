package de.renew.formalism.fs;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.arc.Arc;

import java.util.Collection;
import java.util.Vector;


public class FSArc extends Arc {
    public FSArc(Place place, Transition transition, int arcType,
                 Expression tokenExpr, Expression timeExpr) {
        super(place, transition, arcType, tokenExpr, timeExpr);
    }

    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher) {
        Collection<Occurrence> coll = new Vector<Occurrence>();
        coll.add(new FSArcOccurrence(this, mapper, netInstance));
        return coll;
    }
}