package de.uni_hamburg.fs;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Partition implements java.io.Serializable {
    private Set<ConceptImpl> concepts = new HashSet<ConceptImpl>();

    public Partition() {
    }

    public Partition(ConceptImpl con) {
        concepts.add(con);
        con.partitions.add(this);
    }

    public void union(Partition partition) throws TypeException {
        Iterator<ConceptImpl> conciterator = partition.concepts();
        while (conciterator.hasNext()) {
            ConceptImpl con = conciterator.next();
            addConcept(con);
        }
    }

    public void addConcept(ConceptImpl newcon) throws TypeException {
        for (Iterator<ConceptImpl> conciterator = concepts.iterator();
                     conciterator.hasNext();) {
            ConceptImpl con = conciterator.next();
            if (con == newcon) {
                return;
            }
            if (con.isa(newcon) || newcon.isa(con)) {
                throw new TypeException();
            }
        }


        // When adding a concept, the hash code of this partition changes.
        // Therefore we first have to remove the partition from all hash
        // sets that still refer to the old hash code.
        for (Iterator<ConceptImpl> conciterator = concepts.iterator();
                     conciterator.hasNext();) {
            ConceptImpl con = conciterator.next();
            con.partitions.remove(this);
        }

        // Then we can add the new concept.
        concepts.add(newcon);


        // Now we inform all concepts (including the new one) about the
        // changed partition.       
        for (Iterator<ConceptImpl> conciterator = concepts.iterator();
                     conciterator.hasNext();) {
            ConceptImpl con = conciterator.next();
            con.partitions.add(this);
        }
    }

    public boolean containsConcept(ConceptImpl con) {
        return concepts.contains(con);
    }

    public Iterator<ConceptImpl> concepts() {
        return concepts.iterator();
    }

    public String toString() {
        StringBuffer output = new StringBuffer();
        Iterator<ConceptImpl> concs = concepts.iterator();
        for (int i = 1; concs.hasNext(); ++i) {
            if (i > 1) {
                output.append(',');
            }
            ConceptImpl con = concs.next();
            output.append(con.getName());
        }
        return output.toString();
    }

    /**
     * Compares this <code>Partition</code> object to the specified
     * <code>Object</code>.
     * @param that the <code>Object</code> to compare.
     * @return <code>true</code> if the argument is a <code>Partition</code>
     * and comprises the same concepts as this partition.
     **/
    public boolean equals(Object that) {
        if (that instanceof Partition) {
            return ((Partition) that).concepts.equals(concepts);
        }
        return false;
    }

    /**
     * Returns a hashcode for this <code>Partition</code>.
     * @return a hashcode value for this <code>Partition</code>.
     **/
    public int hashCode() {
        return concepts.hashCode();
    }
}