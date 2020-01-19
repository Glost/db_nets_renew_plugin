package de.renew.net.arc;

import de.renew.net.PlaceInstance;
import de.renew.net.TokenReserver;

import de.renew.unify.Variable;


public class InhibitorArcBinder extends ArcRemoveBinder {
    protected InhibitorArcBinder(Variable variable, PlaceInstance placeInstance) {
        super(variable, placeInstance);
    }

    protected boolean mayBind() {
        return true;
    }

    protected boolean possible(TokenReserver reserver, Object token) {
        return !placeInstance.containsTestableToken(token);
    }

    protected boolean remove(TokenReserver reserver, Object token) {
        return possible(reserver, token);
    }

    protected void unremove(TokenReserver reserver, Object token) {
        // Nothing to do. No tokens are moved.
    }
}