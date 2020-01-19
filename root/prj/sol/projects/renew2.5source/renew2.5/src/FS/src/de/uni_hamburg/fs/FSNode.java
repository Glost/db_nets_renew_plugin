package de.uni_hamburg.fs;

import collections.CollectionEnumeration;
import collections.Map;


public class FSNode extends AbstractNode {
    private static final boolean DEBUG = false;
    OrderedTable feats;

    public FSNode(Type nodetype, Map feats) {
        super(nodetype);
        this.feats = new OrderedTable(nodetype.appropFeatureNames());
        CollectionEnumeration features = feats.keys();
        while (features.hasMoreElements()) {
            Name feature = (Name) features.nextElement();
            setFeature(feature, (Node) feats.at(feature));
        }
    }

    public FSNode(Type nodetype) {
        super(nodetype);
        this.feats = new OrderedTable(nodetype.appropFeatureNames());
    }

    public FSNode() {
        this(Type.ANY);
    }

    public FSNode(String nodetype) throws UnificationFailure {
        this(ConjunctiveType.getType(nodetype));
    }

    public FSNode(String nodetype, OrderedTable feats)
            throws UnificationFailure, TypeException {
        this(ConjunctiveType.getType(nodetype), feats);
    }

    FSNode(Type nodetype, OrderedTable feats) {
        super(nodetype);
        this.feats = feats;
    }

    public CollectionEnumeration featureNames() {
        return feats.keys();
    }

    public boolean hasFeature(Name featureName) {
        return feats.includesKey(featureName);
    }

    public Node delta(Name featureName) throws NoSuchFeatureException {
        if (feats.includesKey(featureName)) {
            return (Node) feats.at(featureName);
        }
        if (nodetype.isApprop(featureName)) {
            return nodetype.appropType(featureName).newNode();
        }
        throw new NoSuchFeatureException(featureName, nodetype);
    }

    /** Sets the value of the feature with the given name.
      *  This method should only be called during construction of
      *  a Node and with a value of the correct type.
      */
    public void setFeature(Name featureName, Node value) {
        if (DEBUG) {
            //if (nodetype.isApprop(featureName) && nodetype.appropType(featureName).subsumes(value.getType())) {
            if (!(nodetype.isApprop(featureName)
                        && nodetype.appropType(featureName)
                                           .canUnify(value.getType()))) {
                throw new NoSuchFeatureException(featureName, nodetype);
            }
        }
        if (value == null) {
            feats.removeAt(featureName);
        } else {
            feats.putAt(featureName, value);
        }
    }

    public Node duplicate() {
        return new FSNode(nodetype, (OrderedTable) feats.duplicate());
    }
}