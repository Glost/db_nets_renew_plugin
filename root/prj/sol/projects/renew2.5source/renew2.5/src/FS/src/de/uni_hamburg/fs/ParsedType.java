package de.uni_hamburg.fs;

public interface ParsedType extends java.io.Serializable {

    /** A ParsedType is a data structure for a type that has not yet
     *  been compiled into a "real" type. The method asType() does
     *  that and returns the "real" type.
     *  A ParsedType can also be "united" with another ParsedType,
     *  which is a pre-form of unification.
     */
    public final static ParsedType PARSED_TOP = new ParsedConjunctiveType(new ConceptSet());

    public ParsedType unite(ParsedType that) throws UnificationFailure;

    public Type asType() throws UnificationFailure;
}