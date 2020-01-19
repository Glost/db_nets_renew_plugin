package de.uni_hamburg.fs;

import collections.CollectionEnumeration;
import collections.HashedMap;
import collections.UpdatableMap;


public class Equality {
    private UpdatableMap lr = new HashedMap();
    private UpdatableMap rl = new HashedMap();

    private Equality() {
    }

    public static boolean equals(FeatureStructure thiz, FeatureStructure that) {
        return new Equality().equals(thiz.getRoot(), that.getRoot());
    }

    private boolean equals(Node thiz, Node that) {
        if (thiz.equals(that)) {
            return true;
        }

        // same type?
        if (!thiz.getType().equals(that.getType())) {
            return false;
        }

        // same co-references?
        if (lr.includesKey(thiz)) {
            return lr.at(thiz).equals(that);
        }
        if (rl.includesKey(that)) {
            return rl.at(that).equals(thiz);
        }
        lr.putAt(thiz, that);
        rl.putAt(that, thiz);
        CollectionEnumeration features = thiz.featureNames();

        // same number of features?
        if (that.featureNames().numberOfRemainingElements() != features
                        .numberOfRemainingElements()) {
            return false;
        }
        while (features.hasMoreElements()) {
            Name featureName = (Name) features.nextElement();
            Node thispost = thiz.delta(featureName);

            // same feature?
            if (that.hasFeature(featureName)) {
                Node thatpost = that.delta(featureName);

                // same values for feature?
                if (!equals(thispost, thatpost)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }
}