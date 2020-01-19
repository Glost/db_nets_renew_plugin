package de.renew.formalism.efsnet;

import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.NoSuchFeatureException;

import de.renew.expression.Function;

import de.renew.unify.Impossible;


public class PlaceMarkingFunction implements Function {
    private static Object[] NONE = new Object[0];
    String placeName;

    public PlaceMarkingFunction(String placeName) {
        this.placeName = placeName;
    }

    public Object function(Object param) throws Impossible {
        if (param instanceof FeatureStructure) {
            FeatureStructure post = (FeatureStructure) param;
            Object result = null;
            try {
                result = post.at(placeName + ":val");
            } catch (NoSuchFeatureException nsf) {
            }
            if (result == null) {
                return NONE;
            } else {
                return new Object[] { result };
            }
        } else {
            throw new Impossible("Argument of PlaceMarkingFunction was not a Feature Structure!");
        }
    }
}