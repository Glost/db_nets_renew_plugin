package de.uni_hamburg.fs;

import collections.CollectionEnumeration;


public interface Concept extends java.io.Serializable {

    /** Return the name of this Concept. */
    public String getName();

    /** Return the name of the namespace this Concept belongs to. */
    public String getNamespace();

    /** Return the full name of this Concept in the form
     * namespace::name.
     */
    public String getFullName();

    /** Return whether this Concept is extensional. */
    public boolean isExtensional();

    /** Return whether the feature <feature> is appropriate in this Concept.
     */
    public boolean isApprop(Name featureName);

    /** Return the required ParsedType for the given feature. */
    public ParsedType appropParsedType(Name featureName)
            throws NoSuchFeatureException;

    /** Return the required Type for the Value under the given feature. */
    public Type appropType(Name featureName) throws NoSuchFeatureException;

    /** Return an ordered Enumeration of all appropriate features.
     *  A non-restrictive Concept returns only an Enumeration of the
     *  FeatureNames for which it defines restricted Types (since it
     *  cannot return an Enumeration of all possible FeatureNames).
     */
    public CollectionEnumeration appropFeatureNames();

    /** Return whether this Concept is-a <that> Concept.
      * In other words, return wheter this Concept is more special than <that>
      * Concept.
      */
    public boolean isa(Concept that);

    /** Return whether this Concept is-not-a <that> Concept.
      * In other words, return wheter this Concept is incompatible with <that>
      * Concept.
      * This method should only be called for concepts which do not stand
      * in a subsumtion relation.
      */
    public boolean isNotA(Concept that);

    /** Given a compatible concept <that>, returns a concept more special
     *  than the type {this,that} or null if this type is the unification.
     */


    //  public Concept unify(Concept that);
    public ConceptEnumeration extensionalSuperconcepts();
}