package de.uni_hamburg.fs;

import collections.CollectionEnumeration;
import collections.HashedSet;
import collections.Set;
import collections.UpdatableSet;

import de.renew.util.ClassSource;
import de.renew.util.StringUtil;
import de.renew.util.Types;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Enumeration;


public class JavaConcept implements Concept {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(JavaConcept.class);
    public static final Object[] NOPARAM = new Object[] {  };
    public static final Class<?>[] NOPARAMDEF = new Class<?>[] {  };
    static final Object[] FIRSTINDEX = new Object[] { new Integer(0) };
    private static final UpdatableSet dontUse = new HashedSet();

    static {
        dontUse.include("getClass");
        dontUse.include("hashCode");
        dontUse.include("clone");
        dontUse.include("getPeer");
        dontUse.include("create");
        dontUse.include("build");
        dontUse.include("toString"); // or should we allow this?
        dontUse.include("paramString"); // or should we allow this?


        //dontInstantiate.include("java.awt.EventQueue");
        dontUse.include("getNextEvent");
        dontUse.include("peekEvent");
    }

    private static String VISIBILITY_CHARS = "-#+";
    private Class<?> javaClass;
    private String javaClassName;
    private transient OrderedTable approp = new OrderedTable();

    // from featureNames to Classes or JavaFeatures
    private boolean isClass;
    private boolean canInstantiate = false;

    /** Construct a new wrapper for the given Java Type. */
    JavaConcept(Class<?> javaClass) {
        if (Enumeration.class.isAssignableFrom(javaClass)) {
            throw new RuntimeException("Someone tried to build a JavaConcept for "
                                       + javaClass.getName());
        }
        int mods = javaClass.getModifiers();
        this.javaClass = javaClass;
        javaClassName = javaClass.getName();


        //logger.debug("Building JavaConcept for "+javaClassName); 
        isClass = !Modifier.isAbstract(mods) && !Modifier.isInterface(mods);
        if (!Modifier.isPublic(mods)) {
            javaClassName += "(n/a)";
        } else {
            findFeatures(new StringBuffer());
        }
    }

    /** Return the name of this JavaConcept. */
    public String getName() {
        int dotpos = javaClassName.lastIndexOf(".");
        if (dotpos == -1) {
            return javaClassName;
        } else {
            return javaClassName.substring(dotpos + 1);
        }
    }

    /** Return the name of the namespace this Concept belongs to. */
    public String getNamespace() {
        return getNamespace(javaClassName);
    }

    public static String getNamespace(String javaClassName) {
        int dotpos = javaClassName.lastIndexOf(".");
        if (dotpos == -1) {
            return javaClassName;
        } else {
            return javaClassName.substring(0, dotpos);
        }
    }

    /** Return the full name of this Concept in the form
     * namespace::name.
     */
    public String getFullName() {
        return javaClassName;
    }

    /** Return the Java Type that is wrapped by this JavaConcept.
      */
    public Class<?> getJavaClass() {
        return javaClass;
    }

