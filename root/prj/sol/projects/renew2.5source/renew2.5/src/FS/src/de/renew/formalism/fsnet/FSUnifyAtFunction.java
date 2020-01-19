package de.renew.formalism.fsnet;

import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.Path;

import de.renew.expression.Function;

import de.renew.unify.Impossible;
import de.renew.unify.Tuple;


public class FSUnifyAtFunction implements Function {
    Path path;

    public FSUnifyAtFunction(Path path) {
        this.path = path;
    }

    public Object function(Object param) throws Impossible {
        //logger.debug("FSUnifyAtFunction of path "+path+" called with "+param);
        Tuple tuple = (Tuple) param;
        if (tuple.getArity() != 2) {
            throw new Impossible();
        }
        try {
            FeatureStructure fs1 = (FeatureStructure) tuple
                                       .getComponent(0);
            FeatureStructure fs2 = (FeatureStructure) tuple.getComponent(1);

            //logger.debug("Trying to unify "+fs1+" and "+fs2+" at "+path+"...");
            return fs1.unify(fs2, path);
        } catch (Exception e) {
            //logger.debug("Cannot unify "+tuple.getComponent(0)
            //		   +" with "+tuple.getComponent(1)+" at "+path);
            throw new Impossible("Cannot unify " + tuple.getComponent(0)
                                 + " with " + tuple.getComponent(1) + " at "
                                 + path);
        }
    }
}