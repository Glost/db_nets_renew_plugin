package de.renew.shadow;



/**
 * A preprocessor that is run during the compilation of
 * a net system. The preprocessor communicates with the
 * actual compiler through the shadow lookup, usually through
 * custom {@link ShadowLookupExtension shadow lookup extensions}.
 * <p>
 * Using a preprocessor results in an increased flexiblity,
 * because the compilation of a net may now access information
 * from other nets. However, a compiler that requires a
 * preprocessor cannot be used when loading nets on demand.
 */
public interface ShadowPreprocessor {

    /**
     * Set the shadow lookup that will be used for lookups during
     * compilation and that will receive the compilation results.
     *
     * @param shadowLookup the lookup
     */
    public void setShadowLookup(ShadowLookup shadowLookup);

    /**
     * Preprocess the given net system, assembling information in
     * the shadow lookup as needed.
     *
     * @param shadowNetSystem the net system
     */
    public void preprocess(ShadowNetSystem shadowNetSystem)
            throws SyntaxException;

    /**
      * Given the comment at {@link #equals(Object)}, you must
      * override the hash code method.
      */
    public int hashCode();

    /**
      * In addition to the contract of {@link Object#equals(Object)},
      * two ShadowNetPreprocessors are equals, if they perform the same
      * actions, disregarding the provided shadow lookup.
      */
    public boolean equals(Object that);
}