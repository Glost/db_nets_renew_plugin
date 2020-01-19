package de.renew.formalism.java;

import de.renew.expression.CallExpression;
import de.renew.expression.Expression;
import de.renew.expression.InvertibleExpression;

import de.renew.formalism.function.CastFunction;

import de.renew.shadow.SyntaxException;

import de.renew.util.Types;


public class JavaNetHelper {
    public static Expression makeCastedOutputExpression(Class<?> type,
                                                        TypedExpression expr)
            throws SyntaxException {
        if (type == Types.UNTYPED) {
            return expr.getExpression();
        } else if (expr.getType() == Types.UNTYPED) {
            throw new SyntaxException("Output arc expression for typed place must be typed.");
        } else if (type == expr.getType()) {
            return expr.getExpression();
        } else if (Types.allowsLosslessWidening(expr.getType(), type)) {
            if (type.isPrimitive()) {
                return new CallExpression(type, expr.getExpression(),
                                          new CastFunction(type));
            } else {
                return expr.getExpression();
            }
        } else {
            throw new SyntaxException("Cannot losslessly convert "
                                      + JavaHelper.makeTypeErrorString(expr
                                          .getType()) + " to "
                                      + JavaHelper.makeTypeErrorString(type)
                                      + ".");
        }
    }

    public static Expression makeCastedInputExpression(Class<?> type,
                                                       TypedExpression expr)
            throws SyntaxException {
        if (type == Types.UNTYPED) {
            // We must make sure not to feed illegal
            // values into the equation.
            return JavaHelper.makeGuardedExpression(expr);
        } else if (expr.getType() == Types.UNTYPED) {
            // The expression will take all values, if we feed them
            // into it. If the expression results in illegal values,
            // there will simply be no appropriate tokens in the place.
            return expr.getExpression();
        } else if (type == expr.getType()) {
            return expr.getExpression();
        } else if (Types.allowsLosslessWidening(type, expr.getType())) {
            if (type.isPrimitive()) {
                // Although a lossless conversion to the place type
                // is not always possible, we use the same construction
                // as in lossless casts. The cast will automatically fail
                // if it is not lossless, because forward and backward
                // casts together will result in a contradiction.
                return new InvertibleExpression(type, expr.getExpression(),
                                                new CastFunction(type),
                                                new CastFunction(expr.getType()));
            } else {
                return expr.getExpression();
            }
        } else if (Types.allowsLosslessWidening(expr.getType(), type)) {
            if (type.isPrimitive()) {
                // We use the same construction as in lossless casts.
                return new InvertibleExpression(type, expr.getExpression(),
                                                new CastFunction(type),
                                                new CastFunction(expr.getType()));
            } else {
                return JavaHelper.makeGuardedExpression(expr);
            }
        } else {
            throw new SyntaxException("Cannot losslessly convert "
                                      + JavaHelper.makeTypeErrorString(expr
                                          .getType()) + " to "
                                      + JavaHelper.makeTypeErrorString(type)
                                      + " or vice versa.");
        }
    }
}