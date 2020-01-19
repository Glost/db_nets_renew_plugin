package de.renew.unify;

import java.io.Serializable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class ComponentBranch implements Serializable {
    int size = 0;
    ArityBranch[] indexes;
    final Set<Object> all = new HashSet<Object>();

    ComponentBranch(int arity) {
        indexes = new ArityBranch[arity];
        for (int i = 0; i < arity; i++) {
            indexes[i] = new ArityBranch();
        }
    }

    public Iterator<Object> elements() {
        return all.iterator();
    }

    void traverse(TupleIndexVisitor visitor, Object remainder) {
        for (int i = 0; i < indexes.length; i++) {
            // If we reach this point, the current remainder must
            // be a non-empty aggregate.
            indexes[i].traverse(visitor,
                                ((Aggregate) remainder).references[i].value);
        }
        visitor.visitBranch(this);
    }
}