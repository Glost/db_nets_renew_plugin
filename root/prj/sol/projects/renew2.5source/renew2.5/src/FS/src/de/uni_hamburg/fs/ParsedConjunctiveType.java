package de.uni_hamburg.fs;

public class ParsedConjunctiveType implements ParsedType {
    private static Type STRING_TYPE = new BasicType(String.class);

    /** A ParsedType is a data structure for a type that has not yet
     *  been compiled into a "real" type. The method asType() does
     *  that and returns the "real" type.
     */
    private Type type = null; // cache
    private ConceptSet concepts = null;
    private boolean restricted = true;
    private boolean printAny = true;

    public ParsedConjunctiveType(Concept concept) {
        this(new ConceptSet(concept));
    }

    public ParsedConjunctiveType(ConceptSet concepts) {
        this.concepts = concepts;
    }

    public ParsedConjunctiveType(ConceptSet concepts, boolean restricted) {
        this(concepts);
        this.restricted = restricted;
    }

    public ParsedConjunctiveType(ConceptSet concepts, boolean restricted,
                                 boolean noDummyType) {
        this(concepts, noDummyType && restricted);
        this.printAny = noDummyType || !restricted;
    }

    public ParsedType unite(ParsedType that) throws UnificationFailure {
        if (that instanceof ParsedConjunctiveType) {
            ParsedConjunctiveType thatPCT = (ParsedConjunctiveType) that;
            ConceptSet united = new ConceptSet(concepts);
            united.unite(thatPCT.concepts);
            return new ParsedConjunctiveType(united,
                                             restricted && thatPCT.restricted,
                                             printAny || thatPCT.printAny);
        }
        throw new UnificationFailure();
    }

    public Type asType() throws UnificationFailure {
        if (type == null) {
            if (concepts.equals(new ConceptSet(TypeSystem.instance()
                                                                 .getJavaConcept(String.class)))
                        && restricted) {
                type = STRING_TYPE;
            } else {
                type = new ConjunctiveType(concepts, restricted, printAny);
            }
        }
        return type;
    }

    public boolean equals(Object that) {
        if (that instanceof ParsedConjunctiveType) {
            //logger.debug("Hashcodes: this.concepts: "+hashCode()
            //+" that.concepts: "+that.hashCode());
            ParsedConjunctiveType thatPT = (ParsedConjunctiveType) that;
            return restricted == thatPT.restricted
                   && printAny == thatPT.printAny
                   && concepts.equals(thatPT.concepts);
        }
        return false;
    }

    public int hashCode() {
        return concepts.hashCode() + (restricted ? 0 : 999);
    }

    public String toString() {
        return ConjunctiveType.typeToString(restricted, printAny, false,
                                            concepts);
    }
}