package de.uni_hamburg.fs;

import collections.CollectionEnumeration;


public class NullObject extends NoFeatureNode implements JavaType {

    /** The singleton NullObject wraps a java null value as a Type and
     *  at the same time as a node.
     */
    public final static NullObject INSTANCE = new NullObject();

    /** Construct a new wrapper for the given Java Object. */
    private NullObject() {
    }

    public Object getJavaObject() {
        return null;
    }

    public int hashCode() {
        return 0;
    }

    public boolean equals(Object that) {
        return that instanceof NullObject;
    }

    /** Return the name of this Type. */
    public String getName() {
        return "null";
    }

    /** Return the qualified name of this Type. */
    public String getFullName() {
        return "null";
    }

    public boolean isApprop(Name featureName) {
        return false;
    }

    public CollectionEnumeration appropFeatureNames() {
        return EmptyEnumeration.INSTANCE;
    }

    public Type appropType(Name featureName) {
        throw new NoSuchFeatureException(featureName, this);
    }

    /** Return whether this Type is extensional. */
    public boolean isExtensional() {
        return true;
    }

    /** Return whether this Type represents an instance. */
    public boolean isInstanceType() {
        return true;
    }

    public Type getInstanceType() {
        return this;
    }

    /** Return whether this Type subsumes <that> Type.
      * In other words, return wheter this Type is more general than <that> Type.
      */
    public boolean subsumes(Type that) {
        return equals(that);
    }

    /** Return the unification of this Type and <that> Type.
      * this Type is not modified!
      */
    public Type unify(Type that) throws UnificationFailure {
        // special case for other JavaObject:
        if (equals(that)) {
            return this;
        }
        return that.unify(this);
    }

    /** Return whether this Type and <that> Type are compatible.
      */
    public boolean canUnify(Type that) {
        if (equals(that)) {
            return true;
        }
        return that.canUnify(this);
    }

    /** Look for the most general common extensional supertype of this and <that>. */
    public Type mostGeneralExtensionalSupertype(Type that) {
        // TODO
        return null;
    }

    public Type getType() {
        return this;
    }

    public Node newNode() {
        return this;
    }

    public Node duplicate() {
        return this; // no need to clone null!
    }
}