package de.renew.net.arc;

import de.renew.net.PlaceInstance;
import de.renew.net.TokenReserver;

import de.renew.unify.Variable;


public class TestArcBinder extends ArcRemoveBinder {
    protected TestArcBinder(Variable variable, PlaceInstance placeInstance) {
        super(variable, placeInstance);
    }

    protected boolean mayBind() {
        return true;
    }

    protected boolean possible(TokenReserver reserver, Object token) {
        return reserver.containsTestableToken(placeInstance, token);
    }

    protected boolean remove(TokenReserver reserver, Object token) {
        return reserver.testToken(placeInstance, token);
    }

    protected void unremove(TokenReserver reserver, Object token) {
        reserver.untestToken(placeInstance, token);
    }
}