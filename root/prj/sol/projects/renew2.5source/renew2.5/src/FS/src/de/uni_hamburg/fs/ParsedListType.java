package de.uni_hamburg.fs;

public class ParsedListType implements ParsedType {

    /** A ParsedType is a data structure for a type that has not yet
     *  been compiled into a "real" type. The method asType() does
     *  that and returns the "real" type.
     */
    private Type type = null; // cache
    private boolean atLeastOne;
    private ParsedType parsedType;

    public ParsedListType(boolean atLeastOne, ParsedType parsedType) {
        this.atLeastOne = atLeastOne;
        this.parsedType = parsedType;
    }

    public ParsedType unite(ParsedType that) throws UnificationFailure {
        if (that instanceof ParsedListType) {
            ParsedListType thatPLT = (ParsedListType) that;
            return new ParsedListType(atLeastOne || thatPLT.atLeastOne,
                                      parsedType.unite(thatPLT.parsedType));
        }
        throw new UnificationFailure();
    }

    public Type asType() throws UnificationFailure {
        if (type == null) {
            int subtype = atLeastOne ? ListType.NELIST : ListType.LIST;
            type = new ListType(parsedType.asType(), subtype);
        }
        return type;
    }

    public boolean equals(Object that) {
        if (that instanceof ParsedListType) {
            ParsedListType thatPLT = (ParsedListType) that;
            return atLeastOne == thatPLT.atLeastOne
                   && parsedType.equals(thatPLT.parsedType);
        }
        return false;
    }

    public int hashCode() {
        return (atLeastOne ? 1 : 0) + parsedType.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        final int sbSize = 1000;
        final String variableSeparator = ", ";
        final StringBuffer sb = new StringBuffer(sbSize);
        sb.append("ParsedListType(");
        sb.append("type=").append(type);
        sb.append(variableSeparator);
        sb.append("atLeastOne=").append(atLeastOne);
        sb.append(")");
        return sb.toString();
    }
}