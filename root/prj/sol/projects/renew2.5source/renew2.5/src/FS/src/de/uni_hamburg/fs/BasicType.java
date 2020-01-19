package de.uni_hamburg.fs;

import de.renew.util.Types;
import de.renew.util.Value;


public class BasicType extends JavaClassType {
    public final static Object POSINF = new Name("POSINF");
    public final static Object NEGINF = new Name("NEGINF");
    private Object lower;
    private Object upper;
    private boolean toBeInstantiated = false;

    public BasicType(Class<?> type) {
        if (type != String.class && !type.isPrimitive()) {
            throw new RuntimeException("Class " + type
                                       + " cannot be used as a BasicType.");
        }
        this.setJavaClass(type);
        this.lower = NEGINF;
        this.upper = POSINF;
    }

    public BasicType(Object single) {
        this(getValueClass(single));
        this.lower = single;
        this.upper = single;
    }

    public BasicType(Object lower, Object upper) throws TypeException {
        this(getValueClass(lower, upper), lower, upper);
    }

    public BasicType(Class<?> type, Object lower, Object upper)
            throws TypeException {
        checkClass(type, lower);
        checkClass(type, upper);
        if (compare(lower, upper) > 0) {
            // upper is smaller than lower.
            throw new TypeException();
        }
        setJavaClass(type);
        this.lower = lower;
        this.upper = upper;
    }

    private BasicType(Class<?> type, Object lower, Object upper,
                      boolean toBeInstantiated) {
        super(type);
        this.lower = lower;
        this.upper = upper;
        this.toBeInstantiated = toBeInstantiated;
    }

    private static Class<?> getValueClass(Object a) {
        if (a instanceof String) {
            return String.class;
        }
        try {
            return Types.typify(((Value) a).value.getClass());
        } catch (ClassCastException cce) {
            throw new RuntimeException("Class " + a.getClass()
                                       + " cannot be used as a BasicType.");
        }
    }

    private static void checkClass(Class<?> type, Object a)
            throws TypeException {
        if (!POSINF.equals(a) && !NEGINF.equals(a)) {
            Class<?> aclass = getValueClass(a);
            if (aclass != type) {
                throw new TypeException();
                //RuntimeException("Bound "+a+" is not of BasicType "+aclass+".");
            }
        }
    }

    private static Class<?> getValueClass(Object lower, Object upper)
            throws TypeException {
        if (!NEGINF.equals(lower)) {
            return getValueClass(lower);
        } else if (!POSINF.equals(upper)) {
            return getValueClass(upper);
        } else {
            throw new TypeException();
        }
    }

    public boolean isObject() {
        return lower.equals(upper);
    }

    public boolean isInstanceType() {
        return toBeInstantiated;
    }

    public Type getInstanceType() {
        if (isInstanceType()) {
            return this;
        }
        return new BasicType(getJavaClass(), lower, upper, true);
    }

    public Object getJavaObject() {
        if (!isObject()) {
            throw new RuntimeException("getJavaObject() called in BasicType although lower and upper did not meet!");
        }
        return lower;
    }

    public static String objToString(Object obj) {
        if (NEGINF.equals(obj) || POSINF.equals(obj)) {
            return "*";
        } else if (obj instanceof String) {
            return "\"" + obj + "\"";
        } else if (obj instanceof Value) {
            return ((Value) obj).value.toString();
        } else if (obj == null) {
            return "null";
        } else {
            return obj.toString();
        }
    }

    /** Return the name of this Type. */
    public String getName() {
        StringBuffer output = new StringBuffer();
        if (NEGINF.equals(lower) && POSINF.equals(upper)) {
            if (getJavaClass() == String.class) {
                output.append("String");
            } else {
                output.append(getJavaClass().getName());
            }
        } else if (upper.equals(lower)) {
            output.append(objToString(lower));
        } else {
            output.append("{").append(objToString(lower)).append("..")
                  .append(objToString(upper)).append("}");
        }
        return output.toString();
    }

    /** Return the fully qualified name of this Type. */
    public String getFullName() {
        return getName();
    }

    /** Return whether this Type is extensional. */
    public boolean isExtensional() {
        return isObject();
    }

    /** Return whether this Type subsumes <that> Type.
      * In other words, return wheter this Type is more general than <that> Type.
      */
    public boolean subsumes(Type that) {
        if (that instanceof BasicType) {
            BasicType thatBasic = (BasicType) that;
            if (toBeInstantiated && !thatBasic.toBeInstantiated
                        || thatBasic.getJavaClass() != getJavaClass()) {
                return false;
            }
            return compare(lower, thatBasic.lower) <= 0
                   && compare(upper, thatBasic.upper) >= 0;
        } else if (that instanceof NullObject) {
            return getJavaClass() == String.class && lower.equals(NEGINF)
                   && upper.equals(POSINF);
        }
        return false;
    }

