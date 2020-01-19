package de.renew.formalism.java;

import de.renew.expression.ConstantExpression;
import de.renew.expression.Expression;

import de.renew.net.Place;
import de.renew.net.Transition;
import de.renew.net.arc.Arc;

import de.renew.shadow.SyntaxException;


public class SimpleArcFactory implements ArcFactory {
    private int arcType;
    private boolean allowsTime;

    public SimpleArcFactory(int arcType, boolean allowsTime) {
        this.arcType = arcType;
        this.allowsTime = allowsTime;
    }

    public void emptyArcCheck() {
    }

    public boolean allowsTime() {
        return allowsTime;
    }

    protected Arc getArc(Place place, Transition transition, int arcType,
                         Expression expr, Expression timeExpr) {
        return new Arc(place, transition, arcType, expr, timeExpr);
    }

    public void compileArc(Place place, Transition transition, boolean trace,
                           Class<?> placeType, TimedExpression timedExpr)
            throws SyntaxException {
        Expression expr;
        if (arcType == Arc.out) {
            expr = JavaNetHelper.makeCastedOutputExpression(placeType,
                                                            timedExpr
                       .getExpression());
        } else {
            expr = JavaNetHelper.makeCastedInputExpression(placeType,
                                                           timedExpr
                       .getExpression());
        }

        Expression timeExpr = null;
        if (allowsTime) {
            if (timedExpr.isTimed()) {
                timeExpr = timedExpr.getTime().getExpression();
            } else {
                timeExpr = ConstantExpression.doubleZeroExpression;
            }
        }

        Arc arc = getArc(place, transition, arcType, expr, timeExpr);
        arc.setTrace(trace);
        transition.add(arc);
    }
}