package de.renew.dbnets.binder;

import de.renew.engine.searcher.Searcher;
import de.renew.net.PlaceInstance;
import de.renew.net.TokenReserver;
import de.renew.net.ViewPlaceInstance;
import de.renew.net.arc.InputArcBinder;
import de.renew.unify.Variable;

public class ReadArcBinder extends InputArcBinder {

    protected ReadArcBinder(Variable variable, Variable delayVar, PlaceInstance placeInstance) {
        super(variable, delayVar, placeInstance);
    }

    @Override
    protected boolean mayBind() {
        return true;
    }

    @Override
    protected boolean possible(TokenReserver reserver, Object token) {
        return true;
    }

    @Override
    public void bind(Searcher searcher) {
        ViewPlaceInstance placeInstance = (ViewPlaceInstance) getPlaceInstance();

        searcher.insertTriggerable(placeInstance.triggerables());

        // TODO: implement.
    }
}
