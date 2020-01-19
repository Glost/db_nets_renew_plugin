package de.renew.net.arc;

import de.renew.engine.searcher.Binder;
import de.renew.engine.searcher.BindingBadness;
import de.renew.engine.searcher.Searcher;

import de.renew.net.PlaceInstance;
import de.renew.net.TokenReserver;

import de.renew.unify.Unify;
import de.renew.unify.Variable;


abstract class ArcRemoveBinder implements Binder {
    Variable variable;
    PlaceInstance placeInstance;

    protected ArcRemoveBinder(Variable variable, PlaceInstance placeInstance) {
        this.variable = variable;
        this.placeInstance = placeInstance;
    }

    abstract boolean mayBind();

    abstract protected boolean possible(TokenReserver reserver, Object token);

    abstract boolean remove(TokenReserver reserver, Object token);

    abstract void unremove(TokenReserver reserver, Object token);

    protected PlaceInstance getPlaceInstance() {
        return placeInstance;
    }

    public int bindingBadness(Searcher searcher) {
        // The possibly incomplete value in the variable
        // will not be stored anywhere, so that it is
        // not required to make a copy of it.
        if (!Unify.isBound(variable) || !mayBind()) {
            // We must not try to bind the variable.
            return BindingBadness.max;
        } else if (possible(TokenReserver.getInstance(searcher),
                                    variable.getValue())) {
            return 1;
        } else {
            return 0;
        }
    }

    public void bind(Searcher searcher) {
        // Make sure that the place instance notifies
        // the searchable if its marking changes,
        // because in that case the possible binding would have
        // to be rechecked.
        searcher.insertTriggerable(placeInstance.triggerables());


        // Here we must make a copy of the value, because
        // the current value might be rolled back.
        // However, the new value must be created
        // without the possibility for rollback.
        // The value is completely bound anyway, so there
        // won't be a problem.
        Object value = Unify.copyBoundValue(variable.getValue());
        TokenReserver tokenReserver = TokenReserver.getInstance(searcher);
        if (remove(tokenReserver, value)) {
            searcher.search();
            unremove(tokenReserver, value);
        }
    }
}