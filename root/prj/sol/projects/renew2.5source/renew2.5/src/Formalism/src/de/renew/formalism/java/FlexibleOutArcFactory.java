package de.renew.formalism.java;

import de.renew.expression.Function;

import de.renew.formalism.function.CastFunction;

import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.arc.FlexibleArc;

import de.renew.shadow.SyntaxException;

import de.renew.util.Types;


public class FlexibleOutArcFactory implements ArcFactory {
    public static FlexibleOutArcFactory INSTANCE = new FlexibleOutArcFactory();

    private FlexibleOutArcFactory() {
    }

    public void emptyArcCheck() throws SyntaxException {
        throw new SyntaxException("Flexible arcs must be inscribed.");
    }

    public boolean allowsTime() {
        return true;
    }

    public void compileArc(Place place, Transition transition, boolean trace,
                           Class<?> placeType, TimedExpression timedExpr)
            throws SyntaxException {
        TypedExpression typedExpr = timedExpr.getExpression();

        Function conversionFunction = null;

        Class<?> exprType = typedExpr.getType();
        if (exprType != Types.UNTYPED) {
            if (exprType == null) {
                throw new SyntaxException("Null not allowed for flexible arcs.");
            } else if (de.renew.unify.List.class.isAssignableFrom(exprType)
                               || java.util.Enumeration.class.isAssignableFrom(exprType)
                               || java.util.Iterator.class.isAssignableFrom(exprType)
                               || java.util.Collection.class.isAssignableFrom(exprType)) {
                // Check for untyped output place.
                if (placeType != Types.UNTYPED) {
                    throw new SyntaxException("For non-array inscriptions the place must be untyped.");
                }
            } else if (!exprType.isArray()) {
                throw new SyntaxException("Incorrect type for flexible arc inscription.");
            }
        }

        if (placeType == Types.UNTYPED) {
            // Nothing to do. We have no indication that the
            // elements transported by the flexible arc must be converted
            // to a different type. No type checking is required.
        } else if (!typedExpr.isTyped()) {
            throw new SyntaxException("Output arc expression for typed place must be typed.");
        } else {
            // We already know that the expression is of array type.
            Class<?> elementType = exprType.getComponentType();

            if (!Types.allowsLosslessWidening(elementType, placeType)) {
                throw new SyntaxException("Cannot losslessly convert "
                                          + JavaHelper.makeTypeErrorString(elementType)
                                          + " to "
                                          + JavaHelper.makeTypeErrorString(placeType)
                                          + ".");
            }

            if (placeType.isPrimitive()) {
                conversionFunction = new CastFunction(placeType);
            }
        }

        FlexibleArc arc = new FlexibleArc(place, transition, FlexibleArc.out,
                                          typedExpr.getExpression(),
                                          conversionFunction, null);
        arc.setTrace(trace);
        transition.add(arc);
    }
}