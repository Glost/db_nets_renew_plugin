package de.renew.util;



/**
 * Implement the typing rules for Java reference nets.
 *
 * Assignment conversions are not implemented.
 *
 * A special anonymous class serves as a representative of the untype.
 *
 * The null type is represented by the object null.
 */
public class Types {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(Types.class);

    /**
     * Create a new anonymous class that does not
     * differ from java.lang.Object except that it is
     * a different class, then create a single object
     * of that class and extract the class object from it.
     */
    public static final Class<?> UNTYPED = new Object() {
        }.getClass();
    public static final String[] NOPACKAGES = null;
    public static final String[] ALLPACKAGES = {  };

    // Add a test suite. Very rudimentary.
    private static int assertionCount = 0;

    /**
     * This class is completely static. Do not instantiate me.
     */
    private Types() {
    }

    /**
     * Convert from primitive types to the associated classes.
     */
    public static Class<?> objectify(Class<?> clazz) {
        if (clazz == Boolean.TYPE) {
            return Boolean.class;
        } else if (clazz == Character.TYPE) {
            return Character.class;
        } else if (clazz == Byte.TYPE) {
            return Byte.class;
        } else if (clazz == Short.TYPE) {
            return Short.class;
        } else if (clazz == Integer.TYPE) {
            return Integer.class;
        } else if (clazz == Long.TYPE) {
            return Long.class;
        } else if (clazz == Float.TYPE) {
            return Float.class;
        } else if (clazz == Double.TYPE) {
            return Double.class;
        }
        return clazz;
    }

    /**
     * Determine if the argument class is final.
     */
    public static boolean isFinal(Class<?> clazz) {
        if (clazz == null || clazz == UNTYPED) {
            // Hmm, what do make of these two cases?
            // Nobody inherits from them, surely.
            return true;
        }
        return (clazz.getModifiers() & java.lang.reflect.Modifier.FINAL) != 0;
    }

    /**
     * Determine if a conversion is possible by an identity conversion.
     */
    public static boolean allowsIdentityConversion(Class<?> from, Class<?> to) {
        return to == from;
    }

    /**
     * Determine if a conversion is possible by a primitive
     * widening conversion.
     */
    public static boolean allowsPrimitiveWidening(Class<?> from, Class<?> to) {
        // Exclude conversions to null or the untype.
        // They should not occur.
        if (to == null || to == UNTYPED) {
            return false;
        }

        // Exclude identity conversions.
        if (to == from) {
            return false;
        }

        // Exclude the null type and the untype.
        if (from == null || from == UNTYPED) {
            return false;
        }

        // Restrict attention to primitive types on both sides.
        if (!to.isPrimitive() || !from.isPrimitive()) {
            return false;
        }

        // Treat void first.
        if (to == Void.TYPE || from == Void.TYPE) {
            return false;
        }

        // Same goes for booleans.
        if (to == Boolean.TYPE || from == Boolean.TYPE) {
            return false;
        }

        // to and from must both be numeric or character types.
        if (to == Character.TYPE) {
            return false;
        }

        // to must be numeric.
        if (to == Double.TYPE) {
            return true;
        }
        if (from == Double.TYPE) {
            return false;
        }
        if (to == Float.TYPE) {
            return true;
        }
        if (from == Float.TYPE) {
            return false;
        }

        // to must be numeric integral, from must be integral.
        if (to == Long.TYPE) {
            return true;
        }
        if (from == Long.TYPE) {
            return false;
        }
        if (to == Integer.TYPE) {
            return true;
        }
        if (from == Byte.TYPE) {
            return true;
        }

        // The remaining conversions are not allowed.
        return false;
    }

    /**
     * Determine if a conversion is possible by a reference
     * widening conversion.
     */
    public static boolean allowsReferenceWidening(Class<?> from, Class<?> to) {
        // Exclude conversions to null or the untype.
        // They should not occur.
        if (to == null || to == UNTYPED) {
            return false;
        }

        // Exclude identity conversions.
        if (to == from) {
            return false;
        }

        // Exclude the untype.
        if (from == UNTYPED) {
            return false;
        }

        // Treat the null type.
        if (from == null) {
            return !to.isPrimitive();
        }

        // Restrict attention to reference types on both sides.
        if (to.isPrimitive() || from.isPrimitive()) {
            return false;
        }

        // The check is provided by the Java runtime library.
        return to.isAssignableFrom(from);
    }

