package de.uni_hamburg.fs;

import de.renew.util.Null;
import de.renew.util.Types;
import de.renew.util.Value;

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * A JavaArrayType wraps a java array, {@link Enumeration} or
 * {@link Iterator} object as a Type.
 * It builds a List of the component type containing all elements of
 * the array, enumeration or iterator.
 **/
public class JavaArrayType extends ListType implements JavaType {
    Object[] javaArray;

    /** Cache for Hashcode. */
    private int hashCode;
    private AbstractNode root = null;

    /** Construct a new wrapper for the given Java Array. */
    JavaArrayType(Object javaArray) {
        super(TypeSystem.instance()
                        .getType(javaArray.getClass().getComponentType()),
              length(javaArray) == 0 ? ELIST : NELIST);
        this.javaArray = makeObjectArray(javaArray);
    }

    /** Construct a new wrapper for the given Enumeration. */
    JavaArrayType(Enumeration<?> enumeration) {
        super(TypeSystem.instance().getType(Object.class),
              enumeration.hasMoreElements() ? NELIST : ELIST);
        javaArray = makeObjectArray(enumeration);
    }

    /** Construct a new wrapper for the given Iterator. */
    JavaArrayType(Iterator<?> it) {
        super(TypeSystem.instance().getType(Object.class),
              it.hasNext() ? NELIST : ELIST);
        javaArray = makeObjectArray(it);
    }

    public static Object[] makeObjectArray(Object array) {
        if (array instanceof Enumeration) {
            Enumeration<?> enumeration = (Enumeration<?>) array;
            Vector<Object> elements = new Vector<Object>();
            while (enumeration.hasMoreElements()) {
                elements.addElement(enumeration.nextElement());
            }
            Object[] objectArray = new Object[elements.size()];
            elements.copyInto(objectArray);
            return objectArray;
        } else if (array instanceof Iterator) {
            Iterator<?> it = (Iterator<?>) array;
            List<Object> elements = new ArrayList<Object>();
            while (it.hasNext()) {
                elements.add(it.next());
            }
            Object[] objectArray = elements.toArray();
            return objectArray;
        }

        // "array" must be array type:
        if (array == null || !array.getClass().isArray()) {
            throw new IllegalArgumentException();
        }
        Class<?> clazz = array.getClass().getComponentType();
        if (clazz.isPrimitive()) {
            // copy components and wrap primitive types as Values:
            Value[] objectArray = new Value[length(array)];
            for (int i = 0; i < objectArray.length; ++i) {
                Object val;
                if (clazz == Integer.TYPE) {
                    val = new Integer(((int[]) array)[i]);
                } else if (clazz == Character.TYPE) {
                    val = new Character(((char[]) array)[i]);
                } else if (clazz == Double.TYPE) {
                    val = new Double(((double[]) array)[i]);
                } else if (clazz == Float.TYPE) {
                    val = new Float(((float[]) array)[i]);
                } else if (clazz == Long.TYPE) {
                    val = new Long(((long[]) array)[i]);
                } else if (clazz == Byte.TYPE) {
                    val = new Byte(((byte[]) array)[i]);
                } else if (clazz == Boolean.TYPE) {
                    val = new Boolean(((boolean[]) array)[i]);
                } else {
                    throw new RuntimeException("Unknown primitive class: "
                                               + clazz);
                }
                objectArray[i] = new Value(val);
            }
            return objectArray;
        } else {
            return (Object[]) array;
        }
    }

    public static Object makeArray(Object[] objectArray) {
        if (objectArray.length > 0
                    && objectArray.getClass().getComponentType() == Value.class) {
            Class<?> clazz = Types.typify(((Value) objectArray[0]).value
                                 .getClass());

            // copy components and unwrap Values to primitive types:
            Object array = Array.newInstance(clazz, objectArray.length);
            for (int i = 0; i < objectArray.length; ++i) {
                Object val = ((Value) objectArray[i]).value;
                if (clazz == Integer.TYPE) {
                    ((int[]) array)[i] = ((Integer) val).intValue();
                } else if (clazz == Character.TYPE) {
                    ((char[]) array)[i] = ((Character) val).charValue();
                } else if (clazz == Double.TYPE) {
                    ((double[]) array)[i] = ((Double) val).doubleValue();
                } else if (clazz == Float.TYPE) {
                    ((float[]) array)[i] = ((Float) val).floatValue();
                } else if (clazz == Long.TYPE) {
                    ((long[]) array)[i] = ((Long) val).longValue();
                } else if (clazz == Byte.TYPE) {
                    ((byte[]) array)[i] = ((Byte) val).byteValue();
                } else if (clazz == Boolean.TYPE) {
                    ((boolean[]) array)[i] = ((Boolean) val).booleanValue();
                } else {
                    throw new RuntimeException("Unknown primitive class: "
                                               + clazz);
                }
            }
            return array;
        } else {
            return objectArray;
        }
    }

