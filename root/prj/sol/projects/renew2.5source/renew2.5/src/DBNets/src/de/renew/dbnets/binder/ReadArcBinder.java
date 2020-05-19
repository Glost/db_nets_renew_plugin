package de.renew.dbnets.binder;

import de.renew.engine.searcher.BindingBadness;
import de.renew.engine.searcher.Searcher;
import de.renew.net.PlaceInstance;
import de.renew.net.TokenReserver;
import de.renew.net.arc.InputArcBinder;
import de.renew.unify.Variable;

public class ReadArcBinder extends InputArcBinder {

    private boolean isBound = false;

    public ReadArcBinder(Variable variable, Variable delayVar, PlaceInstance placeInstance) {
        super(variable, delayVar, placeInstance);
    }

    @Override
    public int bindingBadness(Searcher searcher) {
        return isBound ? BindingBadness.max : 1;
    }

    @Override
    protected boolean mayBind() {
        return !isBound;
    }

    @Override
    protected boolean possible(TokenReserver reserver, Object token) {
        return true;
    }

    @Override
    protected boolean remove(TokenReserver reserver, Object token) {
        return true;
    }

    @Override
    public void bind(Searcher searcher) {
//        ViewPlaceInstance placeInstance = (ViewPlaceInstance) getPlaceInstance();
//
//        searcher.insertTriggerable(placeInstance.triggerables());

        // TODO: implement.

        if (!isBound) {
            isBound = true;

            searcher.search();
        }
    }
}
