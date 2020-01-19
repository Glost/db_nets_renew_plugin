package de.renew.formalism.java;

import de.renew.net.Place;
import de.renew.net.Transition;

import de.renew.shadow.SyntaxException;


public interface ArcFactory {
    public void emptyArcCheck() throws SyntaxException;

    public boolean allowsTime();

    public void compileArc(Place place, Transition transition, boolean trace,
                           Class<?> placeType, TimedExpression typedExpr)
            throws SyntaxException;
}