package de.renew.net.arc;

import de.renew.net.PlaceInstance;
import de.renew.net.TokenReserver;

import de.renew.unify.Unify;
import de.renew.unify.Variable;

import de.renew.util.Value;


public class InputArcBinder extends ArcRemoveBinder {
    Variable delayVar;

    protected InputArcBinder(Variable variable, Variable delayVar,
                             PlaceInstance placeInstance) {
        super(variable, placeInstance);
        this.delayVar = delayVar;
    }

    private double getDelay() {
        Object timeObj = delayVar.getValue();
        if (timeObj instanceof Value) {
            timeObj = ((Value) timeObj).value;
        }
        if (timeObj instanceof Number) {
            // For input arcs a positive delay forces earlier tokens.
            return ((Number) timeObj).doubleValue();
        } else {
            // Sorry, no such token.
            return Double.POSITIVE_INFINITY;
        }
    }

    protected boolean mayBind() {
        return Unify.isBound(delayVar);
    }

    protected boolean possible(TokenReserver reserver, Object token) {
        double delay = getDelay();
        return delay != Double.POSITIVE_INFINITY
               && reserver.containsRemovableToken(placeInstance, token, delay);
    }

    protected boolean remove(TokenReserver reserver, Object token) {
        double delay = getDelay();
        return delay != Double.POSITIVE_INFINITY
               && reserver.removeToken(placeInstance, token, delay);
    }

    protected void unremove(TokenReserver reserver, Object token) {
        reserver.unremoveToken(placeInstance, token, getDelay());
    }
}