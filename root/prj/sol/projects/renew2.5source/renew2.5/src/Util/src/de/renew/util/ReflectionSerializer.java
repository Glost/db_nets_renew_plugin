package de.renew.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Helper class: supplies static methods to write/read instances
 * of <code>java.lang.reflect</code> classes to/from
 * ObjectInput/OutputStreams.
 * </p><p>
 * Also supplies static methods to write/read instances of
 * <code>java.lang.Class</code> to/from such Streams.
 * Unfortunately, <code>java.lang.Class</code> objects seem
 * to be serializable only if the class described by the
 * object is serializable. So this is a workaround for a bug(?)
 * in the package java.io of JDK 1.1.8. The workaround does
 * not seem to be necessary in JDK 1.2.
 * </p>
 *
 * ReflectionSerializer.java
 *
 * Created: Tue Feb  1  2000
 *
 * @author Michael Duvigneau
 */
public class ReflectionSerializer {

    /** This class cannot be instantiated. */
    private ReflectionSerializer() {
    }

    // ---- FIELD ---------------------------------------------
    //
    // Uniquely identified by: Class    Declaring class
    //                         String   Field name
    public static void writeField(ObjectOutputStream out, Field field)
            throws IOException {
        if (field == null) {
            out.writeObject(null);
        } else {
            writeClass(out, field.getDeclaringClass());
            out.writeObject(field.getName());
        }
    }

    public static Field readField(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        try {
            Class<?> clazz = readClass(in);
            if (clazz == null) {
                return null;
            } else {
                String name = (String) in.readObject();
                return clazz.getField(name);
            }
        } catch (ClassCastException e) {
            throw new StreamCorruptedException("(RRS) Could not read java.lang.reflect.Field: "
                                               + e);
        } catch (NoSuchFieldException e) {
            throw new StreamCorruptedException("(RRS) Could not read java.lang.reflect.Field: "
                                               + e);
        }
    }

    // ---- METHOD --------------------------------------------
    //
    // Uniquely identified by: Class    Declaring class
    //                         String   Method name
    //                         Class[]  Parameter types
    public static void writeMethod(ObjectOutputStream out, Method method)
            throws IOException {
        if (method == null) {
            out.writeObject(null);
        } else {
            writeClass(out, method.getDeclaringClass());
            out.writeObject(method.getName());
            writeClassArray(out, method.getParameterTypes());
        }
    }

    public static Method readMethod(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        try {
            Class<?> clazz = readClass(in);
            if (clazz == null) {
                return null;
            } else {
                String name = (String) in.readObject();
                Class<?>[] params = readClassArray(in);
                return clazz.getMethod(name, params);
            }
        } catch (ClassCastException e) {
            throw new StreamCorruptedException("(RRS) Could not read java.lang.reflect.Method: "
                                               + e);
        } catch (NoSuchMethodException e) {
            throw new StreamCorruptedException("(RRS) Could not read java.lang.reflectMethod: "
                                               + e);
        }
    }

    // ---- CONSTRUCTOR ---------------------------------------
    //
    // Uniquely identified by: Class    Declaring class
    //                         Class[]  Parameter types
    public static void writeConstructor(ObjectOutputStream out,
                                        Constructor<?> constructor)
            throws IOException {
        if (constructor == null) {
            out.writeObject(null);
        } else {
            writeClass(out, constructor.getDeclaringClass());
            writeClassArray(out, constructor.getParameterTypes());
        }
    }

    public static Constructor<?> readConstructor(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        try {
            Class<?> clazz = readClass(in);
            if (clazz == null) {
                return null;
            } else {
                Class<?>[] params = readClassArray(in);
                return clazz.getConstructor(params);
            }
        } catch (ClassCastException e) {
            throw new StreamCorruptedException("(RRS) Could not read java.lang.reflect.Constructor: "
                                               + e);
        } catch (NoSuchMethodException e) {
            throw new StreamCorruptedException("(RRS) Could not read java.lang.reflect.Constructor: "
                                               + e);
        }
    }

    // ---- CLASS ---------------------------------------------
    //
    // Uniquely identified by: String   fully qualified class name
    //
    // The primitive types (boolean, byte, char, double, float,
    // int, long, short, void) are represented by instances of
    // Class which refer to types not classes. They have to be
    // treated in a special way because ordinary class loading does not
    // work for them.
    public static void writeClass(ObjectOutputStream out, Class<?> clazz)
            throws IOException {
        if (clazz == null) {
            out.writeObject(null);
        } else {
            out.writeObject(clazz.getName());
        }
    }

    public static Class<?> readClass(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        try {
            String name = (String) in.readObject();
            if (name == null) {
                return null;
            } else if (name.equals("boolean")) {
                return Boolean.TYPE;
            } else if (name.equals("byte")) {
                return Byte.TYPE;
            } else if (name.equals("char")) {
                return Character.TYPE;
            } else if (name.equals("double")) {
                return Double.TYPE;
            } else if (name.equals("float")) {
                return Float.TYPE;
            } else if (name.equals("int")) {
                return Integer.TYPE;
            } else if (name.equals("long")) {
                return Long.TYPE;
            } else if (name.equals("short")) {
                return Short.TYPE;
            } else if (name.equals("void")) {
                return Void.TYPE;
            } else {
                return ClassSource.classForName(name);
            }
        } catch (ClassCastException e) {
            throw new StreamCorruptedException("(RRS) Could not read java.lang.Class: "
                                               + e);
        }
    }

    // ---- CLASS[] -------------------------------------------
    //
    // The array itself is written as: Integer number of items
    //                                 Class   class 0 
    //                                 Class   class 1
    //                                 ...
    // The Class objects are written using writeClass/readClass.
    //
    // Compatibility with earlier streams:
    // If instead of an Integer object a Class[] object is found,
    // the object is returned immediately.
    public static void writeClassArray(ObjectOutputStream out,
                                       Class<?>[] classes)
            throws IOException {
        if (classes == null) {
            out.writeObject(null);
        } else {
            out.writeObject(new Integer(classes.length));
            for (int i = 0; i < classes.length; i++) {
                writeClass(out, classes[i]);
            }
        }
    }

    public static Class<?>[] readClassArray(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        Object readObject = in.readObject();
        if (readObject == null) {
            return null;
        } else if (readObject instanceof Class<?>[]) {
            return (Class<?>[]) readObject;
        } else if (readObject instanceof Integer) {
            int count = ((Integer) readObject).intValue();
            Class<?>[] classes = new Class<?>[count];
            for (int i = 0; i < count; i++) {
                classes[i] = readClass(in);
            }
            return classes;
        } else {
            throw new StreamCorruptedException("(RRS) Could not read java.lang.Class[]: "
                                               + "Found "
                                               + readObject.getClass().getName()
                                               + " instead.");
        }
    }
}