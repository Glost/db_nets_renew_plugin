package de.renew.unify;

import java.io.Serializable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class TupleIndex implements Serializable {
    ArityBranch tree = new ArityBranch();
    Set<Object> elements = new HashSet<Object>();

    public TupleIndex() {
    }

    static Integer theHashCode(Object object) {
        return new Integer(object == null ? 0 : object.hashCode());
    }

    public synchronized void insert(final Object elem) {
        elements.add(elem);
        tree.traverse(new TupleIndexVisitor() {
                public void visitBranch(ComponentBranch branch) {
                    branch.size++;
                    branch.all.add(elem);
                }

                public boolean visitIndex(ArityBranch branch, Object remainder) {
                    branch.hashCodeRelation.put(theHashCode(remainder), elem);
                    return true;
                }
            }, elem);
    }

    public synchronized void remove(final Object elem) {
        elements.remove(elem);
        tree.traverse(new TupleIndexVisitor() {
                public void visitBranch(ComponentBranch branch) {
                    branch.size--;
                    branch.all.remove(elem);
                }

                public boolean visitIndex(ArityBranch branch, Object remainder) {
                    branch.hashCodeRelation.remove(theHashCode(remainder), elem);
                    return true;
                }
            }, elem);
    }

    public Set<Object> getAllElements() {
        return elements;
    }

    public Set<Object> getPossibleMatches(Object pattern) {
        EstimateVisitor visitor = new EstimateVisitor();
        tree.traverse(visitor, pattern);
        visitor.possibleCollection(getAllElements());

        return visitor.bestSet;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("de.renew.unify.TupleIndex(");
        Iterator<Object> enumeration = getAllElements().iterator();
        while (enumeration.hasNext()) {
            buffer.append(enumeration.next());
            if (enumeration.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");
        return buffer.toString();
    }
}