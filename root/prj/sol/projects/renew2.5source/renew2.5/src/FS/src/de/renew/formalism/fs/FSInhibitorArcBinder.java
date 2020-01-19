package de.renew.formalism.fs;

import de.uni_hamburg.fs.FeatureStructure;

import de.renew.net.PlaceInstance;
import de.renew.net.TokenReserver;
import de.renew.net.arc.InhibitorArcBinder;

import de.renew.unify.Variable;

import java.util.Iterator;


public class FSInhibitorArcBinder extends InhibitorArcBinder {
    protected FSInhibitorArcBinder(Variable variable,
                                   PlaceInstance placeInstance) {
        super(variable, placeInstance);
    }

    protected boolean possible(TokenReserver reserver, Object token) {
        if (token instanceof FeatureStructure) {
            FeatureStructure fs = (FeatureStructure) token;
            Iterator<Object> tokens = getPlaceInstance()
                                          .getDistinctTestableTokens().iterator();
            while (tokens.hasNext()) {
                Object tok = tokens.next();
                if (tok instanceof FeatureStructure
                            && fs.canUnify((FeatureStructure) tok)) {
                    return false;
                }
            }
            return true;
        }
        return super.possible(reserver, token);
    }
}