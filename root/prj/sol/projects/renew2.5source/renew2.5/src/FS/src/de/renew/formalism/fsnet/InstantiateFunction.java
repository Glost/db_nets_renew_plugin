package de.renew.formalism.fsnet;

import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.NotInstantiableException;

import de.renew.expression.Function;

import de.renew.unify.Impossible;


public class InstantiateFunction implements Function {
    public static final InstantiateFunction INSTANCE = new InstantiateFunction();

    private InstantiateFunction() {
    }

    public Object function(Object param) throws Impossible {
        if (param instanceof FeatureStructure) {
            FeatureStructure fs = (FeatureStructure) param;
            try {
                return fs.instantiate();
            } catch (NotInstantiableException nie) {
                throw new Impossible(nie.getMessage());
            }
        } else {
            return param;
        }
    }
}