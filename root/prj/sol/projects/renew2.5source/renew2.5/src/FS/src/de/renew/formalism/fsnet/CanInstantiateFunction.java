package de.renew.formalism.fsnet;

import de.uni_hamburg.fs.FeatureStructure;

import de.renew.expression.Function;

import de.renew.unify.Impossible;

import de.renew.util.Value;


public class CanInstantiateFunction implements Function {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(CanInstantiateFunction.class);
    public static final CanInstantiateFunction INSTANCE = new CanInstantiateFunction();

    private CanInstantiateFunction() {
    }

    public Object function(Object param) throws Impossible {
        boolean canInstantiate;
        if (param instanceof FeatureStructure) {
            canInstantiate = ((FeatureStructure) param).canInstantiate();
        } else {
            canInstantiate = true;
        }

        logger.debug("can instantiate " + param + ": " + canInstantiate);
        return new Value(new Boolean(canInstantiate));
    }
}