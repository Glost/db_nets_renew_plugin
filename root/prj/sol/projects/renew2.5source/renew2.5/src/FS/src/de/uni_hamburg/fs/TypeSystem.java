package de.uni_hamburg.fs;

import collections.HashedMap;
import collections.HashedSet;
import collections.UpdatableMap;

import de.renew.util.ClassSource;

import java.util.Enumeration;
import java.util.NoSuchElementException;


public class TypeSystem implements java.io.Serializable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(TypeSystem.class);
    private static TypeSystem _instance = null;
    private ConceptImpl root = new ConceptImpl();
    private Partition topPartition;
    private UpdatableMap concepts = new HashedMap();

    public TypeSystem() {
        // logger.debug("New TypeSystem.");
    }

    private void init() {
        topPartition = new Partition();
        transitiveClosure();
    }

    public void transitiveClosure() {
        root.transitiveClosure();
    }

    public void recalcDirectIsa() throws TypeException {
        root.transitiveClosure();
        root.recalcDirectIsa(new HashedSet());
    }

    public void inheritFeatures() throws UnificationFailure {
        recalcDirectIsa();
        root.inherit(new HashedSet());
        root.buildFeatureTypes(new HashedSet());
    }

    public Partition getTopPartition() {
        return topPartition;
    }

    public static TypeSystem instance() {
        if (_instance == null) {
            return newInstance();
        } else {
            return _instance;
        }
    }

    public static TypeSystem newInstance() {
        _instance = new TypeSystem();
        _instance.init();
        return _instance;
    }

    public static void setInstance(TypeSystem instance) {
        _instance = instance;
    }

    public ConceptImpl getRoot() {
        return root;
    }

    public void newRoot() {
        ConceptImpl newRoot = new ConceptImpl();
        root.basicAddIsa(newRoot);
        topPartition = new Partition();
        root = newRoot;
    }

    public void addConcept(Concept concept) {
        if (concept instanceof ConceptImpl) {
            ConceptImpl ci = (ConceptImpl) concept;
            ci.basicAddIsa(root);
            if (ci.isDummy()) {
                return;
            }
        }
        concepts.putAt(new Name(concept.getFullName()), concept);
    }

    public void removeConcept(Concept concept) {
        concepts.removeAt(concept.getFullName());
    }

    public boolean hasConcept(String name) {
        return hasConcept(new Name(name));
    }

    public boolean hasConcept(Name name) {
        if (concepts.includesKey(name)) {
            return true;
        }
        try {
            getJavaClass(name.toString());
            return true;
        } catch (TypeException tee) {
            return false;
        }
    }

    public Concept conceptForName(String name) {
        return conceptForName(new Name(name));
    }

    public Concept conceptForName(Name name) {
        Concept concept = null;
        try {
            concept = (Concept) concepts.at(name);
        } catch (NoSuchElementException nse) {
            try {
                concept = getJavaConcept(getJavaClass(name.toString()));
            } catch (TypeException tee) {
                logger.debug("No concept found for name " + name + " in "
                             + concepts);
                logger.debug("Interpretation of name " + name
                             + " as Java concept failed:", tee);
                throw new NoSuchElementException("No concept found for name "
                                                 + name);
            }
        }
        return concept;
    }

    public JavaConcept getJavaConcept(Class<?> javaClass) {
        Name name = new Name(javaClass.getName());
        if (concepts.includesKey(name)) {
            Concept concept = (Concept) concepts.at(name);
            if (concept instanceof JavaConcept) {
                return (JavaConcept) concept;
            }
        }
        JavaConcept jc = new JavaConcept(javaClass);
        addConcept(jc);
        return jc;
    }

    public Class<?> getJavaClass(String javaClassName)
            throws TypeException {
        try {
            return ClassSource.classForName(javaClassName);
        } catch (ClassNotFoundException cnf1) {
            throw new TypeException(cnf1);
        } catch (IllegalArgumentException iae) {
            throw new TypeException(iae);
        } catch (LinkageError le) {
            throw new TypeException(le);
        }
    }

    public Type getType(Class<?> javaClass) {
        if (javaClass.isPrimitive() || javaClass == String.class) {
            return new BasicType(javaClass);
        } else if (javaClass.isArray()) {
            return new ListType(getType(javaClass.getComponentType()));
        } else if (Enumeration.class.isAssignableFrom(javaClass)) {
            return new ListType(getType(Object.class));
        } else {
            return new ConjunctiveType(getJavaConcept(javaClass));
        }
    }

    ParsedType getParsedType(Class<?> javaClass) {
        if (javaClass.isPrimitive() || javaClass == String.class) {
            return (ParsedType) getType(javaClass);
        }
        if (javaClass.isArray()) {
            return new ParsedListType(false,
                                      getParsedType(javaClass.getComponentType()));
        }
        if (Enumeration.class.isAssignableFrom(javaClass)) {
            return new ParsedListType(false, getParsedType(Object.class));
        }
        return new ParsedConjunctiveType(new ConceptSet(getJavaConcept(javaClass)));
    }
}