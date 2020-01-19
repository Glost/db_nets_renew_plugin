package de.uni_hamburg.fs;

import collections.CollectionEnumeration;


public abstract class SingleConceptType implements Type, ParsedType {
    Concept concept;

    /** Return whether this Type is extensional. */
    public boolean isExtensional() {
        return concept.isExtensional();
    }

    public Concept getConcept() {
        return concept;
    }

    /** Return whether the feature <feature> is appropriate in this Type. */
    public boolean isApprop(Name featureName) {
        return concept.isApprop(featureName);
    }

    /** Return the required Type of the Value found under the given feature.
      * The feature has to be appropriate for this Type.
      */
    public Type appropType(Name featureName) throws NoSuchFeatureException {
        return concept.appropType(featureName);
    }

    /** Return an Enumeration of all appropriate features. */
    public CollectionEnumeration appropFeatureNames() {
        return concept.appropFeatureNames();
    }

    public String toString() {
        return getName();
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