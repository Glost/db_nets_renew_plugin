package de.uni_hamburg.fs;

import collections.CollectionEnumeration;
import collections.HashedMap;
import collections.UpdatableMap;


public class Subsumption {
    private UpdatableMap h = new HashedMap();

    private Subsumption() {
    }

    public static boolean subsumes(FeatureStructure thiz, FeatureStructure that) {
        return new Subsumption().subsumes(thiz.getRoot(), that.getRoot());
    }

    private boolean subsumes(Node thiz, Node that) {
        if (h.includesKey(thiz)) {
            return h.at(thiz).equals(that);
        }
        if (!thiz.getType().subsumes(that.getType())) {
            return false;
        }
        if (thiz instanceof JavaObject) {
            // if this java object subsumes that, that must in fact be
            // an equal java object: no need to proceed.
            return true;
        }

        h.putAt(thiz, that);
        CollectionEnumeration features = thiz.featureNames();
        while (features.hasMoreElements()) {
            Name featureName = (Name) features.nextElement();
            Node thispost = thiz.delta(featureName);
            if (that.hasFeature(featureName)) {
                Node thatpost = that.delta(featureName);
                if (!subsumes(thispost, thatpost)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }
}