package de.uni_hamburg.fs;

import collections.CollectionEnumeration;
import collections.HashedSet;
import collections.Set;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;


public class ConjunctiveType implements Type {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ConjunctiveType.class);
    ConceptSet concepts = new ConceptSet();

    // "caches" for aggregated concept attributes:
    private OrderedTable features = new OrderedTable();
    private boolean extensional = false;
    private boolean printAny = true;
    private boolean restricted = true;

    /** Set to true if this ConjunctiveType is composed of JavaConcepts.
     *  Careful: isJavaConceptType is not monotonic for subsumption, since
     *  in Type.TOP, it is false, but e.g. in {Frame} (a subtype of Top), it
     *  is true. For type Top, it should really be "unknown".
     */
    private boolean isJavaConceptType = false;

    /** Set to true if this ConjunctiveType is to be instantiated later. */
    private boolean toBeInstantiated = false;

    public ConjunctiveType(boolean restricted) {
        this.restricted = restricted;
    }

    public ConjunctiveType(boolean restricted, boolean printAny) {
        this(restricted);
        this.printAny = printAny;
    }

    public ConjunctiveType(Concept concept) {
        try {
            addConcept(concept);
        } catch (UnificationFailure uff) {
            // Should not happen.
            throw new RuntimeException("***Unexpected failure in addConcept("
                                       + concept + ").");
        }
    }

    public ConjunctiveType(Concept concept, boolean isRestricted)
            throws UnificationFailure {
        this(isRestricted);
        addConcept(concept);
    }

    public ConjunctiveType(ConceptSet concepts) throws UnificationFailure {
        this(concepts, true);
    }

    public ConjunctiveType(ConceptSet concepts, boolean isRestricted,
                           boolean printAny) throws UnificationFailure {
        this(concepts, isRestricted, printAny, false);
    }

    public ConjunctiveType(ConceptSet concepts, boolean isRestricted,
                           boolean printAny, boolean toBeInstantiated)
            throws UnificationFailure {
        this(isRestricted);
        this.printAny = printAny;
        this.toBeInstantiated = toBeInstantiated;
        ConceptEnumeration conceptEnum = concepts.elements();
        while (conceptEnum.hasMoreElements()) {
            addConcept(conceptEnum.nextConcept());
        }
    }

    public ConjunctiveType(ConceptSet concepts, boolean isRestricted)
            throws UnificationFailure {
        this(concepts, isRestricted, true);
    }

    public int hashCode() {
        return concepts.hashCode() + (restricted ? 3 : 0)
               + (toBeInstantiated ? 5 : 0);
    }

    /** Return an Enumeration of all Concepts this ConjunctiveType is composed
     *  of.
     */
    public ConceptEnumeration concepts() {
        return concepts.elements();
    }

    /** Return the name of this Type. */
    public String getName() {
        // special case: for _Top, return a space:
        if (restricted && toBeInstantiated && concepts.isEmpty()) {
            return " ";
        }
        return typeToString(restricted, printAny, false, concepts);
    }

    /** Return the fully qualified name of this Type. */
    public String getFullName() {
        return typeToString(restricted, printAny, true, concepts);
    }

    /** Return a String representation of a ConjunctiveType
     *  or ParsedConjunctiveType. */
    static String typeToString(boolean restricted, boolean printAny,
                               boolean qualified, ConceptSet concepts) {
        StringBuffer output = new StringBuffer();
        if (!restricted && printAny) {
            output.append("Any");
        }
        ConceptEnumeration conceptEnum = concepts.elements();
        while (conceptEnum.hasMoreElements()) {
            if (output.length() > 0) {
                output.append(',');
            }
            Concept concept = (Concept) conceptEnum.nextElement();
            if (qualified) {
                output.append(concept.getFullName());
            } else {
                output.append(concept.getName());
            }
        }
        return output.toString();
    }

    /** Finds or constructs a ConjunctiveType consisting of the given
      * Concepts. This may fail due to incompatibilities.
      */
    public static Type getType(Set concepts) throws UnificationFailure {
        if (concepts.isEmpty()) {
            return Type.TOP;
        }
        CollectionEnumeration conceptEnum = concepts.elements();
        ConjunctiveType newtype = new ConjunctiveType(true);
        while (conceptEnum.hasMoreElements()) {
            newtype.addConcept((Concept) conceptEnum.nextElement());
        }
        return newtype;
    }

    public static Type getType(String conceptsStr)
            throws UnificationFailure, TypeException {
        StringTokenizer conceptNames = new StringTokenizer(conceptsStr, ",");
        HashedSet concepts = new HashedSet();
        TypeSystem ts = TypeSystem.instance();
        while (conceptNames.hasMoreElements()) {
            try {
                concepts.include(ts.conceptForName(conceptNames.nextToken()));
            } catch (NoSuchElementException e) {
                throw new TypeException();
            }
        }
        return getType(concepts);
    }

    public boolean isExtensional() {
        return extensional;
    }

    public boolean isInstanceType() {
        return toBeInstantiated;
    }

    public Type getInstanceType() {
        if (isInstanceType()) {
            return this;
        }
        ConjunctiveType instType = duplicate();
        instType.toBeInstantiated = true;
        return instType;
    }

    public boolean isApprop(Name featureName) {
        if (restricted) {
            return features.includesKey(featureName);
        } else {
            return true;
        }
    }

    public Type appropType(Name featureName) {
        try {
            Type appropType;
            if (restricted || features.includesKey(featureName)) {
                // logger.debug(seqNo + " is queried for type of feature "
                //              + featureName + ", includes: "
                //              + features.includesKey(featureName) + ", result: "
                //              + features.at(featureName));
                ParsedType pt = (ParsedType) features.at(featureName);


                // logger.debug("this: " + this + ", restricted: " + restricted
                //              + ", PT: " + pt);
                appropType = pt.asType();
                if (toBeInstantiated) {
                    appropType = appropType.getInstanceType();
                }
            } else {
                appropType = Type.TOP;
            }
            return appropType;
        } catch (UnificationFailure uff) {
            throw new RuntimeException("Conjunctive type for ConceptSet "
                                       + features.at(featureName)
                                       + " not defined!", uff);
        }
    }

    public CollectionEnumeration appropFeatureNames() {
        return features.keys();
    }

    public boolean subsumes(Type that) {
        if (equals(Type.TOP)) {
            return true;
        }
        if (that instanceof ConjunctiveType) {
            ConjunctiveType thatCT = (ConjunctiveType) that;
            if (!restricted && thatCT.restricted) {
                return false;
            }
            if (toBeInstantiated && !thatCT.toBeInstantiated) {
                return false;
            }
            ConceptEnumeration conceptEnum = concepts.elements();
forAllConcepts: 
            while (conceptEnum.hasMoreElements()) {
                Concept concept = (Concept) conceptEnum.nextElement();
                ConceptEnumeration thatConceptEnum = thatCT.concepts();
                while (thatConceptEnum.hasMoreElements()) {
                    Concept thatConcept = (Concept) thatConceptEnum.nextElement();
                    if (thatConcept.isa(concept)) {
                        continue forAllConcepts;
                    }
                }
                return false; // No that-concept found!
            }
            return true;
        } else if (restricted && (concepts.isEmpty() || isJavaConceptType)) {
            // Java types/objects may only be compatible with Top or
            // with a concept set of JavaConcepts.
            if (that instanceof JavaClassType) {
                JavaConcept jc = TypeSystem.instance()
                                           .getJavaConcept(((JavaClassType) that)
                                                           .getJavaClass());
                ConceptEnumeration conceptEnum = concepts.elements();
                while (conceptEnum.hasMoreElements()) {
                    Concept concept = (Concept) conceptEnum.nextElement();
                    if (!jc.isa(concept)) {
                        return false;
                    }
                }
                return true;
            } else if (that instanceof NullObject) {
                return true;
            }
        }
        return false;
    }

    public boolean canUnify(Type that) {
        try {
            unify(that);
            return true;
        } catch (UnificationFailure uff) {
            return false;
        }
    }

    private void basicAddConcept(Concept newConcept) throws UnificationFailure {
        CollectionEnumeration featenumeration = newConcept.appropFeatureNames();
        while (featenumeration.hasMoreElements()) {
            Name feature = (Name) featenumeration.nextElement();
            ParsedType newType = newConcept.appropParsedType(feature);
            if (features.includesKey(feature)) {
                // this may fail and return null:
                newType = newType.unite((ParsedType) features.at(feature));
                if (newType == null) {
                    throw new UnificationFailure();
                }
            }
            features.putAt(feature, newType);
        }
        extensional = extensional || newConcept.isExtensional();
    }

    /** Adds a part Type to this ConjunctiveType. This method should
      * only be used during the construction of a new Type.
      */
    public void addConcept(Concept newConcept) throws UnificationFailure {
        if (newConcept instanceof JavaConcept
                    && ((JavaConcept) newConcept).getJavaClass()
                                .equals(String.class)) {
            throw new RuntimeException("String wrapped in ConjunctiveType!");
        }
        boolean newIsJavaConcept = newConcept instanceof JavaConcept;
        if (printAny) { // not a dummy type
            if (concepts.isEmpty()) {
                if (!restricted && newIsJavaConcept) {
                    throw new UnificationFailure();
                }
                isJavaConceptType = newIsJavaConcept;
            } else if (isJavaConceptType != newIsJavaConcept) {
                throw new UnificationFailure();
            }
        }

        concepts.joinConcept(newConcept);

        // logger.debug(seqNo + " hashcode " + previousHashCode + " changed to "
        //             + hashCode());
        basicAddConcept(newConcept); // add features & extensionality of newConcept
    }

    private ConjunctiveType duplicate() {
        ConjunctiveType typ = new ConjunctiveType(restricted);
        typ.concepts = new ConceptSet(concepts);
        typ.features = (OrderedTable) features.duplicate();
        typ.extensional = extensional;
        typ.printAny = printAny;
        typ.toBeInstantiated = toBeInstantiated;
        typ.isJavaConceptType = isJavaConceptType;
        return typ;
    }

    public Type unify(Type that) throws UnificationFailure {
        if (subsumes(that)) {
            return that;
        } else if (that.subsumes(this)) {
            return this;
        } else if (that instanceof ConjunctiveType) {
            ConjunctiveType typ = duplicate();
            ConjunctiveType thatCT = (ConjunctiveType) that;
            typ.restricted = typ.restricted && thatCT.restricted;
            if (!(concepts.isEmpty() || thatCT.concepts.isEmpty()
                        || isJavaConceptType == thatCT.isJavaConceptType)) {
                throw new UnificationFailure();
            }
            typ.isJavaConceptType = isJavaConceptType
                                    || thatCT.isJavaConceptType;
            if (typ.isJavaConceptType && !typ.restricted) {
                throw new UnificationFailure();
            }
            typ.toBeInstantiated = typ.toBeInstantiated
                                   || thatCT.toBeInstantiated;
            ConceptEnumeration thatConceptEnum = thatCT.concepts();
            while (thatConceptEnum.hasMoreElements()) {
                Concept thatConcept = (Concept) thatConceptEnum.nextElement();
                typ.addConcept(thatConcept);
            }
            return typ;
        }
        throw new UnificationFailure();
    }

    public Type mostGeneralExtensionalSupertype(Type that) {
        return null;
        /*
        //     System.out.println("Finding most general extensional supertype for "+this+" and "+that+".");
            UpdatableSet thisSupers = new HashedSet();
            Enumeration conceptEnum = concepts.elements();
            while (conceptEnum.hasMoreElements()) {
              Type type = (Concept)conceptEnum.nextElement();
              thisSupers.includeElements(type.extensionalSuperconcepts());
            }
        //    System.out.println("Supertype of "+this+": "+new ConjunctiveType(thisSupers));
            UpdatableSet commonSupers = new HashedSet();
            Enumeration thatConcs = that.concepts();
            while (thatConcs.hasMoreElements()) {
              Type thattype = (Concept)thatConcs.nextElement();
              Enumeration thatSupers = thattype.extensionalSuperconcepts();
              while (thatSupers.hasMoreElements()) {
                 Type thatSuper = (Type)thatSupers.nextElement();
        //    System.out.print("Supertype of "+that+" "+thatSuper.getName());
                 if (thisSupers.includes(thatSuper)) {
        //            System.out.println(" is added!");
                    if (!(new ConjunctiveType(commonSupers).subsumes(new ConjunctiveType(thatSuper)))) {
                       UpdatableSet newCS = new HashedSet();
        //               System.out.print("...and removes some more special concepts:");
                       Enumeration oldCSenum = commonSupers.elements();
                       while (oldCSenumeration.hasMoreElements()) {
                         Type oldSuper = (Type)oldCSenumeration.nextElement();
                         if (!thatSuper.subsumes(oldSuper)) {
                            newCS.include(oldSuper);
                         }
        //                 else
        //                    System.out.print(" "+oldSuper.getName());
                       }
                       commonSupers = newCS;
        //               System.out.println();
                    }
                    commonSupers.include(thatSuper);
                 }
        //         else
        //           System.out.println(" ruled out.");
              }
            }
        //    System.out.println("Most general extensional supertype for "+this+" and "+that+" is "+new ConjunctiveType(commonSupers)+".");
            return new ConjunctiveType(commonSupers);
        */
    }

    /** Return a new node from this type.
     */
    public Node newNode() {
        if (restricted && features.isEmpty()) {
            return new NoFeatureNode(this);
        } else {
            //        if (concepts.size()==1) {
            //  	Concept onlyConcept=concepts.elements().nextConcept();
            //  	if (onlyConcept instanceof JavaConcept &&
            //  	    ((JavaConcept)onlyConcept).canInstantiate()) {
            //  	    return new JavaObjectNode((JavaConcept)onlyConcept);
            //  	}
            //        }
            return new FSNode(this);
        }
    }

    public String toString() {
        String name = getName();
        if (name.length() == 0) {
            return "Top";
        } else {
            return name;
        }
    }

    /** Compares this ConjunctiveType with another Object. Two ConjunctiveTypes
     * are considered equal iff they consist of the same set of Concepts.
     */
    public boolean equals(Object that) {
        if (that instanceof ConjunctiveType) {
            ConjunctiveType thatType = (ConjunctiveType) that;
            return restricted == thatType.restricted
                   && toBeInstantiated == thatType.toBeInstantiated
                   && isJavaConceptType == thatType.isJavaConceptType
                   && concepts.equals(thatType.concepts);
        } else {
            return false;
        }
    }

    JavaConcept getOnlyInstantiableJavaConcept() {
        // check if there is exactly one JavaConcept to instantiate:
        if (isJavaConceptType && concepts.size() == 1) {
            Concept onlyConcept = concepts.elements().nextConcept();
            if (((JavaConcept) onlyConcept).canInstantiate()) {
                return (JavaConcept) onlyConcept;
            }
        }
        return null;
    }
}