package de.uni_hamburg.fs;

import collections.CollectionEnumeration;

import de.renew.util.Types;


public abstract class JavaClassType implements JavaType, ParsedType {
    private Class<?> type;
    private boolean isPrimitive;

    protected JavaClassType() {
    }

    protected JavaClassType(Class<?> type) {
        setJavaClass(type);
    }

    void setJavaClass(Class<?> type) {
        isPrimitive = type.isPrimitive();
        if (isPrimitive) {
            this.type = Types.objectify(type);
        } else {
            this.type = type;
        }
    }

    public Class<?> getJavaClass() {
        if (isPrimitive) {
            return Types.typify(type);
        } else {
            return type;
        }
    }

    /** Return whether the feature <feature> is appropriate in this Type. */
    public boolean isApprop(Name featureName) {
        return false;
    }

    /** Return the required Type of the Value found under the given feature.
      * The feature has to be appropriate for this Type.
      */
    public Type appropType(Name featureName) throws NoSuchFeatureException {
        throw new NoSuchFeatureException(featureName, this);
    }

    /** Return an Enumeration of all appropriate features. */
    public CollectionEnumeration appropFeatureNames() {
        return EmptyEnumeration.INSTANCE;
    }

    /** Return a new node from this type.
     */
    public Node newNode() {
        return new NoFeatureNode(this);
    }

    public String toString() {
        return getName();
    }

    /**
     * Compares this <code>JavaClassType</code> object to the specified
     * <code>Object</code>.
     * <p>
     * TODO: It seems that this implementation is overridden by all known
     * subclasses. Does this equals/hashCode pair neverlethess define a
     * valid equivalence relation?
     * </p>
     * @param that the <code>Object</code> to compare.
     * @return <code>true</code> if the argument is a <code>JavaClassType</code>
     * instance and encapsulates the identical Java type as this instance.
     **/
    public boolean equals(Object that) {
        if (that instanceof JavaClassType) {
            return ((JavaClassType) that).getJavaClass() == getJavaClass();
        }
        return false;
    }

    /**
     * Returns a hashcode for this <code>JavaClassType</code>.
     * <p>
     * TODO: It seems that this implementation is overridden by all known
     * subclasses. Does this equals/hashCode pair neverlethess define a
     * valid equivalence relation?
     * </p>
     * @return a hashcode value for this <code>JavaClassType</code>.
     **/
    public int hashCode() {
        return getJavaClass().hashCode();
    }

    public ParsedType unite(ParsedType that) throws UnificationFailure {
        if (that.equals(ParsedType.PARSED_TOP)) {
            return this;
        }
        if (that instanceof BasicType) {
            return (BasicType) unify((BasicType) that);
        }
        throw new UnificationFailure();
    }

    public Type asType() throws UnificationFailure {
        return this;
    }
}