package de.uni_hamburg.fs;

import collections.ArrayEnumeration;
import collections.CollectionEnumeration;


public class ListType implements Type {
    public static final int LIST = 0;
    public static final int NELIST = 1;
    public static final int ELIST = 2;

    // public static final String LIST_TYPE[]={"List","NEList","EList"};
    public static final Name HEAD = new Name("hd");
    public static final Name TAIL = new Name("tl");
    private Type baseType = null;
    private int subtype;

    public ListType(Type baseType, int subtype) {
        this.baseType = baseType;
        this.subtype = subtype;
    }

    public ListType(Type baseType) {
        this(baseType, LIST);
    }

    public static ListType getList(Type baseType) {
        return new ListType(baseType, LIST);
    }

    public static ListType getEList(Type baseType) {
        return new ListType(baseType, ELIST);
    }

    public static ListType getNEList(Type baseType) {
        return new ListType(baseType, NELIST);
    }

    public Type getBaseType() {
        return baseType;
    }

    public int getSubtype() {
        return subtype;
    }

    /** Return the name of this Type. */
    public String getName() {
        return getName(getBaseType().getName());
    }

    /** Return the qualified name of this Type. */
    public String getFullName() {
        return getName(getBaseType().getFullName());
    }

    private String getName(String basename) {
        switch (subtype) {
        case ELIST:
            return "<" + basename + ">";
        case NELIST:
            return basename + "+";
        case LIST:
            return basename + "*";
        default:
            return "!!!wrong list type!!!";
        }
    }

    /** Return whether this Type is extensional. */
    public boolean isExtensional() {
        return subtype != LIST;
    }

    /** Return whether this Type represents an instance. */
    public boolean isInstanceType() {
        return getBaseType().isInstanceType();
    }

    public Type getInstanceType() {
        if (isInstanceType()) {
            return this;
        }
        return new ListType(getBaseType().getInstanceType(), subtype);
    }

    /** Return whether the feature <feature> is appropriate in this Concept.
     */
    public boolean isApprop(Name feature) {
        return subtype == NELIST
               && (feature.equals(HEAD) || feature.equals(TAIL));
    }

    /** Return the required Type for the Value under the given feature. */
    public Type appropType(Name feature) throws NoSuchFeatureException {
        if (subtype == NELIST) {
            if (feature.equals(HEAD)) {
                return getBaseType();
            } else if (feature.equals(TAIL)) {
                return new ListType(getBaseType(), LIST);
            }
        }
        throw new NoSuchFeatureException(feature, this);
    }

    /** Return an Enumeration of all appropriate features. */
    public CollectionEnumeration appropFeatureNames() {
        if (subtype == NELIST) {
            return new ArrayEnumeration(new Name[] { HEAD, TAIL });
        } else {
            return EmptyEnumeration.INSTANCE;
        }
    }

    /** Return whether this Type subsumes <that> Type.
      * In other words, return wheter this Type is more general than <that> Type.
      */
    public boolean subsumes(Type that) {
        if (that instanceof ListType) {
            ListType thatList = (ListType) that;
            return (subtype == LIST || subtype == thatList.subtype)
                   && getBaseType().subsumes(thatList.getBaseType());
        }
        return false;
    }

    /** Return the unification of this Type and <that> Type.
      * this Type is not modified!
      */
    public Type unify(Type that) throws UnificationFailure {
        // has to be a ListType or (TOP).
        if (that.equals(Type.TOP)) {
            return this;
        }
        if (that instanceof ListType) {
            ListType thatList = (ListType) that;
            if (thatList.subtype + subtype != ELIST + NELIST) {
                // ELIST and NELIST are never compatible
                return new ListType(thatList.getBaseType().unify(getBaseType()),
                                    Math.max(thatList.subtype, subtype));
            }
        }
        throw new UnificationFailure();
    }

    /** Return whether this Type and <that> Type are compatible.
      */
    public boolean canUnify(Type that) {
        if (that.equals(Type.TOP)) {
            return true;
        }
        if (that instanceof ListType) {
            ListType thatList = (ListType) that;
            if (thatList.subtype + subtype != ELIST + NELIST) {
                // ELIST and NELIST are never compatible
                return thatList.getBaseType().canUnify(getBaseType());
            }
        }
        return false;
    }

    /** Look for the most general common extensional supertype of this and <that>. */
    public Type mostGeneralExtensionalSupertype(Type that) {
        // TO-DO
        return null;
    }

    /** Return a new node from this type.
     */
    public Node newNode() {
        if (subtype == NELIST) {
            return new ListNode(this);
        } else {
            return new NoFeatureNode(this);
        }
    }

    public boolean equals(Object that) {
        if (that instanceof ListType) {
            ListType thatList = (ListType) that;
            return thatList.subtype == subtype
                   && thatList.getBaseType().equals(getBaseType());
        }
        return false;
    }

    public int hashCode() {
        return getBaseType().hashCode();
    }

    public String toString() {
        return getName();
    }
}