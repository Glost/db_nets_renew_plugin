package de.renew.formalism.function;

import de.renew.unify.Tuple;

import de.renew.util.ClassSource;
import de.renew.util.Types;
import de.renew.util.Value;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;


public class Executor {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(Executor.class);

    /** returned if there is no result from a method call (void method) */
    public final static Object VOIDRETURN = new Object();
    static Method classForNameMethod;

    static {
        try {
            classForNameMethod = Class.class.getMethod("forName",
                                                       new Class<?>[] { String.class });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);


            // What's that? This method is really supposed to be there.
            // We ignore it, but that's not nice.
            classForNameMethod = null;
        }
    }

    /** This class cannot to be instantiated. */
    private Executor() {
    }

    /**
     * Get the types associated to an array of objects.
     */
    static public Class<?>[] getTypes(Object[] params) {
        Class<?>[] parameterTypes = new Class<?>[params.length];
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof Value) {
                parameterTypes[i] = Types.typify(((Value) params[i]).value
                                        .getClass());
            } else {
                parameterTypes[i] = params[i].getClass();
            }
        }
        return parameterTypes;
    }

    /**
     * Given an array of actual parameter types, find one of
     * the most exact match in an array of constructors. If multiple
     * incomparable matches are found, the first match
     * is returned.
     */
    static public Constructor<?> findBestConstructor(Class<?> clazz,
                                                     Class<?>[] params,
                                                     boolean unique)
            throws NoSuchMethodException {
        // Find the constructor with exact types.
        try {
            return clazz.getConstructor(getTypes(params));
        } catch (Exception e) {
            // This is expected: the constructor does not exist.
            // Let's try it the hard way.
        }


        // Make a list of all constructors and search for the
        // optimal one.
        Constructor<?>[] constructors = clazz.getConstructors();

        Constructor<?> best = null;
        Class<?>[] bestParams = null;

        for (int i = 0; i < constructors.length; i++) {
            Class<?>[] currentParams = constructors[i].getParameterTypes();
            if (Types.allowsWideningConversion(params, currentParams)) {
                // Constructor is usable.
                if (best == null
                            || Types.allowsWideningConversion(currentParams,
                                                                      bestParams)) {
                    best = constructors[i];
                    bestParams = currentParams;
                }
            }
        }

        if (best == null) {
            throw new NoSuchMethodException();
        }

        if (unique) {
            for (int i = 0; i < constructors.length; i++) {
                Class<?>[] currentParams = constructors[i].getParameterTypes();
                if (Types.allowsWideningConversion(params, currentParams)) {
                    // Constructor is usable.
                    if (!Types.allowsWideningConversion(bestParams,
                                                                currentParams)) {
                        // Uncomparable constructors.
                        return null;
                    }
                }
            }
        }

        return best;
    }

    private static boolean isPublic(Class<?> clazz) {
        return Modifier.isPublic(clazz.getModifiers());
    }

    /**
     * Return all those public superclasses of a given class where there is
     * no public class that is both superclass of the given class
     * and subclass of the returned classes.
     *
     * Might have to be changed to collect classes in an updatable set.
     *
     * @param clazz the class object of the base class.
     * @return an enumeration of classes.
     */
    private static Collection<Class<?>> findPublicSuperClasses(Class<?> clazz) {
        // If the clazz is not itself public, we fall back on the
        // super class/interfaces.
        if (isPublic(clazz)) {
            Collection<Class<?>> coll = new Vector<Class<?>>();
            coll.add(clazz);
            return coll;
        } else {
            Collection<Class<?>> classes = new HashSet<Class<?>>();
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                classes.addAll(findPublicSuperClasses(superclass));
            }
            Class<?>[] interfaces = clazz.getInterfaces();
            for (int i = 0; i < interfaces.length; ++i) {
                classes.addAll(findPublicSuperClasses(interfaces[i]));
            }
            return classes;
        }
    }

    private static Tuple makeSignatureTuple(Class<?>[] params) {
        return new Tuple(params, null);
    }

    private static Method[] getPublicMethods(Class<?> clazz, String name,
                                             int paramCount) {
        Hashtable<Tuple, Method> map = new Hashtable<Tuple, Method>();
        Iterator<Class<?>> classes = findPublicSuperClasses(clazz).iterator();
        while (classes.hasNext()) {
            Class<?> publicClass = classes.next();
            Method[] methods = publicClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                Class<?>[] params = method.getParameterTypes();
                if (name.equals(method.getName())
                            && paramCount == params.length) {
                    map.put(makeSignatureTuple(params), method);
                }
            }
        }
        Method[] uniqueMethods = new Method[map.size()];


        // The keys of the map were only used to discard
        // duplicate entries. We need to return the elements
        // of the map. This is no mistake.
        Enumeration<Method> enumeration = map.elements();
        for (int i = 0; i < uniqueMethods.length; i++) {
            uniqueMethods[i] = enumeration.nextElement();
        }
        if (enumeration.hasMoreElements()) {
            throw new RuntimeException("Method count changed. Strange.");
        }
        return uniqueMethods;
    }

    /**
     * Given an array of actual parameter types, find one of
     * the most exact matches in an array of methods. If multiple
     * incomparable matches are found, the first match
     * is returned.
     */
    static public Method findBestMethod(Class<?> clazz, String name,
                                        Class<?>[] params, boolean unique)
            throws NoSuchMethodException {
        // Find the method with exact types.
        try {
            if (isPublic(clazz)) {
                return clazz.getMethod(name, getTypes(params));
            }
        } catch (Exception e) {
            // This is expected: the method does not exist.
            // Let's try it the hard way.
        }

        Method[] methods = getPublicMethods(clazz, name, params.length);
        if (clazz.isInterface()) {
            // We must also look at the methods of java.lang.Object.
            // This is special for methods, as constructors are never called
            // for an interface.
            Method[] interfaceMethods = methods;
            Method[] objectMethods = Object.class.getMethods();
            methods = new Method[interfaceMethods.length + objectMethods.length];
            System.arraycopy(interfaceMethods, 0, methods, 0,
                             interfaceMethods.length);
            System.arraycopy(objectMethods, 0, methods,
                             interfaceMethods.length, objectMethods.length);
        }

        Method best = null;
        Class<?>[] bestParams = null;

        for (int i = 0; i < methods.length; i++) {
            if (name.equals(methods[i].getName())) {
                Class<?>[] currentParams = methods[i].getParameterTypes();
                if (Types.allowsWideningConversion(params, currentParams)) {
                    // Method is usable.
                    if (best == null
                                || Types.allowsWideningConversion(currentParams,
                                                                          bestParams)) {
                        // Method is better than best method found so far.
                        best = methods[i];
                        bestParams = currentParams;
                    }
                }
            }
        }

        if (best == null) {
            throw new NoSuchMethodException();
        }

        if (unique) {
            // Verify that the method found is unique.
            for (int i = 0; i < methods.length; i++) {
                if (name.equals(methods[i].getName())) {
                    Class<?>[] currentParams = methods[i].getParameterTypes();
                    if (Types.allowsWideningConversion(params, currentParams)) {
                        // Method is usable.
                        if (!Types.allowsWideningConversion(bestParams,
                                                                    currentParams)) {
                            // Methods are uncomparable.
                            return null;
                        }
                    }
                }
            }
        }

        return best;
    }

    /**
     * Execute a constructor with parameter objects.
     */
    static public Object executeConstructor(Constructor<?> constructor,
                                            Object[] params)
            throws NoSuchMethodException, InvocationTargetException {
        Object result = null;
        try {
            Object[] castedParams = Value.unvalueAndCast(params,
                                                         constructor
                                        .getParameterTypes());
            result = constructor.newInstance(castedParams);
        } catch (IllegalArgumentException ex) {
            // This should never happen as we got the right number and
            // types of parameters because we got this constructor object
            // through reflections.
            throw new RuntimeException("IllegalArgumentException in Executor!");
        } catch (InstantiationException ex) {
            // This is thrown if the class that is about to be
            // instantiated is abstract. In this case, 
            // we throw an NoSuchMethodException. It is in fact a
            // constructor that cannot be called directly.
            throw new NoSuchMethodException(ex.toString());
        } catch (IllegalAccessException ex) {
            // This is thrown if the constructor that should be called
            // is not public. As only public method should be known in
            // the rest of the world, we throw a NoSuchMethodException.
            throw new NoSuchMethodException(ex.toString());
        }
        return result;
    }

    /**
     * Execute a constructor given the target class and the
     * parameter objects.
     */
    static public Object executeConstructor(Class<?> clazz, Object[] params)
            throws NoSuchMethodException, InvocationTargetException,
                           LinkageError {
        if (clazz.isArray()) {
            Class<?> originalClass = clazz;
            int[] dimensions = new int[params.length];
            try {
                for (int i = 0; i < params.length; i++) {
                    dimensions[i] = ((Integer) Value.unvalueAndCast(params[i],
                                                                    Integer.TYPE))
                                    .intValue();
                    clazz = clazz.getComponentType();
                }
                if (clazz == null) {
                    throw new Exception();
                }
            } catch (Exception e) {
                throw new NoSuchMethodException("Bad array constructor: "
                                                + renderArrayConstructor(originalClass,
                                                                         params));
            }

            return Array.newInstance(clazz, dimensions);
        } else {
            Constructor<?> constructor = findBestConstructor(clazz,
                                                             getTypes(params),
                                                             false);
            return executeConstructor(constructor, params);
        }
    }

    /**
     * Execute a given method on an object using some parameters.
     *
     * The target must be null if and only if a static method is to be called.
     */
    static public Object executeMethod(Method method, Object target,
                                       Object[] params)
            throws NoSuchMethodException, InvocationTargetException {
        // Patch the special Class.forName(String) method.
        // Should this really be done here? It smells fishy,
        // it would not reach the same generality when done in the
        // compiler and it would even be more complex.
        //
        // A user can still circumvent this protection mechanism
        // by calling the reflection mechanims himself in order to
        // get at the native implementation of Class.forName(String),
        // but that's not our problem. Let them have some fun, too.
        if (method.equals(classForNameMethod) && params.length == 1
                    && params[0] instanceof String) {
            // Call the class source instead, so that a class loader
            // can be specified.
            try {
                return ClassSource.classForName((String) params[0]);
            } catch (Throwable t) {
                throw new InvocationTargetException(t);
            }
        }

        Object result = null;
        try {
            Object[] castedParams = Value.unvalueAndCast(params,
                                                         method
                                        .getParameterTypes());
            result = method.invoke(target, castedParams);
            if (method.getReturnType() == Void.TYPE) {
                result = VOIDRETURN;
            } else {
                result = Value.possiblyWrap(result,
                                            method.getReturnType().isPrimitive());
            }
        } catch (IllegalArgumentException ex) {
            // this should never happen as we got the right number and types of
            // parameters because we got this method object through reflections
            throw new RuntimeException("IllegalArgumentException in Executor!");
        } catch (IllegalAccessException ex) {
            // This is thrown if the method that should be called is not public.
            // As only public method should be known in the rest of the world,
            // we throw a NoSuchMethodException
            throw new NoSuchMethodException(ex.toString());
        }

        return result;
    }

    /**
     * Execute a method of a given name in a given class on an object
     * using some parameters.
     *
     * The target object should be of the provided clazz.
     * It must be null if and only if a static method is to be called.
     */
    static public Object executeMethod(Class<?> clazz, Object target,
                                       String name, Object[] params)
            throws NoSuchMethodException, InvocationTargetException,
                           LinkageError {
        Method method = findBestMethod(clazz, name, getTypes(params), false);
        return executeMethod(method, target, params);
    }

    /**
     * Constructs a string description of a method signature based on the
     * given arguments. The given information may be incomplete, but the
     * the method name is mandatory.
     *
     * The signature does not include any modifiers, there is no visible
     * distinction between class and instance methods, and the return type
     * is not included.
     *
     * @param clazz   the <code>Class</code> where the method is defined in
     *                (may be <code>null</code> or {@link Types#UNTYPED}).
     * @param name    the method name (this is mandatory, to describe
     *                constructors, use &quot;<code>&lt;init&gt</code>;&quot).
     * @param params  the parameter types (may be <code>null</code> if
     *                the number of arguments is unknown).
     * @return        a generated signature string.
     **/
    public static String renderMethodSignature(Class<?> clazz, String name,
                                               Class<?>[] params) {
        StringBuffer signature = new StringBuffer();
        try {
            signature.append(Types.typeToString(clazz));
            signature.append('.');
            signature.append(name);
            if (params != null) {
                signature.append('(');
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) {
                        signature.append(", ");
                    }
                    signature.append(Types.typeToString(params[i]));
                }
                signature.append(')');
            } else {
                signature.append("<unknown parameters>");
            }
        } catch (Exception e) {
            signature.append("!!!<");
            signature.append(e.toString());
            signature.append(">!!!");
        }
        return signature.toString();
    }

    /**
     * Constructs a string description of an array instantiation based on
     * the given arguments.
     * The supplied information may be incomplete or incorrect.
     * <p>
     * The output follows no standard syntax: First comes the array type
     * with a bracket pair [] per dimension, afterwards the
     * should-be-integer dimension enclosed in parentheses like method
     * parameters. This notations allows a comparision of the array
     * dimensions and the per-dimension limits also if they don't fit.
     * </p>
     * @param clazz    the array type.
     * @param params   the array of dimensions.
     * @return         a <code>String</code> description of the arguments.
     **/
    public static String renderArrayConstructor(Class<?> clazz, Object[] params) {
        StringBuffer signature = new StringBuffer();
        try {
            signature.append(Types.typeToString(clazz));
            if (params != null) {
                signature.append('(');
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) {
                        signature.append(", ");
                    }
                    signature.append(params[i]);
                }
                signature.append(')');
            } else {
                signature.append("<unknown parameters>");
            }
        } catch (Exception e) {
            signature.append("!!!<");
            signature.append(e.toString());
            signature.append(">!!!");
        }
        return signature.toString();
    }
}