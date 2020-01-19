package de.uni_hamburg.fs;

import collections.CollectionEnumeration;
import collections.HashedMap;
import collections.UpdatableMap;

import de.renew.util.Value;

import java.util.Enumeration;
import java.util.Iterator;


/**
 * A JavaObject wraps a non-primitive java object as a Type and
 * at the same time as a node.
 * It reflects the public (bean) attributes of the java object as features.
 * For wrapping primitive java objects as Types/Nodes, use
 * BasicObjectType/Node.
 **/
public class JavaObject extends JavaClassType implements Node {
    Object javaObject;
    JavaConcept concept;
    UpdatableMap featureCache = new HashedMap();

    /** Construct a new wrapper for the given Java Object. */
    JavaObject(Object javaObject) {
        if (javaObject == null || javaObject instanceof String
                    || javaObject instanceof Value
                    || javaObject.getClass().isArray()
                    || javaObject instanceof Enumeration
                    || javaObject instanceof Iterator) {
            throw new RuntimeException("Someone tried to build a JavaObject for "
                                       + javaObject
                                       + (javaObject == null ? ""
                                                             : "("
                                                             + javaObject.getClass()
                                                                         .getName()
                                                             + ")"));
        }

        this.javaObject = javaObject;
        Class<?> clazz = javaObject.getClass();
        this.concept = TypeSystem.instance().getJavaConcept(clazz);
        setJavaClass(clazz);
    }

    public static JavaType getJavaType(Object javaObject) {
        if (javaObject == null) {
            return NullObject.INSTANCE;
        }


        //      if (javaObject instanceof Class) {
        //        if (javaObject==String.class || javaObject==Value.class) {
        //  	return new BasicType((Class)javaObject);
        //        } else {
        //  	return new ConjunctiveType(TypeSystem.instance().getJavaConcept((Class)javaObject));
        //        }
        //      }
        if (javaObject instanceof String || javaObject instanceof Value) {
            return new BasicType(javaObject);
        }
        if (javaObject.getClass().isArray()) {
            return new JavaArrayType(javaObject);
        }
        if (javaObject instanceof Enumeration) {
            return new JavaArrayType((Enumeration<?>) javaObject);
        }
        if (javaObject instanceof Iterator) {
            return new JavaArrayType((Iterator<?>) javaObject);
        }
        return new JavaObject(javaObject);
    }

    /** Return whether this Type represents an instance. */
    public boolean isInstanceType() {
        return true;
    }

    /** Return the instantiated version of this Type. */
    public Type getInstanceType() {
        return this;
    }

    public Object getJavaObject() {
        return javaObject;
    }

    public int hashCode() {
        // workaround for jdk1.1 bug in hashCode() of java.awt.Dimension:
        if (javaObject instanceof java.awt.Dimension) {
            java.awt.Dimension dim = (java.awt.Dimension) javaObject;
            return dim.width * 3 + dim.height;
        }
        return javaObject.hashCode();
    }

    public boolean equals(Object that) {
        if (that instanceof JavaObject) {
            JavaObject thatJO = (JavaObject) that;
            return javaObject.equals(thatJO.javaObject);
        }
        return false;
    }

    /** Return the name of this Type. */
    public String getName() {
        //return BasicType.objToString(javaObject);
        return concept.getName();
    }

    /** Return the fully qualified name of this Type. */
    public String getFullName() {
        return concept.getFullName();
    }

    public String toString() {
        return BasicType.objToString(javaObject);
    }

    public boolean isApprop(Name featureName) {
        return concept.isApprop(featureName);
    }

    public CollectionEnumeration appropFeatureNames() {
        return concept.appropFeatureNames();
    }

    public Type appropType(Name featureName) {
        return concept.appropType(featureName);
    }

    /** Return whether this Type is extensional. */
    public boolean isExtensional() {
        return concept.isExtensional();
    }

    /** Return whether this Type subsumes <that> Type.
      * In other words, return wheter this Type is more general than <that> Type.
      */
    public boolean subsumes(Type that) {
        if (that instanceof JavaObject) {
            JavaObject thatJO = (JavaObject) that;
            return javaObject.equals(thatJO.javaObject);
        }
        return false;
    }

    /** Return the unification of this Type and <that> Type.
      * this Type is not modified!
      */
    public Type unify(Type that) throws UnificationFailure {
        // special case for other JavaObject:
        if (that instanceof JavaType) {
            JavaType thatJT = (JavaType) that;
            if (javaObject.equals(thatJT.getJavaObject())) {
                return this;
            }
            throw new UnificationFailure();
        }
        return that.unify(this);
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

    public Type getType() {
        return this;
    }

    public Node newNode() {
        return this;
    }

    public CollectionEnumeration featureNames() {
        return appropFeatureNames();
    }

    public boolean hasFeature(Name featureName) {
        return isApprop(featureName);
    }

    public Node delta(Name featureName) {
        // read the feature value from the object:
        if (featureCache.includesKey(featureName)) {
            return (Node) featureCache.at(featureName);
        }
        Object value = concept.getJavaFeature(featureName).getValue(javaObject);
        Node valueNode;
        if (value == javaObject) {
            valueNode = this;
        } else {
            valueNode = getJavaType(value).newNode();
        }
        featureCache.putAt(featureName, valueNode);
        return valueNode;
    }

    /** Returns the Node at the given Path.
     *  The empty path returns the Node itself.
     *  The exception is thrown if at any point, the feature given
     *  by the path does not exist in the current Node.
     */
    public Node delta(Path path) throws NoSuchFeatureException {
        Enumeration<?> featenumeration = path.features();
        Node curr = this;
        while (featenumeration.hasMoreElements()) {
            curr = curr.delta((Name) featenumeration.nextElement());
        }
        return curr;
    }

    /** Sets the value of the feature with the given name.
      *  This method should only be called during construction of
      *  a Node and with a value of the correct type.
      */
    public void setFeature(Name featureName, Node value) {
        // update the real java object:
        JavaFeature feature = concept.getJavaFeature(featureName);
        Object val = ((JavaType) value.getType()).getJavaObject();


        // logger.debug("Setting feature "+featureName+" of object "+javaObject+" to "+val);
        feature.setValue(javaObject, val);
    }

    public void setFeature(Name featureName, Object val) {
        // update the real java object:
        JavaFeature feature = concept.getJavaFeature(featureName);


        // logger.debug("Setting feature "+featureName+" of object "+javaObject+" to "+val);
        feature.setValue(javaObject, val);
    }

    public Node duplicate() {
        return this; // better clone the object (how?)
    }
}