    public static int length(Object array) {
        Class<?> clazz = array.getClass().getComponentType();
        if (clazz.isPrimitive()) {
            // cast to the right primitve array type
            if (clazz == Integer.TYPE) {
                return ((int[]) array).length;
            } else if (clazz == Character.TYPE) {
                return ((char[]) array).length;
            } else if (clazz == Double.TYPE) {
                return ((double[]) array).length;
            } else if (clazz == Float.TYPE) {
                return ((float[]) array).length;
            } else if (clazz == Long.TYPE) {
                return ((long[]) array).length;
            } else if (clazz == Byte.TYPE) {
                return ((byte[]) array).length;
            } else if (clazz == Boolean.TYPE) {
                return ((boolean[]) array).length;
            }
        }
        return ((Object[]) array).length;
    }

    public static boolean equals(Object o1, Object o2) {
        if (Null.nullAwareEquals(o1, o2)) {
            return true;
        }
        try {
            return equals(makeObjectArray(o1), makeObjectArray(o2));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean equals(Object[] a1, Object[] a2) {
        if (a1.length == a2.length) {
            if (a1.getClass().getComponentType() == a2.getClass()
                                                              .getComponentType()) {
                for (int i = 0; i < a1.length; ++i) {
                    if (!Null.nullAwareEquals(a1[i], a2[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /** Return whether this Type represents an instance. */
    public boolean isInstanceType() {
        return true;
    }

    public Object getJavaObject() {
        return javaArray;
    }

    public int hashCode() {
        buildList();
        return hashCode;
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof JavaArrayType) {
            JavaArrayType thatJAT = (JavaArrayType) that;
            return equals(javaArray, thatJAT.javaArray);
        }
        return false;
    }

    public String toString() {
        return BasicType.objToString(javaArray);
    }

    /** Return whether this Type subsumes <that> Type.
      * In other words, return wheter this Type is more general than <that> Type.
      */
    public boolean subsumes(Type that) {
        return equals(that);
    }

    /** Return the unification of this Type and <that> Type.
      * this Type is not modified!
      */
    public Type unify(Type that) throws UnificationFailure {
        if (that instanceof JavaArrayType) {
            if (equals(that)) {
                return this;
            }
        } else if (that instanceof ConjunctiveType) {
            return that.unify(this);
        }
        throw new UnificationFailure();
    }

    /** Return whether this Type and <that> Type are compatible.
      */
    public boolean canUnify(Type that) {
        try {
            unify(that);
            return true;
        } catch (UnificationFailure uff) {
            return false;
        }
    }

    /** Look for the most general common extensional supertype of this and <that>. */
    public Type mostGeneralExtensionalSupertype(Type that) {
        // TODO
        return null;
    }

    private void buildList() {
        if (root == null) {
            // build the actual list:
            root = (AbstractNode) ListType.getEList(getBaseType()).newNode();
            hashCode = 0;
            ListType listType = ListType.getNEList(getBaseType());
            for (int i = javaArray.length - 1; i >= 0; --i) {
                hashCode += Null.nullAwareHashCode(javaArray[i]);
                //root=new JavaArrayNode(this,javaArray[i],root,i,hashCode);
                Node tail = root;
                root = new ListNode(listType);
                root.setFeature(ListType.HEAD,
                                JavaObject.getJavaType(javaArray[i]).newNode());
                root.setFeature(ListType.TAIL, tail);
            }
            root.nodetype = this;
            hashCode += 3 * getBaseType().hashCode();
        }
    }

    public Node newNode() {
        buildList();
        return root; // always return the same node!
    }
}