package de.renew.formalism.java;

import de.renew.expression.Function;

import de.renew.formalism.function.CastFunction;

import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.arc.FlexibleArc;

import de.renew.shadow.SyntaxException;

import de.renew.util.Types;


public class FlexibleInArcFactory implements ArcFactory {
    public static FlexibleInArcFactory INSTANCE = new FlexibleInArcFactory();

    private FlexibleInArcFactory() {
    }

    public void emptyArcCheck() throws SyntaxException {
        throw new SyntaxException("Flexible arcs must be inscribed.");
    }

    public boolean allowsTime() {
        return false;
    }

    public void compileArc(Place place, Transition transition, boolean trace,
                           Class<?> placeType, TimedExpression timedExpr)
            throws SyntaxException {
        TypedExpression typedExpr = timedExpr.getExpression();

        Function forwardFunction = null;
        Function backwardFunction = null;

        Class<?> exprType = typedExpr.getType();
        if (exprType != Types.UNTYPED) {
            if (exprType == null) {
                throw new SyntaxException("Null not allowed for flexible arcs.");
            } else if (!de.renew.unify.List.class.isAssignableFrom(exprType)
                               && !java.util.Collection.class.isAssignableFrom(exprType)
                               && !exprType.isArray()) {
                throw new SyntaxException("Incorrect type for flexible arc inscription.");
            }
        }

        if (placeType != Types.UNTYPED && exprType.isArray()) {
            // The place and the inscriptions and the individual elements
            // are typed. A conversion may be required.
            Class<?> elementType = exprType.getComponentType();

            if (!Types.allowsLosslessWidening(placeType, elementType)
                        && !Types.allowsLosslessWidening(elementType, placeType)) {
                throw new SyntaxException("Cannot losslessly convert "
                                          + JavaHelper.makeTypeErrorString(placeType)
                                          + " to "
                                          + JavaHelper.makeTypeErrorString(elementType)
                                          + " or vice versa.");
            }

            forwardFunction = new CastFunction(placeType);
            backwardFunction = new CastFunction(elementType);
        }

        FlexibleArc arc = new FlexibleArc(place, transition, FlexibleArc.in,
                                          typedExpr.getExpression(),
                                          forwardFunction, backwardFunction);
        arc.setTrace(trace);
        transition.add(arc);
    }
}