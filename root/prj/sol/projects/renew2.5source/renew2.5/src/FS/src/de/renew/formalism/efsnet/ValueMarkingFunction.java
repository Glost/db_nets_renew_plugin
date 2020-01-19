package de.renew.formalism.efsnet;

import collections.CollectionEnumeration;

import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.Name;
import de.uni_hamburg.fs.Path;
import de.uni_hamburg.fs.UnificationFailure;

import de.renew.expression.Function;

import de.renew.unify.Impossible;


public class ValueMarkingFunction implements Function {
    public static ValueMarkingFunction INSTANCE = new ValueMarkingFunction();

    private ValueMarkingFunction() {
    }

    public Object function(Object param) throws Impossible {
        if (param instanceof FeatureStructure) {
            return valmark((FeatureStructure) param);
        } else {
            throw new Impossible("Argument of ValueMarkingFunction was not a Feature Structure!");
        }
    }

    public static FeatureStructure valmark(FeatureStructure m) {
        // Re-build m by copying all substructures:
        FeatureStructure mval = new FeatureStructure(m.getType());
        CollectionEnumeration feats = m.featureNames();
        while (feats.hasMoreElements()) {
            Path featpath = new Path((Name) feats.nextElement());
            try {
                mval = mval.unify(m.at(featpath), featpath);
            } catch (UnificationFailure uff) {
                throw new RuntimeException("Internal Error in ValueMarkingFunction while unifying "
                                           + mval + " with " + m.at(featpath)
                                           + " at " + featpath + ":\n" + uff);
            }
        }
        return mval;
    }
}