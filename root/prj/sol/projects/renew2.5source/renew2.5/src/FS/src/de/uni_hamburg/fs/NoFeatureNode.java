package de.uni_hamburg.fs;

import collections.CollectionEnumeration;


public class NoFeatureNode extends AbstractNode {
    //  private int hashCode;
    public NoFeatureNode(Type nodetype) {
        super(nodetype);


        //    if (nodetype.isExtensional())
        //      hashCode=3*nodetype.hashCode();
        //    else
        //      hashCode=super.hashCode();
    }

    public NoFeatureNode() {
        this(Type.TOP);
    }

    /*
      public int hashCode() {
        return hashCode;
      }

      public boolean equals(Object that) {
        if (that instanceof NoFeatureNode) {
          if (nodetype.isExtensional() &&
        nodetype.equals(((NoFeatureNode)that).nodetype))
          return true;
        }
        return super.equals(that);
      }
    */
    public CollectionEnumeration featureNames() {
        return EmptyEnumeration.INSTANCE;
    }

    public boolean hasFeature(Name featureName) {
        return false;
    }

    public Node delta(Name featureName) throws NoSuchFeatureException {
        throw new NoSuchFeatureException(featureName, nodetype);
    }

    /** Sets the value of the feature with the given name.
      *  This method should only be called during construction of
      *  a Node.
      */
    public void setFeature(Name featureName, Node value) {
        throw new NoSuchFeatureException(featureName, nodetype);
    }

    public Node duplicate() {
        if (nodetype.isExtensional()) {
            return this;
        }
        return new NoFeatureNode(nodetype);
    }
}