    public boolean canInstantiate() {
        return canInstantiate;
    }

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        findFeatures(new StringBuffer());
    }

    /** Returns the JavaFeature for the given feature.
     *  Only call this method for instantiable JavaConcepts!
     */
    JavaFeature getJavaFeature(Name featureName) {
        return (JavaFeature) approp.at(featureName);
    }

    /** Return whether this JavaConcept is-a <that> Concept.
      * In other words, return wheter this Concept is more special than <that>
      * Concept.
      */
    public boolean isa(Concept that) {
        if (that instanceof JavaConcept) {
            Class<?> thatJavaClass = ((JavaConcept) that).getJavaClass();
            return thatJavaClass.isAssignableFrom(getJavaClass());
        }
        return false;
    }

    /** Return whether this JavaConcept is-not-a "that" Concept.
      * In other words, return wheter this JavaConcept is incompatible with
      *  "that" JavaConcept. This is the case if
      * <ol><i>the other Concept is not a JavaConcept,
      *        <i>both JavaConcepts are classes which are not in a subclass
      *              relation, or</i>
      *        <i> the Java classes/interfaces define methods in an
      *               incompatible way.</i></ol>
      */
    public boolean isNotA(Concept that) {
        // the following is checked before caling isNotA,
        // so it doesn't have to be checked again:
        //    if (this.isa(that) || that.isa(this))
        //       return false;
        //      if (that instanceof JavaObjectFS) {
        //         return true; // the only chance was: that.isa(this)
        //      }
        if (Modifier.isFinal(getJavaClass().getModifiers())) {
            return true;
        }
        if (that instanceof JavaConcept) {
            Class<?> thatJavaClass = ((JavaConcept) that).getJavaClass();
            if (Modifier.isFinal(thatJavaClass.getModifiers())) {
                return true;
            }
            if (thatJavaClass.isInterface()) {
                return false; // method-inconsistencies are not checked yet!
            }
        }
        if (getJavaClass().isInterface()) {
            return false; // method-inconsistencies are not checked yet!
        }
        return true;
    }

    /** Return whether this Concept is extensional. */
    public boolean isExtensional() {
        return false;
    }

    /** Look for the most general common extensional supertype of this and "that". */
    public ConceptEnumeration extensionalSuperconcepts() {
        return null; // not extensional, should never be called!
    }

    /** Return whether the feature <feature> is appropriate in this Concept.
     */
    public boolean isApprop(Name feature) {
        return approp.includesKey(feature);
    }

    private Class<?> getClassOf(Name feature) {
        if (isClass) {
            return getJavaFeature(feature).getJavaClass();
        } else {
            return (Class<?>) approp.at(feature);
        }
    }

    /** Return the required ParsedType for the given feature. */
    public ParsedType appropParsedType(Name featureName)
            throws NoSuchFeatureException {
        return TypeSystem.instance().getParsedType(getClassOf(featureName));
    }

    /** Return the required Type of the Value found under the given feature. */
    public Type appropType(Name featureName) throws NoSuchFeatureException {
        return TypeSystem.instance().getType(getClassOf(featureName));
    }

    /** Return an Enumeration of all appropriate features. */
    public CollectionEnumeration appropFeatureNames() {
        return approp.keys();
    }

    public String toString() {
        return getName(); //+"@"+hashCode();
    }

    public int hashCode() {
        return getJavaClass().hashCode();
    }

    public boolean equals(Object that) {
        if (that instanceof JavaConcept) {
            return ((JavaConcept) that).getJavaClass().equals(getJavaClass());
        }
        return false;
    }

    public Set findFeatures(StringBuffer umlDescr) {
        return findFeatures(umlDescr, getWellKnownPackages(javaClass));
    }

    public Set findFeatures(StringBuffer umlDescr, String[] wellKnown) {
        //   Object sample=null;
        // Do not try to build a sample instance anymore!
        // Problems:
        // * some classes have side effects when instantiated
        //   (e.g. java.awt.EventQueue)
        // * if we detect a "volatile" method, it may be inherited from
        //   an abstract superclass or an interface. The type system does
        //   not allow to remove features in subtypes!
        //
        //      if (!dontInstantiate.includes(javaClassName)) {
        //        try {
        //  	// try to build an instance of this class:
        //  	sample=javaClass.newInstance();
        //  	//logger.debug("Built a sample instance of "+javaClassName);
        //        } catch (Throwable e) {
        //  	//logger.debug("Cannot instantiate "+javaClass);
        //        }
        //      }
        //      canInstantiate=sample!=null;
        // Instead, check for no-arg constructor:
        try {
            javaClass.getConstructor(NOPARAMDEF);
            canInstantiate = true;
        } catch (Throwable t) {
            // no accessible no-arg constructor, leave canInstantiate false.
        }
        addClassDescr(javaClass, umlDescr);
        if (javaClass == String.class) {
            return new HashedSet();
        }
        addFieldFeatures(umlDescr, wellKnown);
        return addMethodFeatures(umlDescr, wellKnown);
    }

    private static void addClassDescr(Class<?> clazz, StringBuffer umlDescr) {
        if (clazz.isInterface()) {
            umlDescr.append("\u00abinterface\u00bb\n");
        }
        int mods = clazz.getModifiers();
        if (!Modifier.isPublic(mods)) {
            addModifierDescr(mods, umlDescr, false);
        }
        umlDescr.append(Types.typeToString(clazz, null));
        umlDescr.append("\n");
    }

    public static int getVisibilityLevel(int mods) {
        if (Modifier.isPublic(mods)) {
            return 2;
        } else if (Modifier.isPrivate(mods)) {
            return 0;
        } else {
            return 1;
        }
    }

    private static void addModifierDescr(int mods, StringBuffer umlDescr,
                                         boolean showPublic) {
        if (Modifier.isStatic(mods)) {
            umlDescr.append("_");
        } else if (Modifier.isAbstract(mods) && !Modifier.isInterface(mods)) {
            umlDescr.append("\\");
        }
        if (showPublic || !Modifier.isPublic(mods)) {
            umlDescr.append(VISIBILITY_CHARS.charAt(getVisibilityLevel(mods)))
                    .append(" ");
        }
    }

    public static void loadClass(Class<?> javaClass) {
        if (!javaClass.isPrimitive() && !javaClass.isArray()) {
            try {
                // TODO: Find out what Frank thought.
                //       Which class loader should be used here?
                //       Is the bug mentioned above really a bug
                //       or due to different class loaders?
                ClassSource.classForName(javaClass.getName());
            } catch (ClassNotFoundException e) {
                // should never happen!
                logger.error(e.getMessage(), e);
            } catch (LinkageError e) {
                // should never happen!
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void addFieldFeatures(StringBuffer umlDescr, String[] wellKnown) {
        Field[] field = javaClass.getFields();
        for (int i = 0; i < field.length; ++i) {
            int mod = field[i].getModifiers();
            if (!Modifier.isStatic(mod) && !Modifier.isVolatile(mod)) {
                Class<?> featureType = field[i].getType();


                // JAVA BUG: assure that featureType is loaded correctly:
                loadClass(featureType);
                if (!Enumeration.class.isAssignableFrom(featureType)) {
                    Name featureName = new Name(field[i].getName());
                    Object javaType;
                    if (isClass) {
                        javaType = new FieldFeature(field[i]);
                    } else {
                        javaType = featureType;
                    }
                    try {
                        javaClass.getDeclaredField(featureName.name);
                        umlDescr.append(featureName).append(": ")
                                .append(Types.typeToString(featureType,
                                                           wellKnown))
                                .append("\n");
                    } catch (NoSuchFieldException e) {
                    } catch (SecurityException s) {
                    }
                    approp.putAt(featureName, javaType);
                }
            }
        }
    }

    public static String toFeatureName(String getMethodName) {
        int startIndex = 0;
        if (getMethodName.startsWith("get")) {
            startIndex = 3;
        } else if (getMethodName.startsWith("is")) {
            startIndex = 2;
        }
        if (startIndex > 0
                    && !StringUtil.isUpperCaseAt(getMethodName, startIndex)) {
            startIndex = 0; // did not continue with upper case character!
        }
        getMethodName = getMethodName.substring(startIndex);
        if (StringUtil.isUpperCaseAt(getMethodName, 1)) {
            // if more than one upper case character, leave as is
            return getMethodName;
        }
        return StringUtil.firstToLowerCase(getMethodName);
    }

    private boolean isDeclared(Class<?> clazz, Method method) {
        try {
            clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
            return true;
        } catch (NoSuchMethodException e) {
        } catch (SecurityException s) {
        }
        return false;
    }

    private Set addMethodFeatures(StringBuffer umlDescr, String[] wellKnown) {
        UpdatableSet usedMethods = new HashedSet();
        Method[] method = javaClass.getMethods();
        for (int i = 0; i < method.length; ++i) {
            Class<?> featureType = method[i].getReturnType();
            if (Modifier.isPublic(featureType.getModifiers())) {
                int mod = method[i].getModifiers();
                String featureStr = method[i].getName();
                Class<?>[] paramType = method[i].getParameterTypes();
                int noParams = paramType.length;
                if (!Modifier.isStatic(mod) && featureType != Void.TYPE
                            && noParams <= 1 && !dontUse.includes(featureStr)) {
                    // construct feature name:
                    featureStr = toFeatureName(featureStr);
                    Name featureName = new Name(featureStr);


                    // JAVA BUG: assure that featureType is loaded correctly:
                    loadClass(featureType);
                    Object javaType = null;

                    // check for index feature:
                    if (noParams == 1 && paramType[0] == Integer.TYPE) {
                        javaType = findIndexFeature(method[i], featureStr,
                                                    featureType, usedMethods); //,sample);
                        featureName = new Name(featureStr + "s"); // set to plural
                    } else if (noParams == 0) {
                        // check for bean feature.
                        if (!isClass) {
                            javaType = featureType;
                        } else {
                            //if (checkResult(featureStr,method[i],sample))
                            Method setMethod = findSetMethod(featureStr,
                                                             featureType);
                            if (setMethod != null) {
                                usedMethods.include(setMethod);
                            }
                            javaType = new BeanFeature(method[i], setMethod);
                        }
                        usedMethods.include(method[i]);
                    }
                    if (javaType != null) {
                        boolean canWrite = isClass
                                           && ((JavaFeature) javaType).canSet();
                        if (canWrite || !approp.includesKey(featureName)) {
                            approp.putAt(featureName, javaType);
                            if (isClass) {
                                featureType = ((JavaFeature) javaType)
                                                  .getJavaClass();
                            } else {
                                featureType = (Class<?>) javaType;
                            }
                            if (isDeclared(javaClass, method[i])) {
                                umlDescr.append(featureName).append(": ")
                                        .append(Types.typeToString(featureType,
                                                                   wellKnown));
                                if (isClass && !canWrite) {
                                    umlDescr.append(" {readOnly}");
                                }
                                umlDescr.append("\n");
                            }
                        }
                    }
                }
            }
        }
        return usedMethods;
    }

    public static String[] getWellKnownPackages(Class<?> clazz) {
        return new String[] { "java.lang", Types.getPackageName(clazz) };
    }

    public String getClassDescription() {
        return getClassDescription(getWellKnownPackages(javaClass));
    }

    public String getClassDescription(String[] wellKnown) {
        // re-establish features and use protocol as description:
        approp = new OrderedTable();
        StringBuffer umlDescr = new StringBuffer();
        Set usedMethods = findFeatures(umlDescr, wellKnown);
        umlDescr.append("\n");
        Method[] method = javaClass.getDeclaredMethods();
        for (int i = 0; i < method.length; ++i) {
            int mods = method[i].getModifiers();
            if (Modifier.isPublic(mods) && !Modifier.isStatic(mods)
                        && !usedMethods.includes(method[i])) {
                addMethodDescription(method[i], umlDescr, wellKnown);
            }
        }
        return umlDescr.toString();
    }

    public static int getVisibilityLevel(char visibility) {
        return VISIBILITY_CHARS.indexOf(visibility);
    }

    public static String getImplementationDescription(Class<?> clazz,
                                                      int visibilityLevel) {
        return getImplementationDescription(clazz, visibilityLevel,
                                            getWellKnownPackages(clazz));
    }

    public static String getImplementationDescription(Class<?> clazz,
                                                      int visibilityLevel,
                                                      String[] wellKnown) {
        //logger.debug(Types.typeToString(clazz,Types.ALLPACKAGES));
        //if (clazz.getSuperclass()!=null)
        //logger.debug(" extends "+Types.typeToString(clazz.getSuperclass(),
        //						  Types.ALLPACKAGES));
        //Class[] interfaces=clazz.getInterfaces();
        //for (int i=0; i<interfaces.length; ++i)
        //  logger.debug(" implements "+Types.typeToString(interfaces[i],
        //						       Types.ALLPACKAGES));
        StringBuffer umlDescr = new StringBuffer();
        addClassDescr(clazz, umlDescr);
        Field[] field = clazz.getDeclaredFields();
        for (int i = 0; i < field.length; ++i) {
            if (field[i].getName().indexOf("$") < 0) {
                int mods = field[i].getModifiers();
                if (getVisibilityLevel(mods) >= visibilityLevel) {
                    addModifierDescr(mods, umlDescr, true);
                    umlDescr.append(field[i].getName()).append(": ")
                            .append(Types.typeToString(field[i].getType(),
                                                       wellKnown));
                    if (Modifier.isFinal(mods)) {
                        umlDescr.append(" {frozen}");
                    }
                    umlDescr.append("\n");
                }
            }
        }
        umlDescr.append("\n");
        Method[] method = clazz.getDeclaredMethods();
        for (int i = 0; i < method.length; ++i) {
            if (method[i].getName().indexOf("$") < 0) {
                int mods = method[i].getModifiers();
                if (getVisibilityLevel(mods) >= visibilityLevel) {
                    if (!clazz.isInterface()) { // everything's public+abstract anyway
                        addModifierDescr(mods, umlDescr, true);
                    }
                    addMethodDescription(method[i], umlDescr, wellKnown);
                }
            }
        }
        return umlDescr.toString();
    }

    private static void addMethodDescription(Method method,
                                             StringBuffer umlDescr,
                                             String[] wellKnown) {
        umlDescr.append(method.getName()).append("(");
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int p = 0; p < paramTypes.length; ++p) {
            umlDescr.append(Types.typeToString(paramTypes[p], wellKnown));
            if (p < paramTypes.length - 1) {
                umlDescr.append(", ");
            }
        }
        umlDescr.append(")");
        Class<?> returnType = method.getReturnType();
        if (returnType != Void.TYPE) {
            umlDescr.append(": ")
                    .append(Types.typeToString(returnType, wellKnown));
        }
        umlDescr.append("\n");
    }

    private Method findSetMethod(String featureStr, Class<?> featureType) {
        // look for appropriate set method:
        Method setMethod = null;
        if (canInstantiate) {
            String setStr = "set" + StringUtil.firstToUpperCase(featureStr);
            try {
                setMethod = javaClass.getMethod(setStr,
                                                new Class<?>[] { featureType });
                if (Modifier.isStatic(setMethod.getModifiers())
                            || setMethod.getReturnType() != Void.TYPE) {
                    // or should we allow a returned object?
                    setMethod = null;
                }
            } catch (Throwable e1) {
                // We also continue if there is no appropriate set method.
            }
        }
        return setMethod;
    }

    private Object findIndexFeature(Method getMethod, String featureStr,
                                    Class<?> featureType,
                                    UpdatableSet usedMethods) {
        //,Object sample) {
        if (featureType.isPrimitive()) {
            return null;
        }
        Object javaType = null;
        try {
            // look for "...Count"-method (mandatory):
            Method countMethod = javaClass.getMethod(getMethod.getName()
                                                     + "Count", NOPARAMDEF);
            if (!Modifier.isStatic(countMethod.getModifiers())
                        && countMethod.getReturnType() == Integer.TYPE) {
                //&& checkResult(featureStr,countMethod,sample)) {
                usedMethods.include(countMethod);
                usedMethods.include(getMethod);
                if (isClass) {
                    // look for "add..."-method (optional):
                    Method addMethod = null;
                    try {
                        addMethod = javaClass.getMethod("add"
                                                        + StringUtil
                                        .firstToUpperCase(featureStr),
                                                        new Class<?>[] { featureType });
                    } catch (Throwable t1) {
                        try {
                            addMethod = javaClass.getMethod("add",
                                                            new Class<?>[] { featureType });
                        } catch (Throwable t2) {
                            //logger.debug("AddMethod add"+StringUtil.firstToUpperCase(featureStr)+
                            //		   "not found: "+t1);
                            //logger.debug("AddMethod add not found: "+t2);
                        }
                    }
                    if (addMethod != null) {
                        usedMethods.include(addMethod);
                    }


                    //logger.debug("Found addMethod:\n"+addMethod);
                    javaType = new IndexFeature(countMethod, getMethod,
                                                addMethod);
                } else {
                    javaType = Array.newInstance(featureType, 0).getClass();
                }
            }
        } catch (Throwable t0) {
            javaType = null;
        }
        return javaType;
    }
}