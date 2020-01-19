package de.renew.formalism.fs;

import de.renew.expression.Expression;

import de.renew.formalism.java.SimpleArcFactory;

import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.arc.Arc;


public class FSArcFactory extends SimpleArcFactory {
    public FSArcFactory(int arcType, boolean allowTime) {
        super(arcType, allowTime);
    }

    protected Arc getArc(Place place, Transition transition, int arcType,
                         Expression expr, Expression timeExpr) {
        return new FSArc(place, transition, arcType, expr, timeExpr);
    }
}