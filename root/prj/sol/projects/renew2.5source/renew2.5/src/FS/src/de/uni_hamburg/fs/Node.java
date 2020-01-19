package de.uni_hamburg.fs;

import collections.CollectionEnumeration;


/** The abstract class Node defines all methods that are needed
  * to construct, access and unify Feature Structures Nodes.
  * A Node has a Type and a number of Features that as a value
  * contain another Node of a Type appropriate for that Feature.
  */
public interface Node extends java.io.Serializable {

    /** Get the Type of the Node. */
    public Type getType();

    /** Return an Enumeration of the codes of all FeatureNames defined
     *  in this Node. */
    public CollectionEnumeration featureNames();

    /** Returns whether this Node has the Feature given by its Name. */
    public boolean hasFeature(Name featureName);

    /** Returns the Node at the Feature given by its Name. */
    public Node delta(Name featureName) throws NoSuchFeatureException;

    /** Returns the Node at the given Path.
     *  The empty path returns the Node itself.
     *  The exception is thrown if at any point, the feature given
     *  by the path does not exist in the current Node.
     */
    public Node delta(Path path) throws NoSuchFeatureException;

    /** Returns a one-level (not a deep) copy of this Node. */
    public Node duplicate();

    /** Sets the value of the feature with the given name.
      *  This method should only be called during construction of
      *  a Node and with a value of the correct type..
      */
    public void setFeature(Name featureName, Node newValue);
}