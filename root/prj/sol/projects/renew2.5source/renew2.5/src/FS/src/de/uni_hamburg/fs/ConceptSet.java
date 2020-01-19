package de.uni_hamburg.fs;

import collections.CollectionEnumeration;
import collections.HashedSet;
import collections.Set;
import collections.UpdatableSet;

import java.util.Enumeration;


public class ConceptSet implements java.io.Serializable {
    public static final ConceptSet EMPTY = new ConceptSet();
    private UpdatableSet concepts = new HashedSet();
    private int hashCode = 0;

    public ConceptSet() {
    }

    public ConceptSet(Concept con) {
        concepts.include(con);
        hashCode = con.hashCode();
    }

    public ConceptSet(ConceptSet cs) {
        concepts = (UpdatableSet) cs.concepts.duplicate();
        hashCode = cs.hashCode;
    }

    public ConceptSet(Enumeration<Concept> concepts) {
        while (concepts.hasMoreElements()) {
            addConcept(concepts.nextElement());
        }
    }

    public boolean isEmpty() {
        return concepts.isEmpty();
    }

    public int size() {
        return concepts.size();
    }

    public ConceptEnumeration elements() {
        return new ConceptEnumeration(concepts.elements());
    }

    public void addConcept(Concept newConcept) {
        if (!concepts.includes(newConcept)) {
            concepts.include(newConcept);
            hashCode += newConcept.hashCode();
        }
    }

    public void unite(ConceptSet that) {
        concepts.includeElements(that.elements());
        hashCode = -1;
    }

    public void joinConcept(Concept newConcept) throws UnificationFailure {
        UpdatableSet newConcepts = new HashedSet();
        ConceptEnumeration conceptEnum = elements();
        while (conceptEnum.hasMoreElements()) {
            Concept concept = conceptEnum.nextConcept();
            if (concept.isa(newConcept)) {
                return; // already something more special there
            }

            // only include concepts which are not more general than the new:
            if (!newConcept.isa(concept)) {
                // concepts are incomparable: test compatibility!
                if (concept.isNotA(newConcept)) {
                    throw new UnificationFailure(); // incompatible!
                }
                newConcepts.include(concept);
            }
        }


        // now that we have reduced the set of concepts, check
        // for unification with one of these concepts.
        //        concepts = new HashedSet();
        //        boolean addNew=true;
        //        Enumeration concEnum = newConcepts.elements();
        //        while (concEnum.hasMoreElements()) {
        //  	  Concept concept = (Concept)concEnum.nextElement();
        //  	  Concept uni=newConcept.unify(concept);
        //  	  if (uni==null) {
        //  	      concepts.include(concept);
        //  	  } else {
        //  	      addNew=false;
        //  	      concepts.include(uni);
        //  	  }
        //        }
        //        if (addNew)
        //  	  concepts.include(newConcept);
        newConcepts.include(newConcept);
        concepts = newConcepts;
        hashCode = -1;
    }

    public void join() throws UnificationFailure {
        ConceptSet newConcepts = new ConceptSet();
        newConcepts.joinConcepts(elements());
        concepts = newConcepts.concepts;
    }

    public void joinConcepts(ConceptEnumeration conceptEnum)
            throws UnificationFailure {
        while (conceptEnum.hasMoreElements()) {
            joinConcept(conceptEnum.nextConcept());
        }
    }

    public void meetConcept(Concept newConcept) {
        UpdatableSet newConcepts = new HashedSet();
        ConceptEnumeration conceptEnum = elements();
        while (conceptEnum.hasMoreElements()) {
            Concept concept = conceptEnum.nextConcept();
            if (newConcept.isa(concept)) {
                return; // already something more general there
            }

            // only include concepts which are not more special than the new:
            if (!concept.isa(newConcept)) {
                newConcepts.include(concept);
            }
        }
        newConcepts.include(newConcept);
        concepts = newConcepts;
        hashCode = -1;
    }

    public void meet() {
        ConceptSet newConcepts = new ConceptSet();
        ConceptEnumeration conceptEnum = elements();
        while (conceptEnum.hasMoreElements()) {
            Concept concept = conceptEnum.nextConcept();
            newConcepts.meetConcept(concept);
        }
        concepts = newConcepts.concepts;
    }

    //    public boolean equals(Object that) {
    //        if (that instanceof ConceptSet) {
    //  	  //	  logger.debug("Comparing"+concepts+" and "+
    //  	  //			     ((ConceptSet)that).concepts+": "+
    //  	  //			     concepts.sameStructure(((ConceptSet)that).concepts));
    //  	  return ((ConceptSet)that).concepts.sameStructure(concepts);
    //        }
    //        return false;
    //    }
    public boolean equals(Object that) {
        if (that instanceof ConceptSet) {
            return equalSet(((ConceptSet) that).concepts, concepts);
        }
        return false;
    }

    private static boolean equalSet(Set a, Set b) {
        if (a.size() != b.size()) {
            return false;
        }
        CollectionEnumeration aEnum = a.elements();
        for (int i = a.size(); i > 0; --i) {
            if (!b.includes(aEnum.nextElement())) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        if (hashCode == -1) {
            hashCode = 0;
            ConceptEnumeration concenumeration = elements();
            while (concenumeration.hasMoreElements()) {
                hashCode += concenumeration.nextConcept().hashCode();
            }
        }
        return hashCode;
    }

    /** Return a String representation of this ConceptSet. */
    public String toString() {
        StringBuffer output = new StringBuffer("{");
        ConceptEnumeration conceptEnum = new ConceptEnumeration(concepts
                                             .elements());
        for (int i = 1; conceptEnum.hasMoreElements(); ++i) {
            if (i > 1) {
                output.append(',');
            }
            Concept concept = conceptEnum.nextConcept();
            output.append(concept.getName()); //+" ["+concept.getClass()+"]");
        }
        output.append('}');
        return output.toString();
    }
}