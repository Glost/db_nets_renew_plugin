package de.uni_hamburg.fs;

import collections.CollectionEnumeration;


public interface Type extends java.io.Serializable {

    /** The most general Type. */
    public static final ConjunctiveType TOP = new ConjunctiveType(true);

    /** The most general ANY-Type. */
    public static final ConjunctiveType ANY = new ConjunctiveType(false);

    /** Anonymous (dummy) ANY-Type. */
    public static final ConjunctiveType NONE = new ConjunctiveType(false, false);

    /** Return the name of this Type. */
    public String getName();

    /** Return the fully qualified name of this Type. */
    public String getFullName();

    /** Return whether this Type is extensional. */
    public boolean isExtensional();

    /** Return whether this Type represents an instance. */
    public boolean isInstanceType();

    /** Return the instantiated version of this Type. */
    public Type getInstanceType();

    /** Return whether this Type restricts which features are allowed.
     *  A non-restricted Type is appropriate for every feature.
     */


    // public boolean isRestricted();


    /** Return whether the feature <feature> is appropriate in this Type. */
    public boolean isApprop(Name featureName);

    /** Return the required Type of the Value found under the given feature.
      * The feature has to be appropriate for this Type.
      */
    public Type appropType(Name featureName) throws NoSuchFeatureException;

    /** Return an Enumeration of all appropriate features. */
    public CollectionEnumeration appropFeatureNames();

    /** Return whether this Type subsumes <that> Type.
      * In other words, return wheter this Type is more general than <that> Type.
      */
    public boolean subsumes(Type that);

    /** Return the unification of this Type and <that> Type.
      * this Type is not modified!
      */
    public Type unify(Type that) throws UnificationFailure;

    /** Return whether this Type and <that> Type are compatible.
      */
    public boolean canUnify(Type that);

    /** Look for the most general common extensional supertype of this and <that>. */
    public Type mostGeneralExtensionalSupertype(Type that);

    /** Return a new node from this type.
     */
    public Node newNode();

    /** Return an Enumeration of all Concepts this Type is
     *   composed of.
     */


    //  public ConceptEnumeration concepts();
    // should be unnecessary, Renew bug has to be fixed:
    public boolean equals(Object that);
}