    /** Return the unification of this Type and <that> Type.
      * this Type is not modified!
      */
    public Type unify(Type that) throws UnificationFailure {
        // has to be a BasicType or (TOP).
        if (that.equals(Type.TOP)) {
            return this;
        }
        if (subsumes(that)) {
            return that;
        }
        if (that instanceof BasicType) {
            BasicType thatBasic = (BasicType) that;
            if (thatBasic.getJavaClass() == getJavaClass()) {
                Object newLower = max(lower, thatBasic.lower);
                Object newUpper = min(upper, thatBasic.upper);
                if (compare(newLower, newUpper) > 0) {
                    // upper is smaller than lower.
                    throw new TypeException();
                }
                return new BasicType(getJavaClass(), newLower, newUpper,
                                     toBeInstantiated
                                     || thatBasic.toBeInstantiated);
            }
        } else if (that.subsumes(this)) {
            return this;
        }
        throw new UnificationFailure();
    }

    /** Return whether this Type and <that> Type are compatible.
      */
    public boolean canUnify(Type that) {
        if (that.equals(Type.TOP)) {
            return true;
        }
        if (that instanceof BasicType) {
            BasicType thatBasic = (BasicType) that;
            if (thatBasic.getJavaClass() == getJavaClass()) {
                return compare(lower, thatBasic.upper) <= 0
                       && compare(thatBasic.lower, upper) <= 0;
            }
            return false;
        }
        return subsumes(that);
    }

    /** Return a new node from this type.
     */


    //    public Node newNode() {
    //      if (lower.equals(upper))
    //        return new BasicObjectNode(this);
    //      else
    //        return new NoFeatureNode(this);
    //    }


    /** Look for the most general common extensional supertype of this and <that>. */
    public Type mostGeneralExtensionalSupertype(Type that) {
        // TODO
        return null;
    }

    public boolean equals(Object that) {
        if (that instanceof BasicType) {
            BasicType thatBasic = (BasicType) that;
            return toBeInstantiated == thatBasic.toBeInstantiated
                   && super.equals(that) && thatBasic.lower.equals(lower)
                   && thatBasic.upper.equals(upper);
        }
        return false;
    }

    public int hashCode() {
        return getJavaClass().hashCode() + lower.hashCode() + upper.hashCode();
    }

    int compare(Object a, Object b) {
        if (a.equals(b)) {
            return 0;
        }
        if (NEGINF.equals(a) || POSINF.equals(b)) {
            return -1; // NEGINF is smaller than everything and
        }

        // everything is smaller than POSINF
        if (POSINF.equals(a) || NEGINF.equals(b)) {
            return 1; // POSINF is larger than everything and
        }

        // everything is larger than NEGINF
        if (a instanceof String && b instanceof String) {
            return ((String) a).compareTo((String) b);
        }
        if (a instanceof Value && b instanceof Value) {
            Value av = (Value) a;
            Value bv = (Value) b;
            Object ao = av.value;
            Object bo = bv.value;
            if (!ao.getClass().equals(bo.getClass())) {
                throw new RuntimeException("Different BasicType Classes: "
                                           + ao.getClass() + " and "
                                           + bo.getClass());
            }
            if (ao instanceof Boolean) {
                return bv.booleanValue() ? -1 : 1;
            } else if (ao instanceof Character) {
                return av.charValue() < bv.charValue() ? -1 : 1;
            } else if (ao instanceof Byte) {
                return av.byteValue() < bv.byteValue() ? -1 : 1;
            } else if (ao instanceof Short) {
                return av.shortValue() < bv.shortValue() ? -1 : 1;
            } else if (ao instanceof Integer) {
                return av.intValue() < bv.intValue() ? -1 : 1;
            } else if (ao instanceof Long) {
                return av.longValue() < bv.longValue() ? -1 : 1;
            } else if (ao instanceof Float) {
                return av.floatValue() < bv.floatValue() ? -1 : 1;
            } else if (ao instanceof Double) {
                return av.doubleValue() < bv.doubleValue() ? -1 : 1;
            } else {
                a = ao;
                b = bo; // for exception!
            }
        }
        throw new RuntimeException("Wrong BasicType classes: " + a.getClass()
                                   + " and/or " + b.getClass());
    }

    Object max(Object a, Object b) {
        return compare(a, b) >= 0 ? a : b;
    }

    Object min(Object a, Object b) {
        return compare(a, b) <= 0 ? a : b;
    }
}