    /**
     * Determine if a source and target type permit
     * a lossless conversion.
     */
    public static boolean allowsLosslessWidening(Class<?> from, Class<?> to) {
        // Widening conversions are the only candidates for lossless
        // conversions.
        if (!allowsWideningConversion(from, to)) {
            return false;
        }

        // Exclude the three bad casts that remain.
        if (from == Integer.TYPE && to == Float.TYPE
                    || from == Long.TYPE && to == Float.TYPE
                    || from == Long.TYPE && to == Double.TYPE) {
            return false;
        }

        // Everything is ok.
        return true;
    }

    /**
     * Determine if a formal and an actual parameter would match for
     * a method or constructor call.
     */
    public static boolean allowsWideningConversion(Class<?> from, Class<?> to) {
        return allowsIdentityConversion(from, to)
               || allowsPrimitiveWidening(from, to)
               || allowsReferenceWidening(from, to);
    }

    /**
     * Determine if formal and actual parameters would match for
     * a method or constructor call.
     */
    public static boolean allowsWideningConversion(Class<?>[] from,
                                                   Class<?>[] to) {
        if (from.length != to.length) {
            return false;
        }
        for (int i = 0; i < from.length; i++) {
            if (!allowsWideningConversion(from[i], to[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine if a cast is permitted at compile time.
     */
    public static boolean allowsCast(Class<?> from, Class<?> to) {
        // Exclude conversions to null or the untype.
        // They should not occur.
        if (to == null || to == UNTYPED) {
            return false;
        }

        // Treat the null type.
        if (from == null) {
            return !to.isPrimitive();
        }

        // Treat the untype.
        if (from == UNTYPED) {
            return true;
        }

        // Reduce array types.
        while (from.isArray() && to.isArray()) {
            from = from.getComponentType();
            to = to.getComponentType();
        }

        // Consider the identity conversion.
        if (allowsIdentityConversion(from, to)) {
            return true;
        }

        // Treat primitive types;
        if (to.isPrimitive() || from.isPrimitive()) {
            if (!to.isPrimitive() || !from.isPrimitive()) {
                return false;
            }


            // Everything that is not a void or a boolean can be converted.
            // Note that we have already treated identity conversions.
            return to != Boolean.TYPE && from != Boolean.TYPE
                   && to != Void.TYPE && from != Void.TYPE;
        }

        // Treat reference types.
        if (allowsReferenceWidening(from, to)) {
            return true;
        }
        if (allowsReferenceWidening(to, from)) {
            return true;
        }


        // If either from or to is final, there is nothing more
        // we can do. Since array classes are final, this should
        // also eliminate arrays.
        if (isFinal(from)) {
            return false;
        }
        if (isFinal(to)) {
            return false;
        }


        // If neither from nor to are interfaces, there is nothing more
        // we can do.
        if (!to.isInterface() && !from.isInterface()) {
            return false;
        }


        // Do any methods disagree on the return type?
        // 
        // (Actually this check is stronger than documented by the JLS,
        // which allows class types to be cast to reference types
        // even if the return types of some methods disagree. This is
        // not sensible, because such a cast must fail at runtime
        // in any case.)
        java.lang.reflect.Method[] methods = from.getMethods();
        for (int i = 0; i < methods.length; i++) {
            try {
                java.lang.reflect.Method otherMethod = to.getMethod(methods[i]
                                                           .getName(),
                                                                    methods[i]
                                                           .getParameterTypes());
                if (otherMethod.getReturnType() != methods[i].getReturnType()) {
                    return false;
                }
            } catch (Exception e) {
            }
        }
        return true;
    }

    /**
     * Convert object classes to primitive types.
     */
    public static Class<?> typify(Class<?> clazz) {
        if (clazz == Void.class) {
            return Void.TYPE;
        } else if (clazz == Boolean.class) {
            return Boolean.TYPE;
        } else if (clazz == Character.class) {
            return Character.TYPE;
        } else if (clazz == Byte.class) {
            return Byte.TYPE;
        } else if (clazz == Short.class) {
            return Short.TYPE;
        } else if (clazz == Integer.class) {
            return Integer.TYPE;
        } else if (clazz == Long.class) {
            return Long.TYPE;
        } else if (clazz == Float.class) {
            return Float.TYPE;
        } else if (clazz == Double.class) {
            return Double.TYPE;
        }
        throw new RuntimeException("Cannot make primitive type from " + clazz);
    }

    public static String typeToString(Class<?> clazz) {
        return typeToString(clazz, ALLPACKAGES);
    }

    /**
     * Convert a class to a string, while keeping track of well-known
     * packages that do not need to be printed.
     *
     * @param clazz the class to be converted to a string
     * @param packages the packages that do not need to ne
     *   displayed as a string array, or an empty string array if
     *   all packages should be displayed, or null, if no package names
     *   should be displayed.
     * @return the type string
     */
    public static String typeToString(Class<?> clazz, String[] packages) {
        if (clazz == UNTYPED) {
            return "untyped";
        } else if (clazz == null) {
            return "null";
        }

        StringBuffer buf = new StringBuffer();
        while (clazz.isArray()) {
            buf.append("[]");
            clazz = clazz.getComponentType();
        }

        String baseName = clazz.getName();
        String pureName = getPureName(baseName);
        String peckage = null;
        if (packages != null) {
            peckage = getPackageName(baseName);
            for (int i = 0; peckage != null && i < packages.length; i++) {
                if (peckage.equals(packages[i])) {
                    peckage = null;
                }
            }
        }

        StringBuffer result = new StringBuffer();
        if (peckage != null) {
            result.append(peckage).append('.');
        }
        return result.append(pureName).append(buf).toString();
    }

    public static String getPackageName(Class<?> clazz) {
        return getPackageName(typeToString(clazz, ALLPACKAGES));
    }

    public static String getPureName(Class<?> clazz) {
        return getPureName(typeToString(clazz, ALLPACKAGES));
    }

    public static String getPackageName(String className) {
        int lastDotPos = className.lastIndexOf(".");
        if (lastDotPos < 0) {
            return null;
        } else {
            return className.substring(0, lastDotPos);
        }
    }

    public static String getPureName(String className) {
        int lastDotPos = className.lastIndexOf(".");
        if (lastDotPos < 0) {
            return className;
        } else {
            return className.substring(lastDotPos + 1);
        }
    }

    private static void assertTrue(boolean correct) {
        assertionCount++;
        if (!correct) {
            logger.error("Assertion " + assertionCount + " failed.");
            System.exit(-1);
        }
    }

    public static void main(String[] args) throws Exception {
        assertTrue(!allowsCast(Integer.class, Integer.TYPE));
        assertTrue(allowsCast(null, Integer.class));
        assertTrue(!allowsCast(null, Integer.TYPE));
        assertTrue(allowsCast(Object.class, Integer.class));
        assertTrue(allowsCast(Object.class, Object[].class));
        assertTrue(allowsCast(Cloneable.class, Object[].class));
        assertTrue(!allowsCast(String.class, Number.class));
        assertTrue(!allowsCast(String.class, Number.class));
        assertTrue("int[]".equals(typeToString(int[].class, ALLPACKAGES)));
        assertTrue("int[]".equals(typeToString(int[].class, NOPACKAGES)));
        assertTrue("java.lang.Object[]".equals(typeToString(Object[].class,
                                                            ALLPACKAGES)));
        assertTrue("Object[]".equals(typeToString(Object[].class, NOPACKAGES)));
        assertTrue("java.lang.Integer".equals(typeToString(Integer.class,
                                                           new String[] { "java.lung" })));
        assertTrue("java.lang.Integer".equals(typeToString(Integer.class)));
        assertTrue("Integer".equals(typeToString(Integer.class,
                                                 new String[] { "java.lang" })));
    }
}